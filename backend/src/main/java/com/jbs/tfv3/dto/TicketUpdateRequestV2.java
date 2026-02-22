package com.jbs.tfv3.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateRequestV2 {
	@NotNull(message = "Ticket ID cannot be null")
    private Long ticketId;
	
	@Size(max = 150, message = "Subject must be less than 150 characters")
    private String subject;

    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
//    @FutureOrPresent(message = "Due date must be in the future or today")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @FutureOrPresent(message = "Due date must be in the future or today")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
