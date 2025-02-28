package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Cleaner {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer cleanerId;
	private String cleanerName;
	
	@ManyToMany(mappedBy = "cleaners")
	private List<Booking> bookings = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name = "vehicleId")
	private Vehicle vehicle;
}