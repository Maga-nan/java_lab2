package com.example.lab.Repository;

import com.example.lab.Model.ConversionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversionRequestRepository extends JpaRepository<ConversionRequest, Long> {
    List<ConversionRequest> findByUserId(Long userId);
}