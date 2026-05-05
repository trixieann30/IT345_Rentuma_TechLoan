package edu.cit.rentuma.techloan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.RefreshTokenRepository;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.inventory.repository.InventoryRepository;
import edu.cit.rentuma.techloan.features.loan.repository.LoanRepository;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import edu.cit.rentuma.techloan.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JwtUtil jwtUtil;
    @Autowired protected UserRepository userRepository;
    @Autowired protected RefreshTokenRepository refreshTokenRepository;
    @Autowired protected InventoryRepository inventoryRepository;
    @Autowired protected BorrowRequestRepository borrowRequestRepository;
    @Autowired protected LoanRepository loanRepository;
    @Autowired protected PenaltyRepository penaltyRepository;

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    protected final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void clearDatabase() {
        penaltyRepository.deleteAll();
        loanRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        inventoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createStudent() {
        return createStudent("student@cit.edu", "STU-001");
    }

    protected User createStudent(String email, String studentId) {
        User u = User.builder()
                .fullName("Test Student")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password1"))
                .studentId(studentId)
                .role(User.Role.STUDENT)
                .penaltyPoints(0)
                .build();
        return userRepository.save(u);
    }

    protected User createCustodian() {
        User u = User.builder()
                .fullName("Test Custodian")
                .email("custodian@cit.edu")
                .passwordHash(passwordEncoder.encode("Password1"))
                .studentId("CUS-001")
                .role(User.Role.CUSTODIAN)
                .penaltyPoints(0)
                .build();
        return userRepository.save(u);
    }

    protected InventoryItem createAvailableItem() {
        InventoryItem item = new InventoryItem("LAP-001", "Test Laptop", "A test laptop", "Laptop");
        item.setCondition("Excellent");
        item.setSpecifications("8GB RAM");
        item.setTotalQuantity(3);
        item.setAvailableQuantity(3);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(item);
    }

    protected String studentToken(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), "STUDENT");
    }

    protected String custodianToken(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), "CUSTODIAN");
    }
}
