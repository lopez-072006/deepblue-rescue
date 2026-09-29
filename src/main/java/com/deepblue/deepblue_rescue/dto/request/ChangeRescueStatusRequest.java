package com.deepblue.deepblue_rescue.dto.request;

import com.deepblue.deepblue_rescue.RescueStatus;

public record ChangeRescueStatusRequest(
        RescueStatus status
) {
}
