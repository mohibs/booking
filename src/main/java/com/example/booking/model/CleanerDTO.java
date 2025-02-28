package com.example.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CleanerDTO {
	private Integer cleanerId;
	private String cleanerName;
	private List<TimeslotDTO> slotsAvailable;
	@JsonIgnore
	private List<TimeslotDTO> bookings;
}
