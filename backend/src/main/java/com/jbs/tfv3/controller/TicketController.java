package com.jbs.tfv3.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.jbs.tfv3.dto.TicketRequest;
import com.jbs.tfv3.dto.TicketStatusUpdateRequest;
import com.jbs.tfv3.dto.TicketUpdateRequest;
import com.jbs.tfv3.dto.TicketUpdateRequestV2;
import com.jbs.tfv3.entity.Ticket;
import com.jbs.tfv3.service.UserDtlsService;
import com.jbs.tfv3.service.impl.TicketServiceImpl;
import com.jbs.tfv3.service.impl.UserServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

	@Autowired
	private TicketServiceImpl ticketServiceImpl;

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private UserDtlsService userDtlsService;

	// ---------------------------------------------------------------------
	@Operation(tags = "Tickets", summary = "Create a new ticket", description = "Creates a new support ticket for the logged-in user or by an Admin for another user.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - only Admin or self can create"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "500", description = "Error creating ticket") })
	@PostMapping("/user/{user_email}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Ticket>> createTicket(@PathVariable String user_email,
			@Valid @RequestBody TicketRequest ticketRequest) {

		logger.info("/user/{} encountered >> createTicket(@RequestBody TicketRequest ticketRequest)", user_email);
		logger.info("Authorities in context: {}",
				SecurityContextHolder.getContext().getAuthentication().getAuthorities());

		Long userId = userDtlsService.getUserIdFroEmail(user_email);
		try {
			Ticket createdTicket = ticketServiceImpl.createTicket(userId, ticketRequest);
			if (createdTicket == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Unable to create ticket", null));
			} else {
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(201, "Ticket successfully created", createdTicket));
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error creating ticket: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
		} catch (Exception e) {
			logger.error("Unexpected error creating ticket", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error creating ticket", null));
		}
	}

	// ---------------------------------------------------------------------
	@Operation(tags = "Tickets", summary = "List tickets for a user", description = "Retrieves all tickets associated with a given user. Only the user or ADMIN can view.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - only Admin or self can view"),
			@ApiResponse(responseCode = "404", description = "No tickets found"),
			@ApiResponse(responseCode = "500", description = "Error retrieving tickets") })
	@GetMapping("/user/{user_email}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<List<Ticket>>> getTicketsByUser(
			@PathVariable String user_email) {

		logger.info("/user/{} encountered >> getTicketsByUser()", user_email);

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null || !authentication.isAuthenticated()) {
				logger.warn("Unauthorized access attempt detected for user email {}", user_email);
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(403, "Access denied", null));
			}

			String loggedInEmail = authentication.getName();
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			logger.info("Logged in user: {}, Authorities: {}", loggedInEmail, authentication.getAuthorities());

			Long userId = userDtlsService.getUserIdFroEmail(user_email);
			if (!isAdmin) {
				Long loggedInUserId = userServiceImpl.getUserByEmail(loggedInEmail).getId();
				if (!loggedInUserId.equals(userId)) {
					logger.warn("Forbidden: User {} tried to access tickets of {}", loggedInUserId, user_email);
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.jbs.tfv3.dto.ApiResponse<>(403,
							"Forbidden: You can only view your own tickets", null));
				}
			}

			List<Ticket> tickets = ticketServiceImpl.getTicketsByUser(userId);
			if (tickets == null || tickets.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(404, "No tickets found for this user", null));
			}

			return ResponseEntity
					.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Tickets retrieved successfully", tickets));

		} catch (Exception e) {
			logger.error("Error retrieving tickets for user email {}", user_email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error retrieving tickets", null));
		}
	}

	// ---------------------------------------------------------------------
	@Operation(tags = "Tickets", summary = "Get a specific ticket", description = "Fetches a single ticket by its ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket not found") })
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Ticket>> getTicket(@PathVariable Long id) {
		logger.info("GET /api/tickets/{}", id);
		return ticketServiceImpl.getTicketById(id)
				.map(ticket -> ResponseEntity
						.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Ticket retrieved successfully", ticket)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(404, "Ticket not found", null)));
	}

	// ---------------------------------------------------------------------Vr. 02
	@Operation(tags = "Tickets", summary = "Update ticket status", description = "Updates the status of a ticket. Only ADMIN users can perform this action.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket status updated successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
			@ApiResponse(responseCode = "400", description = "Invalid request body") })
	@PatchMapping("/status")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Ticket>> updateTicketStatus(
			@Valid @RequestBody TicketStatusUpdateRequest statusRequest) {

		logger.info("PATCH /api/tickets/status called with: {}", statusRequest);

		try {
			Ticket updated = ticketServiceImpl.updateTicketStatus(statusRequest.getTicketId(),
					statusRequest.getStatus());

			return ResponseEntity
					.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Ticket status updated successfully", updated));

		} catch (IllegalArgumentException ex) {
			logger.error("Ticket not found: {}", ex.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(404, ex.getMessage(), null));
		} catch (Exception ex) {
			logger.error("Error updating ticket status", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error updating ticket status", null));
		}
	}

	// --------------------------------------------------------------------- Vr. 02
	/*
	 * PATCH http://localhost:8080/api/tickets/details
	*/
	@Operation(tags = "Tickets", summary = "Update ticket details", description = "Allows ADMIN or USER to update subject, description, startDate, endDate, or dueDate of a ticket.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - only Admin or ticket owner can update"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "500", description = "Error updating ticket") })
	@PatchMapping("/details")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Ticket>> updateTicketDetails(
			@RequestBody TicketUpdateRequestV2 ticketUpdateRequestV2) {

		logger.info("PATCH /api/tickets/{} called for partial update", ticketUpdateRequestV2.getTicketId());
		logger.info("Authorities in context: {}",
				SecurityContextHolder.getContext().getAuthentication().getAuthorities());

		try {
			// Get currently logged-in user
			String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			// Verify ownership if not admin
			if (!isAdmin) {
				Long loggedInUserId = userServiceImpl.getUserByEmail(loggedInEmail).getId();
				Ticket ticket = ticketServiceImpl.getTicketById(ticketUpdateRequestV2.getTicketId()).get();

				if (ticket == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(new com.jbs.tfv3.dto.ApiResponse<>(404, "Ticket not found", null));
				}

				if (!ticket.getUserDtls().getId().equals(loggedInUserId)) {
					logger.warn("Forbidden: User {} attempted to update ticket {}", loggedInUserId,
							ticketUpdateRequestV2.getTicketId());
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new com.jbs.tfv3.dto.ApiResponse<>(403,
							"Forbidden: You can only update your own tickets", null));
				}
			}

			Ticket updatedTicket = ticketServiceImpl.updateTicketDetails(ticketUpdateRequestV2);

			if (updatedTicket == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Unable to update ticket", null));
			}

			return ResponseEntity
					.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Ticket updated successfully", updatedTicket));

		} catch (IllegalArgumentException e) {
			logger.error("Ticket not found: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
		} catch (Exception e) {
			logger.error("Unexpected error updating ticket", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error updating ticket", null));
		}
	}

	// ---------------------------------------------------------------------
	/*
	 * DELETE: http://localhost:8080/api/tickets/3 Success: { "status": 200,
	 * "message": "Ticket deleted successfully", "data": { "id": 3, "subject":
	 * "Password Update Fails", "description":
	 * "Password updates but DB not updated", "dueDate": "2025-10-25" } }
	 * 
	 */
	@Operation(tags = "Tickets", summary = "Delete a ticket by ID", description = "Deletes a ticket permanently. Only ADMIN users are authorized to perform this action.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket deleted successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - only Admin can delete"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "500", description = "Error deleting ticket") })
	@DeleteMapping("/{ticketId}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Ticket>> deleteTicket(@PathVariable Long ticketId) {

		logger.info("DELETE /api/tickets/{} called by ADMIN", ticketId);

		try {
			Ticket deletedTicket = ticketServiceImpl.deleteTicket(ticketId);

			if (deletedTicket == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new com.jbs.tfv3.dto.ApiResponse<>(404, "Ticket not found", null));
			}

			return ResponseEntity
					.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "Ticket deleted successfully", deletedTicket));

		} catch (IllegalArgumentException e) {
			logger.error("Ticket not found with ID {}: {}", ticketId, e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(404, e.getMessage(), null));
		} catch (Exception e) {
			logger.error("Unexpected error while deleting ticket {}", ticketId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(500, "Error deleting ticket", null));
		}
	}

}
