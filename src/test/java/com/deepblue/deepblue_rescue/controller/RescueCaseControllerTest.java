package com.deepblue.deepblue_rescue.controller;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue_rescue.dto.response.RescueCaseResponse;
import com.deepblue.deepblue_rescue.exception.BusinessRuleException;
import com.deepblue.deepblue_rescue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue_rescue.exception.ResourceNotFoundException;
import com.deepblue.deepblue_rescue.service.RescueCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RescueCaseController.class)
@Import(GlobalExceptionHandler.class)
public class RescueCaseControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RescueCaseService service;

    private RescueCaseResponse response(RescueStatus status) {
        return new RescueCaseResponse(1L, "RES-001", LocalDate.of(2026, 8, 20),
                "Bahia Concha", status, "DB-CAR", "AN-001");
    }

    @Test
    void shouldReturnRescueCaseByCode() throws Exception {
        when(service.findByCode("RES-001")).thenReturn(response(RescueStatus.IN_REHABILITATION));
        mockMvc.perform(get("/api/rescue-cases/RES-001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.caseCode").value("RES-001"));
        verify(service).findByCode("RES-001");
    }

    @Test
    void shouldReturn404WhenCaseDoesNotExist() throws Exception {
        when(service.findByCode("RES-999")).thenThrow(new ResourceNotFoundException("Rescue case not found: RES-999"));
        mockMvc.perform(get("/api/rescue-cases/RES-999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Rescue case not found: RES-999"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void shouldReturnCasesByStatus() throws Exception {
        when(service.findByStatus(RescueStatus.IN_REHABILITATION))
                .thenReturn(List.of(response(RescueStatus.IN_REHABILITATION), response(RescueStatus.IN_REHABILITATION)));
        mockMvc.perform(get("/api/rescue-cases").param("status", "IN_REHABILITATION"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[1].status").value("IN_REHABILITATION"));
        verify(service).findByStatus(RescueStatus.IN_REHABILITATION);
    }

    @Test
    void shouldReturn400ForInvalidStatusParameter() throws Exception {
        mockMvc.perform(get("/api/rescue-cases").param("status", "FLYING"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.details.status").exists());
    }

    @Test
    void shouldChangeStatus() throws Exception {
        when(service.changeStatus(eq("RES-001"), any(ChangeRescueStatusRequest.class)))
                .thenReturn(response(RescueStatus.READY_FOR_RELEASE));
        mockMvc.perform(patch("/api/rescue-cases/RES-001/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY_FOR_RELEASE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("READY_FOR_RELEASE"));
        verify(service).changeStatus(eq("RES-001"), any(ChangeRescueStatusRequest.class));
    }

    @Test
    void shouldRejectInvalidStatusRequestWithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/rescue-cases/RES-001/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.status").value("Status is required"));
        verify(service, never()).changeStatus(anyString(), any());
    }

    @Test
    void shouldReturn409ForInvalidTransition() throws Exception {
        when(service.changeStatus(eq("RES-001"), any())).thenThrow(new BusinessRuleException("Invalid status transition"));
        mockMvc.perform(patch("/api/rescue-cases/RES-001/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY_FOR_RELEASE\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    void shouldReturn400ForInvalidJson() throws Exception {
        mockMvc.perform(patch("/api/rescue-cases/RES-001/status").contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnSafe500ErrorForUnexpectedException() throws Exception {
        when(service.findByCode("RES-001")).thenThrow(new IllegalStateException("internal detail"));
        mockMvc.perform(get("/api/rescue-cases/RES-001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not("internal detail")));
    }
}
