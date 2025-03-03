package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.Cleaner;
import com.example.booking.model.CleanerDTO;
import com.example.booking.model.TimeslotDTO;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.CleanerRepository;
import com.example.booking.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.booking.util.Util.validateWorkingDay;
import static com.example.booking.util.Util.validateWorkingHours;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvailabilityService {
	
	private static final Integer BREAK_MINUTES = 30;
	private static final Duration BREAK_DURATION = Duration.ofMinutes(BREAK_MINUTES);
	private final BookingRepository bookingRepo;
	private final CleanerRepository cleanerRepo;
	
	public List<CleanerDTO> getAvailableCleanerDtos(LocalDate date, LocalTime startTime, Integer duration) { //skipped
		validateWorkingDay(date);
		validateWorkingHours(date, startTime, duration);
		List<Cleaner> cleaners = cleanerRepo.findAll();
		List<CleanerDTO> availableCleanerDtos = new ArrayList<>();
		for (Cleaner cleaner : cleaners) {
			List<Booking> bookings = bookingRepo.findByBookingDateAndCleanerId(date, cleaner.getCleanerId());
			List<TimeslotDTO> cleanerBookingTimeSlots = getTimeslotsAgainstBookings(bookings);
			List<TimeslotDTO> slotsAvailable = getAvailableSlots(date, bookings);
			if (!slotsAvailable.isEmpty()) {
				CleanerDTO availableCleaner = CleanerDTO.builder()
						.cleanerId(cleaner.getCleanerId())
						.cleanerName(cleaner.getCleanerName())
						.slotsAvailable(slotsAvailable)
						.bookings(cleanerBookingTimeSlots)
						.build();
				availableCleanerDtos.add(availableCleaner);
			}
		}
		
		if (startTime != null && duration != null) {
			LocalDateTime bookingStart = LocalDateTime.of(date, startTime);
			LocalDateTime bookingEnd = bookingStart.plusHours(duration);
			availableCleanerDtos = filterCleanersByTimeSlot(availableCleanerDtos, bookingStart, bookingEnd);
		}
		return availableCleanerDtos;
	}
	
	private List<CleanerDTO> filterCleanersByTimeSlot(List<CleanerDTO> availableCleanerDtos, LocalDateTime bookingStart, LocalDateTime bookingEnd) {
		return availableCleanerDtos.stream().filter(cleaner -> isSlotAvailable(cleaner.getSlotsAvailable(), bookingStart, bookingEnd)).toList();
	}
	
	private List<TimeslotDTO> getTimeslotsAgainstBookings(List<Booking> bookings) {
		List<TimeslotDTO> timeSlots = new ArrayList<>();
		for(Booking booking : bookings) {
			LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getBookingStartTime());
			LocalDateTime bookingEnd = bookingStart.plusHours(booking.getBookingDuration());
			timeSlots.add(TimeslotDTO.builder().from(bookingStart).to(bookingEnd).build());
		}
		return timeSlots;
	}
	
	public List<Cleaner> getAvailableCleaners(LocalDate date, LocalTime time, Integer duration) {
		validateWorkingDay(date);
		validateWorkingHours(date, time, duration);
		
		return cleanerRepo.findAll().stream()
				.filter(cleaner -> isCleanerAvailable(cleaner, date, time, duration))
				.collect(Collectors.toList());
	}
	
	private boolean isCleanerAvailable(Cleaner cleaner, LocalDate date, LocalTime startTime, Integer duration) {
		if (cleaner.getBookings() == null || cleaner.getBookings().isEmpty())
			return true;
		
		LocalDateTime startTimeRequested = LocalDateTime.of(date, startTime);
		LocalDateTime endTimeRequested = startTimeRequested.plusHours(duration);
		
		return cleaner.getBookings().stream().allMatch(booking -> {
			LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getBookingStartTime());
			LocalDateTime bookingEnd = bookingStart.plusHours(booking.getBookingDuration());
			boolean beforeCurrentBooking = bookingEnd.plusMinutes(BREAK_MINUTES).isBefore(startTimeRequested);
			boolean afterCurrentBooking = bookingStart.isAfter(endTimeRequested.plusMinutes(BREAK_MINUTES));
			return beforeCurrentBooking || afterCurrentBooking;
		});
	}
	
	public List<TimeslotDTO> getAvailableSlots(LocalDate workDay, List<Booking> bookings) {
		List<TimeslotDTO> availableSlots = new ArrayList<>();
		
		// Define the start and end of the working hours for the day
		LocalDateTime shiftStart = LocalDateTime.of(workDay, Util.cleanerShiftStart);
		LocalDateTime shiftEnd = LocalDateTime.of(workDay, Util.cleanerShiftEnd);
		
		// Sort bookings by start time
		bookings.sort(Comparator.comparing(Booking::getBookingStartTime));
		
		LocalDateTime currentSlotStart = shiftStart;
		
		for (Booking booking : bookings) {
			
			LocalDateTime bookingStart = LocalDateTime.of(workDay, booking.getBookingStartTime());
			LocalDateTime bookingEnd = bookingStart.plusHours(booking.getBookingDuration());
			
			// Check if there is an available slot before the current booking
			if (currentSlotStart.isBefore(bookingStart.minus(BREAK_DURATION))) {
				availableSlots.add(new TimeslotDTO(currentSlotStart, bookingStart.minus(BREAK_DURATION)));
			}
			
			// After the booking ends, the next available time is after a 30-minute break
			currentSlotStart = bookingEnd.plus(BREAK_DURATION);
		}
		
		// Check if there is any free slot after the last booking until the end of the shift
		if (currentSlotStart.isBefore(shiftEnd)) {
			availableSlots.add(new TimeslotDTO(currentSlotStart, shiftEnd));
		}
		return availableSlots;
	}
	
	public boolean isCleanerAvailableForUpdate(Cleaner cleaner, LocalDate date, LocalTime startTime, Integer duration, Booking currentBooking) {
		List<Booking> bookings = bookingRepo.findByBookingDateAndCleanerId(date, cleaner.getCleanerId());
		
		if (bookings == null) {
			bookings = new ArrayList<>();
		}
		
		// Exclude the current booking
		bookings = bookings.stream()
				.filter(booking -> !booking.getBookingId().equals(currentBooking.getBookingId()))
				.collect(Collectors.toList());
		
		List<TimeslotDTO> availableSlots = getAvailableSlots(date, bookings);
		if (availableSlots.isEmpty()) {
			return false;
		}
		LocalDateTime bookingStart = LocalDateTime.of(date, startTime);
		LocalDateTime bookingEnd = bookingStart.plusHours(duration);
		return isSlotAvailable(availableSlots, bookingStart, bookingEnd);
	}
	
	public boolean isSlotAvailable(List<TimeslotDTO> availableSlots, LocalDateTime bookingStartTime, LocalDateTime bookingEndTime) {
		for (TimeslotDTO slot : availableSlots) {
			boolean beforeSlotEnds = bookingEndTime.isBefore(slot.getTo()) || bookingEndTime.equals(slot.getTo());
			boolean afterSlotBegins = bookingStartTime.isAfter(slot.getFrom()) || bookingStartTime.equals(slot.getFrom());
			if (afterSlotBegins && beforeSlotEnds) {
				return true;
			}
		}
		return false;
	}
}
