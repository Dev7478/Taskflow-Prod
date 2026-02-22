package com.jbs.tfv3.service.impl;

import com.jbs.tfv3.service.CommentService;
import com.jbs.tfv3.dto.CommentRequest;
import com.jbs.tfv3.dto.CommentResponse;
import com.jbs.tfv3.entity.Comment;
import com.jbs.tfv3.entity.Ticket;
import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.repository.CommentRepository;
import com.jbs.tfv3.repository.TicketRepository;
import com.jbs.tfv3.repository.UserDtlsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
    private TicketRepository ticketRepository;
	
	@Autowired
    private UserDtlsRepository userDtlsRepository;

    @Transactional
    public Comment createComment(Long ticketId, String authorEmail, CommentRequest req) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        UserDtls author = userDtlsRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorEmail));

        Comment c = new Comment();
        c.setComment(req.getComment());
        c.setTicket(ticket);
        c.setUserDtls(author);

        return commentRepository.save(c);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicket(Long ticketId) {
        // ensure ticket exists (optional)
        ticketRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Comment updateComment(Long commentId, String authorEmail, CommentRequest req) {
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        // check permission: admin or owner
        UserDtls author = userDtlsRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorEmail));

        boolean isAdmin = "ROLE_ADMIN".equals(author.getRole());
        boolean isOwner = existing.getUserDtls().getId().equals(author.getId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: you are not allowed to edit this comment");
        }

        if (req.getComment() != null && !req.getComment().isBlank()) {
            existing.setComment(req.getComment().trim());
        }

        return commentRepository.save(existing);
    }

    @Transactional
    public Comment deleteComment(Long commentId, String authorEmail) {
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        UserDtls author = userDtlsRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorEmail));

        boolean isAdmin = "ROLE_ADMIN".equals(author.getRole());
        boolean isOwner = existing.getUserDtls().getId().equals(author.getId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: you are not allowed to delete this comment");
        }

        commentRepository.delete(existing);
        return existing;
    }

    public CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getComment(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getUserDtls().getId(),
                c.getUserDtls().getEmail()
        );
    }
}
