package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.domain.Animal;
import com.deepblue.deepblue_rescue.domain.RescueCase;
import com.deepblue.deepblue_rescue.mapper.AnimalMapper;
import com.deepblue.deepblue_rescue.repository.AnimalRepository;
import com.deepblue.deepblue_rescue.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository repository;

    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    private AnimalServiceImpl service;

    @Test
    void shouldAllowTreatmentDuringEvaluationAndRehabilitation() {
        Animal underEvaluation = animalWithStatus(RescueStatus.UNDER_EVALUATION);
        Animal inRehabilitation = animalWithStatus(RescueStatus.IN_REHABILITATION);

        when(repository.findByAnimalCode("AN-EVALUATION"))
                .thenReturn(Optional.of(underEvaluation));
        when(repository.findByAnimalCode("AN-REHABILITATION"))
                .thenReturn(Optional.of(inRehabilitation));

        assertThat(service.canReceiveTreatment("AN-EVALUATION")).isTrue();
        assertThat(service.canReceiveTreatment("AN-REHABILITATION")).isTrue();
    }

    @Test
    void shouldNotAllowTreatmentForOtherStatuses() {
        Animal released = animalWithStatus(RescueStatus.RELEASED);
        when(repository.findByAnimalCode("AN-RELEASED"))
                .thenReturn(Optional.of(released));

        assertThat(service.canReceiveTreatment("AN-RELEASED")).isFalse();
    }

    @Test
    void shouldNotAllowTreatmentWhenAnimalHasNoRescueCase() {
        Animal animal = new Animal();
        animal.setAnimalCode("AN-WITHOUT-CASE");
        when(repository.findByAnimalCode("AN-WITHOUT-CASE"))
                .thenReturn(Optional.of(animal));

        assertThat(service.canReceiveTreatment("AN-WITHOUT-CASE")).isFalse();
    }

    private Animal animalWithStatus(RescueStatus status) {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setStatus(status);

        Animal animal = new Animal();
        animal.setRescueCase(rescueCase);
        return animal;
    }
}
