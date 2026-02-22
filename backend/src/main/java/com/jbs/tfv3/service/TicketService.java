package com.jbs.tfv3.service;

import java.util.List;
import java.util.Optional;

import com.jbs.tfv3.dto.TicketRequest;
import com.jbs.tfv3.dto.TicketUpdateRequest;
import com.jbs.tfv3.dto.TicketUpdateRequestV2;
import com.jbs.tfv3.entity.Ticket;

public interface TicketService {
	Ticket createTicket(Long userId, TicketRequest TicketRequest);
	List<Ticket> getTicketsByUser(Long userId);
	Optional<Ticket> getTicketById(Long ticketId);
	Ticket updateTicketStatus(Long ticketId, String newStatus);
	Ticket updateTicketDetails(TicketUpdateRequestV2 ticketUpdateRequestV2);
	Ticket deleteTicket(Long ticketId);
}
