package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.TreatmentType;
import com.deepblue.deepblue_rescue.domain.Animal;
import com.deepblue.deepblue_rescue.domain.RescueCase;
import com.deepblue.deepblue_rescue.domain.Specialist;
import com.deepblue.deepblue_rescue.domain.Treatment;
import com.deepblue.deepblue_rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue_rescue.dto.response.TreatmentResponse;
import com.deepblue.deepblue_rescue.exception.BusinessRuleException;
import com.deepblue.deepblue_rescue.mapper.TreatmentMapper;
import com.deepblue.deepblue_rescue.repository.AnimalRepository;
import com.deepblue.deepblue_rescue.repository.SpecialistRepository;
import com.deepblue.deepblue_rescue.repository.TreatmentRepository;
import com.deepblue.deepblue_rescue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    @Test
    void shouldRegisterTreatmentSuccessfully() {
        Animal animal = animalWithStatus(RescueStatus.IN_REHABILITATION);
        Specialist specialist = specialist(true);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );
        TreatmentResponse response = new TreatmentResponse(
                1L,
                "AN-2026-100",
                "SPEC-001",
                request.performedAt(),
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));
        when(treatmentRepository.save(any(Treatment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Treatment.class))).thenReturn(response);

        TreatmentResponse result = service.register(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Treatment> treatmentCaptor =
                ArgumentCaptor.forClass(Treatment.class);
        verify(treatmentRepository).save(treatmentCaptor.capture());
        Treatment savedTreatment = treatmentCaptor.getValue();
        assertThat(savedTreatment.getAnimal()).isSameAs(animal);
        assertThat(savedTreatment.getSpecialist()).isSameAs(specialist);
        assertThat(savedTreatment.getType()).isEqualTo(TreatmentType.WOUND_CARE);
        assertThat(savedTreatment.getPerformedAt())
                .isEqualTo(LocalDateTime.of(2026, 8, 21, 9, 0));
        assertThat(savedTreatment.getDescription())
                .isEqualTo("Cleaning of left front flipper injury.");
        verify(mapper).toResponse(savedTreatment);
    }

    @Test
    void shouldRejectTreatmentWhenSpecialistIsInactive() {
        Animal animal = animalWithStatus(RescueStatus.IN_REHABILITATION);
        Specialist inactiveSpecialist = specialist(false);

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(inactiveSpecialist));

        assertThatThrownBy(() -> service.register(
                validTreatmentRequest(TreatmentType.WOUND_CARE)
        )).isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldRejectTreatmentForReleasedAnimal() {
        Animal releasedAnimal = animalWithStatus(RescueStatus.RELEASED);
        Specialist activeSpecialist = specialist(true);

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(releasedAnimal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(activeSpecialist));

        assertThatThrownBy(() -> service.register(
                validTreatmentRequest(TreatmentType.OBSERVATION)
        )).isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    @Test
    void shouldRejectTreatmentDatedBeforeRescue() {
        Animal animal = animalWithStatus(RescueStatus.IN_REHABILITATION);
        Specialist activeSpecialist = specialist(true);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 15, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );

        when(animalRepository.findByAnimalCode("AN-2026-100"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(activeSpecialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    private Animal animalWithStatus(RescueStatus status) {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 20));
        rescueCase.setStatus(status);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-100");
        animal.setCommonName("Green Sea Turtle");
        animal.setRescueCase(rescueCase);
        return animal;
    }

    private Specialist specialist(boolean active) {
        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setActive(active);
        return specialist;
    }

    private CreateTreatmentRequest validTreatmentRequest(TreatmentType type) {
        return new CreateTreatmentRequest(
                "AN-2026-100",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                type,
                "Cleaning of left front flipper injury."
        );
    }
}
