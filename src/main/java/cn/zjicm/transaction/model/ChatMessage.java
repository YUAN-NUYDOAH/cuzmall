package cn.zjicm.transaction.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private Long id;
    private Long substitutePostId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {
    }

    public ChatMessage(Long id, Long substitutePostId, String senderName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.substitutePostId = substitutePostId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubstitutePostId() {
        return substitutePostId;
    }

    public void setSubstitutePostId(Long substitutePostId) {
        this.substitutePostId = substitutePostId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
