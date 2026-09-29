package com.deepblue.deepblue_rescue.repository;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.domain.RescueCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {
    Optional<RescueCase> findByCaseCode(String caseCode);

    List<RescueCase> findByStatus(RescueStatus status);

    List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);

    List<RescueCase> findByRescueCenterCode(String centerCode);

    List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDate date);
}
