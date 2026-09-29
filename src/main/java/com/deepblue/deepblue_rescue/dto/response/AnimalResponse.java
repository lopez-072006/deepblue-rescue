package com.deepblue.deepblue_rescue.dto.response;

import com.deepblue.deepblue_rescue.AnimalSex;
import com.deepblue.deepblue_rescue.RescueStatus;

public record AnimalResponse(
        Long id,
        String animalCode,
        String commonName,
        String scientificName,
        AnimalSex sex,
        String caseCode,
        RescueStatus rescueStatus
) {
}
