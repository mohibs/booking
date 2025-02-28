package com.example.booking.service;


import com.example.booking.entity.Booking;
import com.example.booking.entity.Cleaner;
import com.example.booking.model.CleanerDTO;
import com.example.booking.model.TimeslotDTO;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.CleanerRepository;
import com.example.booking.util.Util;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AvailabilityServiceTest {
	
	@InjectMocks
	private AvailabilityService availabilityService;
	
	@Mock
	private CleanerRepository cleanerRepo;
	
	@Mock
	private BookingRepository bookingRepo;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void testGetAvailableCleanerDtoList_withValidInput() {
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(10, 0);
		int duration = 2;
		
		List<Cleaner> cleaners = Arrays.asList(
				Cleaner.builder()
						.cleanerId(11)
						.cleanerName("John Doe")
						.build(),
				Cleaner.builder()
						.cleanerId(22)
						.cleanerName("Jane Doe")
						.build()
		);
		
		when(cleanerRepo.findAll()).thenReturn(cleaners);
		
		List<Booking> bookings = new ArrayList<>();
		when(bookingRepo.findByBookingDateAndCleanerId(any(), anyInt())).thenReturn(bookings);
		
		List<CleanerDTO> availableCleanerDtos = availabilityService.getAvailableCleanerDtos(date, startTime, duration);
		
		assertNotNull(availableCleanerDtos);
		assertEquals(2, availableCleanerDtos.size());
	}
	
	@Test
	void testValidateCleanerWorkingDay_withFriday_throwsValidationException() {
		LocalDate friday = LocalDate.of(2024, 9, 13); // This is a Friday
		
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			Util.validateWorkingDay(friday);
		});
		
		assertEquals("No cleaners working on Friday", exception.getMessage());
	}
	
	@Test
	void testValidateCleanerWorkingHours_outsideWorkingHours_throwsValidationException() {
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(7, 0); // Before working hours
		int duration = 2;
		
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			Util.validateWorkingHours(date, startTime, duration);
		});
		
		assertEquals("Provided time is outside working hours", exception.getMessage());
	}
	
	@Test
	void testGetAvailableSlots_withBookings() {
		LocalDate date = LocalDate.now();
		
		List<Booking> bookings = new ArrayList<>();
		bookings.add(Booking.builder()
				.bookingId(33)
				.bookingDate(date)
				.bookingStartTime(LocalTime.of(9, 0))
				.bookingDuration(2)
				.build());
		
		bookings.add(Booking.builder()
				.bookingId(44)
				.bookingDate(date)
				.bookingStartTime(LocalTime.of(12, 0))
				.bookingDuration(2)
				.build());
		
		List<TimeslotDTO> availableSlots = availabilityService.getAvailableSlots(date, bookings);
		assertNotNull(availableSlots);
		assertEquals(2, availableSlots.size()); // Expected 2 available slots
	}
	
	@Test
	void testHasAvailableSlot_withMatchingSlot() {
		List<TimeslotDTO> slots = new ArrayList<>();
		slots.add(new TimeslotDTO(LocalDateTime.of(2024, 9, 5, 10, 0),
				LocalDateTime.of(2024, 9, 5, 12, 0)));
		
		LocalDateTime bookingStart = LocalDateTime.of(2024, 9, 5, 10, 0);
		LocalDateTime bookingEnd = LocalDateTime.of(2024, 9, 5, 12, 0);
		boolean result = availabilityService.isSlotAvailable(slots, bookingStart, bookingEnd);
		assertTrue(result);
	}
}