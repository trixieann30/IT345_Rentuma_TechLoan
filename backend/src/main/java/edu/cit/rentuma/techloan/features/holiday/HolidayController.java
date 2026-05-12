package edu.cit.rentuma.techloan.features.holiday;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    /**
     * Returns upcoming Philippine public holidays sourced from the Nager.Date public API.
     * Used by the dashboard to display lab closure dates.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<HolidayDTO>> upcoming(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(holidayService.getUpcoming(limit));
    }
}
