package com.example.lab.Service;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Model.ConversionRequest;
import com.example.lab.Model.User;
import com.example.lab.Repository.ConversionRequestRepository;
import com.example.lab.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DateConversionService {

    private final UserRepository userRepository;
    private final ConversionRequestRepository requestRepository;

    @Autowired
    public DateConversionService(UserRepository userRepository,
                                 ConversionRequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public ConversionDTO createConversion(ConversionDTO conversionDTO) {
        User user = null;
        if (conversionDTO.getUserId() != null) {
            user = this.userRepository.findById(conversionDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        ConversionRequest request = new ConversionRequest();
        request.setTimestamp(conversionDTO.getTimestamp());
        request.setLocalTime(conversionDTO.getLocalTime());
        request.setGmtTime(conversionDTO.getGmtTime());
        request.setRequestTime(Instant.now());
        request.setUser(user);

        ConversionRequest savedRequest = this.requestRepository.save(request);
        return this.convertToDTO(savedRequest);
    }

    public List<ConversionDTO> getAllConversions() {
        return this.requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ConversionDTO getConversionById(Long id) {
        return this.requestRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Conversion not found with id: " + id));
    }

    @Transactional
    public ConversionDTO updateConversion(Long id, ConversionDTO conversionDTO) {
        ConversionRequest request = this.requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversion not found with id: " + id));

        if (conversionDTO.getUserId() != null) {
            User user = this.userRepository.findById(conversionDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            request.setUser(user);
        }

        request.setTimestamp(conversionDTO.getTimestamp());
        request.setLocalTime(conversionDTO.getLocalTime());
        request.setGmtTime(conversionDTO.getGmtTime());

        ConversionRequest updatedRequest = this.requestRepository.save(request);
        return this.convertToDTO(updatedRequest);
    }

    @Transactional
    public void deleteConversion(Long id) {
        if (!this.requestRepository.existsById(id)) {
            throw new RuntimeException("Conversion not found with id: " + id);
        }
        this.requestRepository.deleteById(id);
    }

    public ConversionDTO convertTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime localTime = instant.atZone(ZoneId.systemDefault());
        ZonedDateTime gmtTime = instant.atZone(ZoneId.of("GMT"));

        ConversionDTO dto = new ConversionDTO();
        dto.setTimestamp(timestamp);
        dto.setLocalTime(localTime.toString());
        dto.setGmtTime(gmtTime.toString());
        dto.setRequestTime(Instant.now());

        return dto;
    }

    public ConversionDTO convertAndSave(long timestamp, Long userId) {
        ConversionDTO dto = this.convertTimestamp(timestamp);

        if (userId != null) {
            Optional<User> user = this.userRepository.findById(userId);
            user.ifPresent(u -> this.saveConversionRequest(dto, u));
        }

        return dto;
    }

    private void saveConversionRequest(ConversionDTO dto, User user) {
        ConversionRequest request = new ConversionRequest();
        request.setTimestamp(dto.getTimestamp());
        request.setLocalTime(dto.getLocalTime());
        request.setGmtTime(dto.getGmtTime());
        request.setRequestTime(dto.getRequestTime());
        request.setUser(user);

        this.requestRepository.save(request);
    }

    public List<ConversionDTO> getUserConversionHistory(Long userId) {
        return this.requestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ConversionDTO convertToDTO(ConversionRequest request) {
        ConversionDTO dto = new ConversionDTO();
        dto.setId(request.getId());
        dto.setTimestamp(request.getTimestamp());
        dto.setLocalTime(request.getLocalTime());
        dto.setGmtTime(request.getGmtTime());
        dto.setRequestTime(request.getRequestTime());
        if (request.getUser() != null) {
            dto.setUserId(request.getUser().getId());
        }
        return dto;
    }
}
