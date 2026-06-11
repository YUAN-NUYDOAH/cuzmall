package cn.zjicm.transaction.repository;

import cn.zjicm.transaction.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySubstitutePostIdOrderByCreatedAtAsc(Long substitutePostId);
}
