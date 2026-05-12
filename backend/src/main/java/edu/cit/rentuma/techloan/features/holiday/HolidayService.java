package edu.cit.rentuma.techloan.features.holiday;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private final RestClient restClient;

    public HolidayService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://date.nager.at").build();
    }

    public List<HolidayDTO> getUpcoming(int limit) {
        int year = LocalDate.now().getYear();
        List<NagerPublicHoliday> all = new ArrayList<>();

        fetchYear(year, all);
        fetchYear(year + 1, all);

        LocalDate today = LocalDate.now();
        return all.stream()
                .filter(h -> !LocalDate.parse(h.getDate()).isBefore(today))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .limit(limit)
                .map(h -> new HolidayDTO(h.getDate(), h.getLocalName(), h.getName()))
                .collect(Collectors.toList());
    }

    private void fetchYear(int year, List<NagerPublicHoliday> target) {
        try {
            NagerPublicHoliday[] result = restClient.get()
                    .uri("/api/v3/PublicHolidays/{year}/PH", year)
                    .retrieve()
                    .body(NagerPublicHoliday[].class);
            if (result != null) target.addAll(Arrays.asList(result));
        } catch (Exception ignored) {
            // External API unavailable — return whatever was fetched so far
        }
    }
}
