package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue_rescue.dto.response.RescueCaseResponse;

import java.util.List;

public interface RescueCaseService {

    RescueCaseResponse findByCode(String caseCode);

    List<RescueCaseResponse> findByStatus(RescueStatus status);

    RescueCaseResponse changeStatus(
            String caseCode,
            ChangeRescueStatusRequest request
    );
}
