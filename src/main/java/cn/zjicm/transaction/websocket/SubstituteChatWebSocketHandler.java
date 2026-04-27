package cn.zjicm.transaction.websocket;

import cn.zjicm.transaction.model.ChatMessage;
import cn.zjicm.transaction.repository.SubstitutePostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SubstituteChatWebSocketHandler extends TextWebSocketHandler {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final UriTemplate CHAT_URI_TEMPLATE = new UriTemplate("/ws/substitutes/{id}");

    private final SubstitutePostRepository substitutePostRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByPostId = new ConcurrentHashMap<>();

    public SubstituteChatWebSocketHandler(SubstitutePostRepository substitutePostRepository, ObjectMapper objectMapper) {
        this.substitutePostRepository = substitutePostRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long postId = getPostId(session);
        substitutePostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "代课信息不存在"));
        sessionsByPostId.computeIfAbsent(postId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long postId = getPostId(session);
        IncomingMessage incomingMessage = readIncomingMessage(message.getPayload());
        if (incomingMessage == null
                || isBlank(incomingMessage.senderName())
                || isBlank(incomingMessage.content())) {
            sendSystemError(session, "昵称和消息内容都不能为空。");
            return;
        }

        ChatMessage savedMessage = substitutePostRepository.addMessage(
                postId,
                incomingMessage.senderName(),
                incomingMessage.content()
        );
        broadcast(postId, OutgoingMessage.from(savedMessage));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long postId = getPostId(session);
        Set<WebSocketSession> sessions = sessionsByPostId.getOrDefault(postId, Collections.emptySet());
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByPostId.remove(postId);
        }
    }

    private IncomingMessage readIncomingMessage(String payload) {
        try {
            return objectMapper.readValue(payload, IncomingMessage.class);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    private void broadcast(Long postId, OutgoingMessage outgoingMessage) throws IOException {
        String payload = objectMapper.writeValueAsString(outgoingMessage);
        TextMessage textMessage = new TextMessage(payload);
        for (WebSocketSession session : sessionsByPostId.getOrDefault(postId, Collections.emptySet())) {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
    }

    private void sendSystemError(WebSocketSession session, String content) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(OutgoingMessage.error(content))));
        }
    }

    private Long getPostId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        return Long.valueOf(CHAT_URI_TEMPLATE.match(path).get("id"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record IncomingMessage(String senderName, String content) {
    }

    private record OutgoingMessage(Long id, String senderName, String content, String createdAt, boolean system) {

        private static OutgoingMessage from(ChatMessage message) {
            return new OutgoingMessage(
                    message.getId(),
                    message.getSenderName(),
                    message.getContent(),
                    MESSAGE_TIME_FORMATTER.format(message.getCreatedAt()),
                    false
            );
        }

        private static OutgoingMessage error(String content) {
            return new OutgoingMessage(null, "系统提示", content, "", true);
        }
    }
}
