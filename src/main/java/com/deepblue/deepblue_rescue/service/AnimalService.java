package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.dto.response.AnimalResponse;

import java.util.List;

public interface AnimalService {

    AnimalResponse findByCode(String animalCode);

    List<AnimalResponse> findAnimalsInRehabilitation();

    boolean canReceiveTreatment(String animalCode);
}
