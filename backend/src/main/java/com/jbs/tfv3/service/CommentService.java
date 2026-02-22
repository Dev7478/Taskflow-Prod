package com.jbs.tfv3.service;

import java.util.List;

import com.jbs.tfv3.dto.CommentRequest;
import com.jbs.tfv3.dto.CommentResponse;
import com.jbs.tfv3.entity.Comment;

public interface CommentService {
	Comment createComment(Long ticketId, String authorEmail, CommentRequest req);
	List<CommentResponse> getCommentsByTicket(Long ticketId);
	Comment updateComment(Long commentId, String authorEmail, CommentRequest req);
	Comment deleteComment(Long commentId, String authorEmail);
}
