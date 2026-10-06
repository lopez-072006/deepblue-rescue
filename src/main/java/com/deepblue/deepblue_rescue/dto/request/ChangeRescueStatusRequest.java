package com.deepblue.deepblue_rescue.dto.request;

import com.deepblue.deepblue_rescue.RescueStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeRescueStatusRequest(
        @NotNull(message = "Status is required")
        RescueStatus status
) {
}
