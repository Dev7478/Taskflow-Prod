package com.jbs.tfv3.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.dto.TicketRequest;
import com.jbs.tfv3.dto.TicketUpdateRequest;
import com.jbs.tfv3.dto.TicketUpdateRequestV2;
import com.jbs.tfv3.entity.Ticket;
import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.repository.TicketRepository;
import com.jbs.tfv3.repository.UserDtlsRepository;
import com.jbs.tfv3.service.TicketService;

import jakarta.transaction.Transactional;

@Service
public class TicketServiceImpl implements TicketService{
	
	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private UserDtlsRepository userRepository;
	
	@Override
	@Transactional
	public Ticket createTicket(Long userId, TicketRequest ticketRequest) {
	    UserDtls user = userRepository.findById(userId)
	            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

	    Ticket ticket = new Ticket();
	    ticket.setSubject(ticketRequest.getSubject());
	    ticket.setDescription(ticketRequest.getDescription());
	    ticket.setDueDate(ticketRequest.getDueDate());
	    ticket.setStartDate(LocalDate.now());
	    ticket.setEndDate(LocalDate.of(2000, 1, 1));
	    ticket.setUserDtls(user);

	    return ticketRepository.save(ticket);
	}

    public List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserDtlsId(userId);
    }

    public Optional<Ticket> getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    @Transactional
    public Ticket updateTicketStatus(Long ticketId, String newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        ticket.setStatus(Enum.valueOf(com.jbs.tfv3.entity.TicketStatus.class, newStatus));
        return ticketRepository.save(ticket);
    }

	@Override
	public Ticket updateTicketDetails(TicketUpdateRequestV2 ticketUpdateRequestV2) {
		// 1️- Fetch existing ticket
		Long ticketId = ticketUpdateRequestV2.getTicketId();
	    Ticket existingTicket = ticketRepository.findById(ticketId)
	            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

	    // 2️- Apply partial updates safely
	    if (ticketUpdateRequestV2.getSubject() != null && !ticketUpdateRequestV2.getSubject().isBlank()) {
	        existingTicket.setSubject(ticketUpdateRequestV2.getSubject().trim());
	    }

	    if (ticketUpdateRequestV2.getDescription() != null && !ticketUpdateRequestV2.getDescription().isBlank()) {
	        existingTicket.setDescription(ticketUpdateRequestV2.getDescription().trim());
	    }

	    if (ticketUpdateRequestV2.getStartDate() != null) {
	        existingTicket.setStartDate(ticketUpdateRequestV2.getStartDate());
	    }

	    if (ticketUpdateRequestV2.getEndDate() != null) {
	        existingTicket.setEndDate(ticketUpdateRequestV2.getEndDate());
	    }

	    if (ticketUpdateRequestV2.getDueDate() != null) {
	        existingTicket.setDueDate(ticketUpdateRequestV2.getDueDate());
	    }

	    // 3️- Validation logic for date consistency
	    LocalDate startDate = existingTicket.getStartDate();
	    LocalDate endDate = existingTicket.getEndDate();
	    LocalDate dueDate = existingTicket.getDueDate();

//	    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
//	        throw new IllegalArgumentException("End date cannot be before start date");
//	    }

	    if (startDate != null && dueDate != null && dueDate.isBefore(startDate)) {
	        throw new IllegalArgumentException("Due date cannot be before start date");
	    }

	    // 4️- Optionally update the last modified time
	    existingTicket.setUpdatedAt(LocalDateTime.now());

	    // 5️- Save and return updated ticket
	    return ticketRepository.save(existingTicket);
	}
	
	@Override
	public Ticket deleteTicket(Long ticketId) {
	    // 1- Find ticket
	    Ticket ticket = ticketRepository.findById(ticketId)
	            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

	    // 2- Delete ticket
	    ticketRepository.delete(ticket);

	    // 3- Optionally return the deleted entity (useful for logging/response)
	    return ticket;
	}


}
