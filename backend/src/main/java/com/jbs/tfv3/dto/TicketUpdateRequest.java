package com.jbs.tfv3.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateRequest {
	@Size(max = 150, message = "Subject must be less than 150 characters")
    private String subject;

    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @FutureOrPresent(message = "Due date must be in the future or today")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @FutureOrPresent(message = "Due date must be in the future or today")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
