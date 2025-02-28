package com.example.booking.controller;

import com.example.booking.model.CleanerDTO;
import com.example.booking.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping(value = "/check")
    public List<CleanerDTO> checkCleanersAvailabilityByDate(@RequestParam("date") LocalDate date,
                                                            @RequestParam(value = "startTime", required = false) LocalTime startTime,
                                                            @RequestParam(value = "duration", required = false) Integer duration) {
        //log.info("Checking availability for date: {}, start time: {}, duration: {}", date, startTime, duration);
        return availabilityService.getAvailableCleanerDtos(date, startTime, duration);
    }
}
