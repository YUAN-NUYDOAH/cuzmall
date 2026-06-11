package cn.zjicm.transaction.repository;

import cn.zjicm.transaction.model.SubstitutePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstitutePostRepository extends JpaRepository<SubstitutePost, Long> {

    @Query("""
            SELECT p FROM SubstitutePost p
            WHERE :keyword = ''
               OR LOWER(p.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.classTime) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.createdAt DESC
            """)
    List<SubstitutePost> search(@Param("keyword") String keyword);
}
