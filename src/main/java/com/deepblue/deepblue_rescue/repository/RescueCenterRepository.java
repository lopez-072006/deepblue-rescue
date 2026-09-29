package com.deepblue.deepblue_rescue.repository;

import com.deepblue.deepblue_rescue.domain.RescueCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RescueCenterRepository extends JpaRepository<RescueCenter, Long> {
    Optional<RescueCenter> findByCode(String code);
}
