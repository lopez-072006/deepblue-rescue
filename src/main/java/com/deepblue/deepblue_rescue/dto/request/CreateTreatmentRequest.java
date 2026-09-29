package com.deepblue.deepblue_rescue.dto.request;

import com.deepblue.deepblue_rescue.TreatmentType;

import java.time.LocalDateTime;

public record CreateTreatmentRequest(
        String animalCode,
        String specialistCode,
        LocalDateTime performedAt,
        TreatmentType type,
        String description
) {
}
