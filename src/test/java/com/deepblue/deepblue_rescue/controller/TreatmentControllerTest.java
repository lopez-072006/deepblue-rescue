package com.deepblue.deepblue_rescue.controller;

import com.deepblue.deepblue_rescue.TreatmentType;
import com.deepblue.deepblue_rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue_rescue.dto.response.TreatmentResponse;
import com.deepblue.deepblue_rescue.exception.BusinessRuleException;
import com.deepblue.deepblue_rescue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue_rescue.exception.ResourceNotFoundException;
import com.deepblue.deepblue_rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
public class TreatmentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TreatmentService service;

    private static final String VALID_REQUEST = """
            {"animalCode":"AN-001","specialistCode":"SPEC-001",
             "performedAt":"2026-08-21T09:00:00","type":"WOUND_CARE",
             "description":"Cleaning and treatment of flipper injury."}
            """;

    @Test
    void shouldCreateTreatment() throws Exception {
        when(service.register(any(CreateTreatmentRequest.class))).thenReturn(new TreatmentResponse(
                10L, "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE, "Cleaning and treatment of flipper injury."));
        mockMvc.perform(post("/api/treatments").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.animalCode").value("AN-001"));
        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldRejectInvalidTreatmentWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/treatments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"animalCode\":\"\",\"specialistCode\":\"\",\"performedAt\":null,\"type\":null,\"description\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.animalCode").exists())
                .andExpect(jsonPath("$.details.description").value("Description is required"));
        verify(service, never()).register(any());
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(service.register(any())).thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));
        mockMvc.perform(post("/api/treatments").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Animal not found: AN-999"));
    }

    @Test
    void shouldReturn409ForBusinessRuleViolation() throws Exception {
        when(service.register(any())).thenThrow(new BusinessRuleException("Released animals cannot receive treatments"));
        mockMvc.perform(post("/api/treatments").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
