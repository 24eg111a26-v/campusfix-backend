package com.campusfix.repository;

import com.campusfix.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(String status);

    // Both filters are optional (pass null to skip)
    @Query("select c from Complaint c " +
           "where (:status is null or c.status = :status) " +
           "and (:category is null or c.category = :category) " +
           "order by c.createdAt desc")
    List<Complaint> search(@Param("status") String status, @Param("category") String category);
}
