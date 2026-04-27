package cn.zjicm.transaction.repository;

import cn.zjicm.transaction.model.ChatMessage;
import cn.zjicm.transaction.model.SubstitutePost;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SubstitutePostRepository {

    private final AtomicLong postIdSequence = new AtomicLong(2000);
    private final AtomicLong messageIdSequence = new AtomicLong(5000);
    private final List<SubstitutePost> posts = new ArrayList<>();
    private final List<ChatMessage> messages = new ArrayList<>();

    public SubstitutePostRepository() {
        seedPosts();
    }

    public synchronized List<SubstitutePost> search(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        return posts.stream()
                .filter(post -> normalizedKeyword.isEmpty()
                        || post.getCourseName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || post.getClassTime().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || post.getLocation().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || (post.getDescription() != null
                        && post.getDescription().toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .sorted(Comparator.comparing(SubstitutePost::getCreatedAt).reversed())
                .toList();
    }

    public synchronized List<SubstitutePost> findLatest(int limit) {
        return posts.stream()
                .sorted(Comparator.comparing(SubstitutePost::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    public synchronized Optional<SubstitutePost> findById(Long id) {
        return posts.stream()
                .filter(post -> post.getId().equals(id))
                .findFirst();
    }

    public synchronized SubstitutePost save(SubstitutePost post) {
        post.setId(postIdSequence.incrementAndGet());
        post.setCreatedAt(LocalDateTime.now());
        posts.add(post);
        return post;
    }

    public synchronized List<ChatMessage> findMessagesByPostId(Long postId) {
        return messages.stream()
                .filter(message -> message.getSubstitutePostId().equals(postId))
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .toList();
    }

    public synchronized ChatMessage addMessage(Long postId, String senderName, String content) {
        ChatMessage message = new ChatMessage(
                messageIdSequence.incrementAndGet(),
                postId,
                senderName.trim(),
                content.trim(),
                LocalDateTime.now()
        );
        messages.add(message);
        return message;
    }

    private void seedPosts() {
        SubstitutePost morningClass = new SubstitutePost(
                postIdSequence.incrementAndGet(),
                "媒介经营与管理",
                "周三 08:30-10:00",
                "钱塘校区教学楼 B204",
                new BigDecimal("35"),
                "老师会点名，需要能按时到教室听完整节课。",
                "刘同学",
                LocalDateTime.now().minusHours(4)
        );
        posts.add(morningClass);

        SubstitutePost eveningClass = new SubstitutePost(
                postIdSequence.incrementAndGet(),
                "大学英语视听说",
                "周五 18:30-20:00",
                "桐乡校区 3 号教学楼 301",
                new BigDecimal("45"),
                "需要帮忙签到并记录课堂作业。",
                "周同学",
                LocalDateTime.now().minusDays(1)
        );
        posts.add(eveningClass);

        messages.add(new ChatMessage(
                messageIdSequence.incrementAndGet(),
                morningClass.getId(),
                "王同学",
                "我周三上午有空，可以确认一下座位和老师要求吗？",
                LocalDateTime.now().minusHours(3)
        ));
    }
}
