package edu.cit.rentuma.techloan.dto;

import edu.cit.rentuma.techloan.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String studentId;
    private User.Role role;
    private Integer penaltyPoints;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .role(user.getRole())
                .penaltyPoints(user.getPenaltyPoints())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
