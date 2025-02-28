package com.example.booking.controller;

import com.example.booking.model.BookingDTO;
import com.example.booking.service.BookingService;
import com.example.booking.validation.CreateValidationGroup;
import com.example.booking.validation.UpdateValidationGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
	
	private final BookingService bookingService;
	
	@PostMapping(value = "/create")
	public ResponseEntity<BookingDTO> createBooking(@Validated(CreateValidationGroup.class) @RequestBody BookingDTO bookingDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(bookingDTO));
	}
	
	@PutMapping(value = "/update/{bookingId}")
	public ResponseEntity<BookingDTO> updateBooking(@PathVariable Integer bookingId,
													@Validated(UpdateValidationGroup.class) @RequestBody BookingDTO bookingDTO) {
		return ResponseEntity.ok(bookingService.updateBooking(bookingId, bookingDTO));
	}
}
