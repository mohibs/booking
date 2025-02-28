package com.example.booking.model;

import com.example.booking.validation.CreateValidationGroup;
import com.example.booking.validation.UpdateValidationGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
	
	private List<CleanerDTO> cleaners;
	
	@JsonIgnore
	private Integer bookingId;
	
	@NotNull(message = "Booking date is missing", groups={CreateValidationGroup.class, UpdateValidationGroup.class})
	private LocalDate bookingDate;
	
	@NotNull(message = "Booking start time is missing", groups={CreateValidationGroup.class, UpdateValidationGroup.class})
	private LocalTime bookingStartTime;
	
	//@Min(value = 2, message = "Minimum booking duration is 2 hours.", groups={CreateValidationGroup.class, UpdateValidationGroup.class})
	//@Max(value = 4, message = "Maximum booking duration is 4 hours.", groups={CreateValidationGroup.class, UpdateValidationGroup.class})
	private int bookingDuration;
	@AssertTrue(message = "Booking duration must be either 2 or 4 hours.", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
	public boolean isValidBookingDuration() {
		return bookingDuration == 2 || bookingDuration == 4;
	}
	
	@NotNull(message = "Cleaner count is missing.", groups = CreateValidationGroup.class)
	@Min(value = 1, message = "Minimum 1 cleaner is required.", groups={CreateValidationGroup.class})
	@Max(value = 3, message = "Maximum 3 cleaners are allowed.", groups={CreateValidationGroup.class})
	private int cleanerCount;
}
