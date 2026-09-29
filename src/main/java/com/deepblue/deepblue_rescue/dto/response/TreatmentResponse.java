package com.deepblue.deepblue_rescue.dto.response;

import com.deepblue.deepblue_rescue.TreatmentType;

import java.time.LocalDateTime;

public record TreatmentResponse(
        Long id,
        String animalCode,
        String specialistCode,
        LocalDateTime performedAt,
        TreatmentType type,
        String description
) {
}
