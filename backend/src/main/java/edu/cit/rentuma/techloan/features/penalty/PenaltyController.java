package edu.cit.rentuma.techloan.features.penalty;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.penalty.dto.AdminPenaltyDTO;
import edu.cit.rentuma.techloan.features.penalty.dto.PenaltyDTO;
import edu.cit.rentuma.techloan.features.penalty.dto.PenaltySummaryDTO;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import edu.cit.rentuma.techloan.shared.factory.DTOFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PenaltyController {

    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final DTOFactory dtoFactory;

    public PenaltyController(UserRepository userRepository,
                              PenaltyRepository penaltyRepository,
                              DTOFactory dtoFactory) {
        this.userRepository    = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.dtoFactory        = dtoFactory;
    }

    @GetMapping("/penalties")
    public ResponseEntity<?> getAllPenalties(
            @AuthenticationPrincipal UserDetails userDetails) {

        User caller = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (caller.getRole() != User.Role.CUSTODIAN) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<AdminPenaltyDTO> result = penaltyRepository.findAll().stream()
                .map(p -> {
                    String name  = userRepository.findById(p.getUserId())
                            .map(User::getFullName).orElse("Unknown");
                    String email = userRepository.findById(p.getUserId())
                            .map(User::getEmail).orElse("");
                    return new AdminPenaltyDTO(p.getId(), p.getUserId(), name, email,
                            p.getItemName(), p.getDaysOverdue(), p.getPenaltyPoints(),
                            p.getPaid(), p.getCalculatedAt());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}/penalties")
    public ResponseEntity<?> getUserPenalties(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User caller = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (caller.getRole() != User.Role.CUSTODIAN && !caller.getId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-001: User not found: " + id));

        List<Penalty> penalties = penaltyRepository.findByUserId(id);
        List<PenaltyDTO> penaltyDTOs = penalties.stream()
                .map(dtoFactory::toPenaltyDTO)
                .collect(Collectors.toList());

        int total = penalties.stream()
                .filter(p -> !p.getPaid())
                .mapToInt(Penalty::getPenaltyPoints)
                .sum();

        PenaltySummaryDTO summary = new PenaltySummaryDTO(
                target.getId(), target.getFullName(), total, penaltyDTOs);

        return ResponseEntity.ok(summary);
    }
}
