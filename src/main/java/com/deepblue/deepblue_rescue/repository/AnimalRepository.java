package com.deepblue.deepblue_rescue.repository;

import com.deepblue.deepblue_rescue.RescueStatus;
import com.deepblue.deepblue_rescue.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByAnimalCode(String animalCode);

    List<Animal> findByCommonNameContainingIgnoreCase(String commonName);

    List<Animal> findByRescueCaseStatus(RescueStatus status);

    List<Animal> findByRescueCaseRescueCenterCode(String centerCode);
    }
