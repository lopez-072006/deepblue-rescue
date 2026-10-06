package com.deepblue.deepblue_rescue.controller;

import com.deepblue.deepblue_rescue.AnimalSex;
import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.TreatmentType;
import com.deepblue.deepblue_rescue.dto.response.AnimalResponse;
import com.deepblue.deepblue_rescue.dto.response.TreatmentResponse;
import com.deepblue.deepblue_rescue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue_rescue.exception.ResourceNotFoundException;
import com.deepblue.deepblue_rescue.service.AnimalService;
import com.deepblue.deepblue_rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
public class AnimalControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AnimalService animalService;
    @MockitoBean TreatmentService treatmentService;

    private AnimalResponse animal() {
        return new AnimalResponse(1L, "AN-001", "Green Sea Turtle", "Chelonia mydas",
                AnimalSex.UNKNOWN, "RES-001", RescueStatus.IN_REHABILITATION);
    }

    @Test
    void shouldReturnAnimalByCode() throws Exception {
        when(animalService.findByCode("AN-001")).thenReturn(animal());
        mockMvc.perform(get("/api/animals/AN-001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.animalCode").value("AN-001"));
        verify(animalService).findByCode("AN-001");
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(animalService.findByCode("AN-999")).thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));
        mockMvc.perform(get("/api/animals/AN-999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"));
    }

    @Test
    void shouldReturnAnimalsInRehabilitation() throws Exception {
        when(animalService.findAnimalsInRehabilitation()).thenReturn(List.of(animal(), animal()));
        mockMvc.perform(get("/api/animals/in-rehabilitation"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[1].animalCode").value("AN-001"));
        verify(animalService).findAnimalsInRehabilitation();
    }

    @Test
    void shouldReturnAnimalTreatments() throws Exception {
        when(treatmentService.findByAnimalCode("AN-001")).thenReturn(List.of(
                new TreatmentResponse(10L, "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 21, 9, 0),
                        TreatmentType.WOUND_CARE, "Cleaning of flipper injury.")));
        mockMvc.perform(get("/api/animals/AN-001/treatments"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("WOUND_CARE"));
        verify(treatmentService).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnTreatmentEligibility() throws Exception {
        when(animalService.canReceiveTreatment("AN-001")).thenReturn(true);
        mockMvc.perform(get("/api/animals/AN-001/treatment-eligibility"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.eligible").value(true));
        verify(animalService).canReceiveTreatment("AN-001");
    }
}
