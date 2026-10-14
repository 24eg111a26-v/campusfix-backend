package com.campusfix.controller;

import com.campusfix.dto.Dtos.ComplaintResponse;
import com.campusfix.dto.Dtos.UpdateComplaintRequest;
import com.campusfix.model.Complaint;
import com.campusfix.model.User;
import com.campusfix.repository.ComplaintRepository;
import com.campusfix.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintRepository complaints;
    private final FileStorageService storage;

    public ComplaintController(ComplaintRepository complaints, FileStorageService storage) {
        this.complaints = complaints;
        this.storage = storage;
    }

    // GET /api/complaints/categories
    @GetMapping("/categories")
    public List<String> categories() {
        return Complaint.CATEGORIES;
    }

    // POST /api/complaints   (multipart form: title, description, category, location, optional image)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponse> create(
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String location,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        if (title.isBlank() || description.isBlank() || location.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields are required");
        if (!Complaint.CATEGORIES.contains(category))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category");

        Complaint c = new Complaint();
        c.setTitle(title.trim());
        c.setDescription(description.trim());
        c.setCategory(category);
        c.setLocation(location.trim());
        c.setCreatedBy(user);
        if (image != null && !image.isEmpty()) c.setImage(storage.saveImage(image));

        return ResponseEntity.status(HttpStatus.CREATED).body(ComplaintResponse.of(complaints.save(c)));
    }

    // GET /api/complaints/mine   (student's own complaints)
    @GetMapping("/mine")
    public List<ComplaintResponse> mine(@AuthenticationPrincipal User user) {
        return complaints.findByCreatedByIdOrderByCreatedAtDesc(user.getId())
                .stream().map(ComplaintResponse::of).toList();
    }

    // GET /api/complaints?status=&category=   (admin only - see SecurityConfig)
    @GetMapping
    public List<ComplaintResponse> all(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String category) {
        return complaints.search(blankToNull(status), blankToNull(category))
                .stream().map(ComplaintResponse::of).toList();
    }

    // GET /api/complaints/stats   (admin only)
    @GetMapping("/stats")
    public Map<String, Long> stats() {
        Map<String, Long> result = new LinkedHashMap<>();
        long total = 0;
        for (String s : Complaint.STATUSES) {
            long n = complaints.countByStatus(s);
            result.put(s, n);
            total += n;
        }
        result.put("Total", total);
        return result;
    }

    // PATCH /api/complaints/{id}   (admin only: status, assignedTo, adminNote)
    @PatchMapping("/{id}")
    public ComplaintResponse update(@PathVariable Long id, @RequestBody UpdateComplaintRequest req) {
        Complaint c = find(id);
        if (req.status() != null && !req.status().isBlank()) {
            if (!Complaint.STATUSES.contains(req.status()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
            c.setStatus(req.status());
        }
        if (req.assignedTo() != null) c.setAssignedTo(req.assignedTo());
        if (req.adminNote() != null) c.setAdminNote(req.adminNote());
        return ComplaintResponse.of(complaints.save(c));
    }

    // DELETE /api/complaints/{id}   (owner while Pending, or admin)
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Complaint c = find(id);
        boolean isAdmin = "admin".equals(user.getRole());
        boolean ownerAndPending = c.getCreatedBy().getId().equals(user.getId())
                && "Pending".equals(c.getStatus());
        if (!isAdmin && !ownerAndPending)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this complaint");
        complaints.delete(c);
        return Map.of("message", "Deleted");
    }

    private Complaint find(Long id) {
        return complaints.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
