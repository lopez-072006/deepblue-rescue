package com.deepblue.deepblue_rescue.service;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.domain.RescueCase;
import com.deepblue.deepblue_rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue_rescue.dto.response.RescueCaseResponse;
import com.deepblue.deepblue_rescue.exception.BusinessRuleException;
import com.deepblue.deepblue_rescue.exception.ResourceNotFoundException;
import com.deepblue.deepblue_rescue.mapper.RescueCaseMapper;
import com.deepblue.deepblue_rescue.repository.RescueCaseRepository;
import com.deepblue.deepblue_rescue.service.impl.RescueCaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;

    @Test
    void shouldFindRescueCaseByCode() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 20));
        rescueCase.setRescueLocation("Santa Marta");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        RescueCaseResponse response = new RescueCaseResponse(
                1L,
                "RES-001",
                LocalDate.of(2026, 8, 20),
                "Santa Marta",
                RescueStatus.ADMITTED,
                "DB-CAR",
                null
        );

        when(repository.findByCaseCode("RES-001"))
                .thenReturn(Optional.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.findByCode("RES-001");

        assertThat(result).isEqualTo(response);
        verify(repository).findByCaseCode("RES-001");
        verify(mapper).toResponse(rescueCase);
    }

    @Test
    void shouldThrowWhenRescueCaseDoesNotExist() {
        when(repository.findByCaseCode("RES-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("RES-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(mapper, never()).toResponse(any(RescueCase.class));
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-001");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        RescueCaseResponse response = new RescueCaseResponse(
                1L,
                "RES-001",
                LocalDate.of(2026, 8, 20),
                "Santa Marta",
                RescueStatus.UNDER_EVALUATION,
                "DB-CAR",
                null
        );

        when(repository.findByCaseCode("RES-001"))
                .thenReturn(Optional.of(rescueCase));
        when(repository.save(rescueCase)).thenReturn(rescueCase);
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.changeStatus(
                "RES-001",
                new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION)
        );

        assertThat(rescueCase.getStatus())
                .isEqualTo(RescueStatus.UNDER_EVALUATION);
        assertThat(result).isEqualTo(response);
        verify(repository).save(rescueCase);
    }

    @Test
    void shouldNotSaveWhenStatusTransitionIsInvalid() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-001");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        when(repository.findByCaseCode("RES-001"))
                .thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() -> service.changeStatus(
                "RES-001",
                new ChangeRescueStatusRequest(RescueStatus.READY_FOR_RELEASE)
        )).isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any(RescueCase.class));
    }
}
