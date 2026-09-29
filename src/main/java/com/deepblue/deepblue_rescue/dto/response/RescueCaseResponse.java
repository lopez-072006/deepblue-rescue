package com.deepblue.deepblue_rescue.dto.response;

import com.deepblue.deepblue_rescue.RescueStatus;

import java.time.LocalDate;

public record RescueCaseResponse(
        Long id,
        String caseCode,
        LocalDate rescueDate,
        String rescueLocation,
        RescueStatus status,
        String centerCode,
        String animalCode
) {
}
