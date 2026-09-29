package com.deepblue.deepblue_rescue.repository;

import com.deepblue.deepblue_rescue.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
