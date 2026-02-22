package com.jbs.tfv3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jbs.tfv3.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
