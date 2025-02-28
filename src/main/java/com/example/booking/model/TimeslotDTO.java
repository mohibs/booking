package com.example.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TimeslotDTO {
	private LocalDateTime from;
	private LocalDateTime to;
}
