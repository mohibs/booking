package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.Cleaner;
import com.example.booking.model.BookingDTO;
import com.example.booking.model.CleanerDTO;
import com.example.booking.repo.BookingRepository;
import com.example.booking.util.Util;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository bookingRepo;
	private final AvailabilityService availabilityService;
	
	public BookingDTO createBooking(BookingDTO bookingDTO) {
		log.info("Creating new booking {}", bookingDTO);
		LocalDate bookingDate = bookingDTO.getBookingDate();
		LocalTime bookingStartTime = bookingDTO.getBookingStartTime();
		int bookingDuration = bookingDTO.getBookingDuration();
		
		Util.validateWorkingDay(bookingDate);
		Util.validateWorkingHours(bookingDate, bookingStartTime, bookingDuration);
		
		List<Cleaner> availableCleaners = availabilityService.getAvailableCleaners(bookingDate, bookingStartTime, bookingDuration);
		if (availableCleaners == null || availableCleaners.isEmpty()) {
			throw new ValidationException("No cleaners available for requested time");
		}
		availableCleaners = getCleanersByVehicleAndCount(availableCleaners, bookingDTO.getCleanerCount()).stream()
				.limit(bookingDTO.getCleanerCount()).toList();
		
		if (bookingDTO.getCleanerCount() != availableCleaners.size()) {
			throw new ValidationException("Not enough cleaners available for requested time");
		}
		
		Booking createdBooking = bookingRepo.save(Booking.builder()
				.bookingDate(bookingDate)
				.bookingStartTime(bookingStartTime)
				.bookingDuration(bookingDuration)
				.cleaners(availableCleaners)
				.build());
		
		return buildBookingResponse(createdBooking);
	}
	
	public BookingDTO updateBooking(Integer id, BookingDTO bookingDTO) {
		log.info("Updating booking id {} to {}", id, bookingDTO);
		LocalDate bookingDate = bookingDTO.getBookingDate();
		LocalTime bookingStartTime = bookingDTO.getBookingStartTime();
		int bookingDuration = bookingDTO.getBookingDuration();
		
		Util.validateWorkingDay(bookingDate);
		Util.validateWorkingHours(bookingDate, bookingStartTime, bookingDuration);
		
		Booking booking = bookingRepo.findById(id).orElseThrow(() -> new ValidationException("No existing booking against id " + id));
		
		List<Cleaner> bookingCleaners = booking.getCleaners(); // cleaners of current booking
		
		// checking if current cleaners are available for new time slots
		List<Cleaner> filteredCleaners = bookingCleaners.stream()
				.filter(cleaner -> availabilityService.isCleanerAvailableForUpdate(cleaner, bookingDate, bookingStartTime, bookingDuration, booking))
				.collect(Collectors.toList());
		
		if (bookingCleaners.size() != filteredCleaners.size()) {
			List<Cleaner> availableCleaners = availabilityService.getAvailableCleaners(bookingDate, bookingStartTime, bookingDuration);
			availableCleaners = getCleanersByVehicleAndCount(availableCleaners, bookingCleaners.size());
			for (Cleaner cleaner : availableCleaners) {
				if (filteredCleaners.size() == bookingCleaners.size()) {
					break;
				}
				if(!filteredCleaners.contains(cleaner.getCleanerId())) {
					filteredCleaners.add(cleaner);
				}
			}
		}
		
		if (filteredCleaners.size() != bookingCleaners.size())
			throw new ValidationException("Not enough cleaners available for requested time slot");
		
		booking.setBookingDate(bookingDate);
		booking.setBookingStartTime(bookingStartTime);
		booking.setBookingDuration(bookingDuration);
		booking.setCleaners(filteredCleaners);
		return buildBookingResponse(bookingRepo.save(booking));
	}
	
	private BookingDTO buildBookingResponse(Booking booking) {
		List<CleanerDTO> cleanerDtos = booking.getCleaners().stream()
				.map(cleaner -> CleanerDTO.builder()
						.cleanerId(cleaner.getCleanerId())
						.cleanerName(cleaner.getCleanerName())
						.build())
				.collect(Collectors.toList());
		
		return BookingDTO.builder()
				.bookingDate(booking.getBookingDate())
				.bookingStartTime(booking.getBookingStartTime())
				.bookingDuration(booking.getBookingDuration())
				.cleanerCount(booking.getCleaners().size())
				.cleaners(cleanerDtos)
				.build();
	}
	
	public List<Cleaner> getCleanersByVehicleAndCount(List<Cleaner> cleaners, int cleanerCount) {
		// Step 1: Group cleaners by vehicle_id
		Map<Integer, List<Cleaner>> vehicleCleanerMap = cleaners.stream()
				.collect(Collectors.groupingBy(cleaner -> cleaner.getVehicle().getVehicleId()));
		
		vehicleCleanerMap.forEach((vehicleId, cleanerList) -> {
			log.info("Vehicle ID: {} has cleaners: {}", vehicleId, cleanerList.stream()
					.map(Cleaner::getCleanerName)
					.collect(Collectors.joining(", ")));
		});
		
		// Step 2: Find first vehicle that has cleaners equal to or more than cleanerCount
		Optional<Map.Entry<Integer, List<Cleaner>>> matchingVehicleEntry = vehicleCleanerMap.entrySet().stream()
				.filter(entry -> entry.getValue().size() >= cleanerCount)
				.findFirst();
		
		// Step 3: If matching vehicle is found, extract its cleaners and return
		if (matchingVehicleEntry.isPresent()) {
			List<Cleaner> matchingCleaners = matchingVehicleEntry.get().getValue();
			log.info("First vehicle (ID: {}) with {} or more cleaners: {}",
					matchingVehicleEntry.get().getKey(), cleanerCount,
					matchingCleaners.stream()
							.map(Cleaner::getCleanerName)
							.collect(Collectors.joining(", ")));
			return matchingCleaners;
		} else {
			log.info("No vehicles found with {} or more cleaners.", cleanerCount);
			return Collections.emptyList();
		}
	}
}
