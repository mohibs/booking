package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.Cleaner;
import com.example.booking.entity.Vehicle;
import com.example.booking.model.BookingDTO;
import com.example.booking.model.CleanerDTO;
import com.example.booking.repo.BookingRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookingServiceTest {
	
	@Mock
	private BookingRepository bookingRepo;
	@Mock
	private AvailabilityService availabilityService;
	@InjectMocks
	private BookingService bookingService;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void testCreateBooking_Success() {
		// Given
		BookingDTO bookingDto = createSampleBookingDto();
		Cleaner cleaner = createSampleCleaner();
		Booking booking = createSampleBooking();
		
		// Mocking the service responses
		when(availabilityService.getAvailableCleaners(any(), any(), anyInt()))
				.thenReturn(Collections.singletonList(cleaner));
		when(bookingRepo.save(any(Booking.class))).thenReturn(booking);
		CleanerDTO cleanerDTO = CleanerDTO.builder()
				.cleanerId(cleaner.getCleanerId())
				.cleanerName(cleaner.getCleanerName())
				.build();
		bookingDto.setCleaners(Collections.singletonList(cleanerDTO));
		// When
		BookingDTO createdBooking = bookingService.createBooking(bookingDto);
		
		// Then
		assertNotNull(createdBooking);
		assertEquals(bookingDto.getBookingDate(), createdBooking.getBookingDate());
		verify(availabilityService, times(1)).getAvailableCleaners(any(), any(), anyInt());
		verify(bookingRepo, times(1)).save(any(Booking.class));
	}
	
	@Test
	void testCreateBooking_NoAvailableCleaners() {
		// Given
		BookingDTO bookingDto = createSampleBookingDto();
		// Mocking no available cleaners
		when(availabilityService.getAvailableCleaners(any(), any(), anyInt()))
				.thenReturn(Collections.emptyList());
		// Then
		ValidationException exception = assertThrows(ValidationException.class,
				() -> bookingService.createBooking(bookingDto));
		
		assertEquals("No cleaners available for requested time", exception.getMessage());
		verify(availabilityService, times(1)).getAvailableCleaners(any(), any(), anyInt());
	}
	
	@Test
	void testUpdateBooking_BookingNotFound() {
		BookingDTO bookingDto = createSampleBookingDto();
		// Mocking no existing booking
		when(bookingRepo.findByBookingDateAndCleanerId(any(LocalDate.class), anyInt()))
				.thenReturn(Collections.emptyList()); // Return an empty list for the mock
		ValidationException exception = assertThrows(ValidationException.class,
				() -> bookingService.updateBooking(109, bookingDto));
		assertEquals("No existing booking against id 109", exception.getMessage());
	}
	
	
	private BookingDTO createSampleBookingDto() {
		return BookingDTO.builder()
				.bookingDate(LocalDate.now().plusDays(1))
				.bookingStartTime(LocalTime.of(10, 0))
				.bookingDuration(2)
				.cleaners(new ArrayList<>())
				.cleanerCount(1)
				.build();
	}
	
	private Cleaner createSampleCleaner() {
		Cleaner cleaner = new Cleaner();
		cleaner.setCleanerId(55);
		cleaner.setCleanerName("John Doe");
		
		// Create a Vehicle and associate it with the Cleaner
		Vehicle vehicle = new Vehicle();
		vehicle.setVehicleId(10);
		vehicle.setVehicleName("Van 1");
		cleaner.setVehicle(vehicle);
		return cleaner;
	}
	
	
	private Booking createSampleBooking() {
		Booking booking = new Booking();
		booking.setBookingId(66);
		booking.setBookingDate(LocalDate.now().plusDays(1));
		booking.setBookingStartTime(LocalTime.of(10, 0));
		booking.setBookingDuration(2);
		return booking;
	}
}