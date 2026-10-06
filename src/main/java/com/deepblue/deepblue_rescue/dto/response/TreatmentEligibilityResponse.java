package com.deepblue.deepblue_rescue.dto.response;

public record TreatmentEligibilityResponse(
        String animalCode,
        boolean eligible
) {
}
