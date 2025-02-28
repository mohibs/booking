package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bookingId;
	private LocalDate bookingDate;
	private LocalTime bookingStartTime;
	private Integer bookingDuration;
	
	@ManyToMany
	@JoinTable (
			name = "cleanerBookings",
			joinColumns = @JoinColumn(name = "bookingId", referencedColumnName = "bookingId"),
			inverseJoinColumns = @JoinColumn(name = "cleanerId", referencedColumnName = "cleanerId")
	)
	private List<Cleaner> cleaners;
}
