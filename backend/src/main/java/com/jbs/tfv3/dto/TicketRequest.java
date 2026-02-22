package com.jbs.tfv3.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
	@NotBlank(message = "Subject cannot be empty")
    @Size(max = 150, message = "Subject must be less than 150 characters")
    private String subject;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @FutureOrPresent(message = "Due date must be in the future or today")
    @JsonFormat(pattern = "yyyy-MM-dd")  // ✅ ensures correct parsing
    private LocalDate dueDate;
}
