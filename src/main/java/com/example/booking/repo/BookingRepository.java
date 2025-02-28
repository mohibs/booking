package com.example.booking.repo;

import com.example.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
	@Query("SELECT b FROM Booking b JOIN b.cleaners Cleaner WHERE b.bookingDate = :bookingDate AND Cleaner.cleanerId = :cleanerId")
	List<Booking> findByBookingDateAndCleanerId(LocalDate bookingDate, Integer cleanerId);
}
