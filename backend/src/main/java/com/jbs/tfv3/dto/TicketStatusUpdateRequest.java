package com.jbs.tfv3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketStatusUpdateRequest {
	@NotNull(message = "Ticket ID cannot be null")
    private Long ticketId;

    @NotBlank(message = "Status cannot be blank")
    private String status;
}
