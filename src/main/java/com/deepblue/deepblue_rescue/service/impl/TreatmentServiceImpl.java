package com.deepblue.deepblue_rescue.service.impl;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.domain.Animal;
import com.deepblue.deepblue_rescue.domain.RescueCase;
import com.deepblue.deepblue_rescue.domain.Specialist;
import com.deepblue.deepblue_rescue.domain.Treatment;
import com.deepblue.deepblue_rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue_rescue.dto.response.TreatmentResponse;
import com.deepblue.deepblue_rescue.exception.BusinessRuleException;
import com.deepblue.deepblue_rescue.exception.ResourceNotFoundException;
import com.deepblue.deepblue_rescue.mapper.TreatmentMapper;
import com.deepblue.deepblue_rescue.repository.AnimalRepository;
import com.deepblue.deepblue_rescue.repository.SpecialistRepository;
import com.deepblue.deepblue_rescue.repository.TreatmentRepository;
import com.deepblue.deepblue_rescue.service.TreatmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TreatmentServiceImpl implements TreatmentService {

    private final AnimalRepository animalRepository;
    private final SpecialistRepository specialistRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper mapper;

    public TreatmentServiceImpl(
            AnimalRepository animalRepository,
            SpecialistRepository specialistRepository,
            TreatmentRepository treatmentRepository,
            TreatmentMapper mapper
    ) {
        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.treatmentRepository = treatmentRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TreatmentResponse register(CreateTreatmentRequest request) {
        Animal animal = animalRepository.findByAnimalCode(request.animalCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + request.animalCode()
                ));

        Specialist specialist = specialistRepository
                .findByProfessionalCode(request.specialistCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Specialist not found: " + request.specialistCode()
                ));

        if (!specialist.isActive()) {
            throw new BusinessRuleException(
                    "Cannot register treatment because specialist "
                            + request.specialistCode() + " is inactive."
            );
        }

        RescueCase rescueCase = animal.getRescueCase();
        if (rescueCase == null) {
            throw new BusinessRuleException(
                    "Cannot register treatment because animal "
                            + request.animalCode() + " has no rescue case."
            );
        }

        if (rescueCase.getStatus() == RescueStatus.RELEASED
                || rescueCase.getStatus() == RescueStatus.CLOSED) {
            throw new BusinessRuleException(
                    "Cannot register treatment because rescue case "
                            + rescueCase.getCaseCode() + " is "
                            + rescueCase.getStatus() + "."
            );
        }

        if (request.performedAt() == null) {
            throw new BusinessRuleException(
                    "Treatment performedAt is required."
            );
        }

        if (request.performedAt().toLocalDate()
                .isBefore(rescueCase.getRescueDate())) {
            throw new BusinessRuleException(
                    "Treatment date cannot be before rescue date "
                            + rescueCase.getRescueDate() + "."
            );
        }

        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        Treatment savedTreatment = treatmentRepository.save(treatment);
        return mapper.toResponse(savedTreatment);
    }

    @Override
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        return treatmentRepository
                .findByAnimalAnimalCodeOrderByPerformedAtAsc(animalCode)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
