package com.example.booking.util;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Util {
	
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	public static final LocalTime cleanerShiftStart = LocalTime.parse("08:00", formatter);
	public static final LocalTime cleanerShiftEnd = LocalTime.parse("22:00", formatter);
	
	public static void validateWorkingDay(LocalDate date) {
		if (DayOfWeek.FRIDAY.equals(date.getDayOfWeek()))
			throw new ValidationException("No cleaners working on Friday");
	}
	
	public static void validateWorkingHours(LocalDate date, LocalTime time, Integer duration) {
		if (time == null && duration == null) {
			return;
		}
		LocalDateTime cleanerStartTime = LocalDateTime.of(date, time);
		LocalDateTime cleanerEndTime = cleanerStartTime.plusHours(duration);
		
		LocalDateTime shiftStartTime = LocalDateTime.of(date, cleanerShiftStart);
		LocalDateTime shiftEndTime = LocalDateTime.of(date, cleanerShiftEnd);
		
		if (cleanerStartTime.isBefore(shiftStartTime) || cleanerEndTime.isAfter(shiftEndTime)) {
			log.error("ERROR: Provide time is not within working hours");
			throw new ValidationException("Provided time is outside working hours");
		}
	}
}
