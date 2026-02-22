package com.jbs.tfv3.controller;

import com.jbs.tfv3.dto.CommentRequest;
import com.jbs.tfv3.dto.CommentResponse;
import com.jbs.tfv3.entity.Comment;
import com.jbs.tfv3.service.impl.CommentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {
	private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

	@Autowired
    private CommentServiceImpl commentServiceImpl;

    // ---------------------------------------------------------------------
	/*
	POST localhost:8080/api/tickets/{ticketId}/comments
	success:
	
	Failure:
	
	*/
    @Operation(
        tags = "Comments",
        summary = "Create a new comment on a ticket",
        description = "Allows ADMIN or USER to add a comment to a specific ticket identified by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comment created successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket or user not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - only Admin or User can comment"),
        @ApiResponse(responseCode = "500", description = "Error creating comment")
    })
    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<CommentResponse>> createComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentRequest req) {

        logger.info("POST /tickets/{}/comments called", ticketId);
        String authorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Comment saved = commentServiceImpl.createComment(ticketId, authorEmail, req);
            com.jbs.tfv3.dto.ApiResponse<CommentResponse> resp = new com.jbs.tfv3.dto.ApiResponse<>(201, "Comment created successfully", commentServiceImpl.toResponse(saved));
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.jbs.tfv3.dto.ApiResponse<>(403, e.getMessage(), null));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error creating comment", null));
        }
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Get all comments for a ticket",
        description = "Retrieves a list of comments associated with a specific ticket. Accessible by any user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Error retrieving comments")
    })
    @GetMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long ticketId) {
        logger.info("GET /tickets/{}/comments", ticketId);
        try {
            List<CommentResponse> list = commentServiceImpl.getCommentsByTicket(ticketId);
            return ResponseEntity.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Comments retrieved successfully", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error retrieving comments", null));
        }
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Update a comment",
        description = "Allows ADMIN or the original comment author to update the content of an existing comment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - not allowed to update this comment"),
        @ApiResponse(responseCode = "500", description = "Error updating comment")
    })
    @PatchMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest req) {

        logger.info("PATCH /comments/{} called", commentId);
        String authorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Comment updated = commentServiceImpl.updateComment(commentId, authorEmail, req);
            return ResponseEntity.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Comment updated successfully", commentServiceImpl.toResponse(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.jbs.tfv3.dto.ApiResponse<>(403, e.getMessage(), null));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error updating comment", null));
        }
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Delete a comment",
        description = "Allows ADMIN or the original comment author to delete an existing comment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - not allowed to delete this comment"),
        @ApiResponse(responseCode = "500", description = "Error deleting comment")
    })
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<CommentResponse>> deleteComment(@PathVariable Long commentId) {
        logger.info("DELETE /comments/{} called", commentId);
        String authorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Comment deleted = commentServiceImpl.deleteComment(commentId, authorEmail);
            return ResponseEntity.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Comment deleted successfully", commentServiceImpl.toResponse(deleted)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.jbs.tfv3.dto.ApiResponse<>(403, e.getMessage(), null));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error deleting comment", null));
        }
    }
}
