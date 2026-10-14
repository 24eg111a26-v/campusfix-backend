package com.campusfix.dto;

import com.campusfix.model.Complaint;
import com.campusfix.model.User;

import java.time.LocalDateTime;

/** Request/response shapes used by the controllers (never expose the password). */
public final class Dtos {

    private Dtos() {}

    // ----- requests -----
    public record RegisterRequest(String name, String email, String password, String rollNo, String adminCode) {}

    public record LoginRequest(String email, String password) {}

    public record UpdateComplaintRequest(String status, String assignedTo, String adminNote) {}

    // ----- responses -----
    public record UserResponse(Long id, String name, String email, String rollNo, String role) {
        public static UserResponse of(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRollNo(), u.getRole());
        }
    }

    public record AuthResponse(String token, UserResponse user) {}

    public record ComplaintResponse(
            Long id, String title, String description, String category, String location,
            String image, String status, String assignedTo, String adminNote,
            LocalDateTime createdAt, UserResponse createdBy) {

        public static ComplaintResponse of(Complaint c) {
            return new ComplaintResponse(
                    c.getId(), c.getTitle(), c.getDescription(), c.getCategory(), c.getLocation(),
                    c.getImage(), c.getStatus(), c.getAssignedTo(), c.getAdminNote(),
                    c.getCreatedAt(), UserResponse.of(c.getCreatedBy()));
        }
    }
}
