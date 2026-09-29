package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue_rescue.dto.response.TreatmentResponse;

import java.util.List;

public interface TreatmentService {

    TreatmentResponse register(CreateTreatmentRequest request);

    List<TreatmentResponse> findByAnimalCode(String animalCode);
}
