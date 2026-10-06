package com.deepblue.deepblue_rescue;

import com.deepblue.deepblue_rescue.domain.*;
import com.deepblue.deepblue_rescue.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.dao.DataIntegrityViolationException;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

        @Container
        @ServiceConnection
        static final PostgreSQLContainer postgres =
                new PostgreSQLContainer("postgres:18-alpine")
                        .withDatabaseName("deepblue_test")
                        .withUsername("deepblue")
                        .withPassword("deepblue");

@Autowired
private RescueCenterRepository rescueCenterRepository;

@Autowired
private RescueCaseRepository rescueCaseRepository;

@Autowired
private AnimalRepository animalRepository;

@Autowired
private SpecialistRepository specialistRepository;

@Autowired
private ExpertiseRepository expertiseRepository;

@Autowired
private TreatmentRepository treatmentRepository;

@Autowired
private JdbcTemplate jdbcTemplate;

@Test
void shouldHaveExecutedFlywayMigrations() {

    Integer v1 = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '1'
              AND success = TRUE
            """,
            Integer.class
    );

    Integer v2 = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '2'
              AND success = TRUE
            """,
            Integer.class
    );

    assertThat(v1).isEqualTo(1);
    assertThat(v2).isEqualTo(1);
}

@Test
void shouldTestInheritedRepositoryMethods() {

    RescueCenter center = new RescueCenter();
    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");

    RescueCenter savedCenter = rescueCenterRepository.save(center);

    Long id = savedCenter.getId();

    assertThat(rescueCenterRepository.findById(id))
            .isPresent();

    assertThat(rescueCenterRepository.existsById(id))
            .isTrue();

    assertThat(rescueCenterRepository.count())
            .isEqualTo(1);
}

@Test
void shouldTestOneToManyRelationship() {

    RescueCenter center = new RescueCenter();
    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");

    RescueCase case1 = new RescueCase();
    case1.setCaseCode("CASE-001");
    case1.setRescueDate(LocalDate.of(2026, 9, 1));
    case1.setRescueLocation("Santa Marta");
    case1.setStatus(RescueStatus.ADMITTED);

    RescueCase case2 = new RescueCase();
    case2.setCaseCode("CASE-002");
    case2.setRescueDate(LocalDate.of(2026, 9, 2));
    case2.setRescueLocation("Taganga");
    case2.setStatus(RescueStatus.UNDER_EVALUATION);

    center.addCase(case1);
    center.addCase(case2);

    rescueCenterRepository.save(center);

    RescueCase savedCase1 = rescueCaseRepository.findById(case1.getId()).orElseThrow();
    RescueCase savedCase2 = rescueCaseRepository.findById(case2.getId()).orElseThrow();

    assertThat(savedCase1.getRescueCenter().getId())
            .isEqualTo(center.getId());

    assertThat(savedCase2.getRescueCenter().getId())
            .isEqualTo(center.getId());
}

@Test
void shouldTestRescueCaseOneToOneAnimal() {

    RescueCenter center = new RescueCenter();
    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");

    RescueCase rescueCase = new RescueCase();
    rescueCase.setCaseCode("RES-2026-001");
    rescueCase.setRescueDate(LocalDate.of(2026, 9, 1));
    rescueCase.setRescueLocation("Santa Marta");
    rescueCase.setStatus(RescueStatus.ADMITTED);
    rescueCase.setRescueCenter(center);

    Animal animal = new Animal();
    animal.setAnimalCode("AN-2026-001");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    rescueCase.assignAnimal(animal);

    rescueCenterRepository.save(center);
    rescueCaseRepository.save(rescueCase);

    RescueCase savedCase =
            rescueCaseRepository.findById(rescueCase.getId()).orElseThrow();

    Animal savedAnimal =
            animalRepository.findById(animal.getId()).orElseThrow();

    assertThat(savedCase.getAnimal())
            .isNotNull();

    assertThat(savedAnimal.getRescueCase())
            .isNotNull();

    assertThat(savedCase.getAnimal().getId())
            .isEqualTo(savedAnimal.getId());

    assertThat(savedAnimal.getRescueCase().getId())
            .isEqualTo(savedCase.getId());
}

@Test
void shouldTestAnimalOneToOneMedicalRecord() {

    Animal animal = new Animal();
    animal.setAnimalCode("AN-2026-002");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    MedicalRecord medicalRecord = new MedicalRecord();
    medicalRecord.setInitialWeight(new BigDecimal("28.40"));
    medicalRecord.setInitialCondition("STABLE");
    medicalRecord.setInjuries("Left front flipper injury");

    animal.assignMedicalRecord(medicalRecord);

    animalRepository.save(animal);

    assertThat(animal.getId()).isNotNull();
    assertThat(medicalRecord.getId()).isNotNull();
}

@Test
void shouldTestManyToManyRelationship() {

    Expertise trauma = expertiseRepository
            .findByNameIgnoreCase("Trauma")
            .orElseThrow();

    Expertise rehabilitation = expertiseRepository
            .findByNameIgnoreCase("Rehabilitation")
            .orElseThrow();

    Specialist elena = new Specialist();
    elena.setProfessionalCode("PRO-001");
    elena.setFirstName("Elena");
    elena.setLastName("Vargas");
    elena.setEmail("elena.vargas@deepblue.com");
    elena.setActive(true);

    elena.addExpertise(trauma);
    elena.addExpertise(rehabilitation);

    specialistRepository.save(elena);

    Specialist savedElena =
            specialistRepository.findById(elena.getId()).orElseThrow();

    assertThat(savedElena.getExpertiseAreas())
            .hasSize(2);
}

@Test
void shouldFindRescueCasesByStatus() {

    RescueCenter center = new RescueCenter();
    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");

    rescueCenterRepository.save(center);

    RescueCase case1 = new RescueCase();
    case1.setCaseCode("RES-001");
    case1.setRescueDate(LocalDate.of(2026, 9, 1));
    case1.setRescueLocation("Santa Marta");
    case1.setStatus(RescueStatus.IN_REHABILITATION);
    case1.setRescueCenter(center);

    RescueCase case2 = new RescueCase();
    case2.setCaseCode("RES-002");
    case2.setRescueDate(LocalDate.of(2026, 9, 2));
    case2.setRescueLocation("Taganga");
    case2.setStatus(RescueStatus.READY_FOR_RELEASE);
    case2.setRescueCenter(center);

    RescueCase case3 = new RescueCase();
    case3.setCaseCode("RES-003");
    case3.setRescueDate(LocalDate.of(2026, 9, 3));
    case3.setRescueLocation("Rodadero");
    case3.setStatus(RescueStatus.IN_REHABILITATION);
    case3.setRescueCenter(center);

    rescueCaseRepository.saveAll(List.of(case1, case2, case3));

    List<RescueCase> results =
            rescueCaseRepository.findByStatus(RescueStatus.IN_REHABILITATION);

    assertThat(results).hasSize(2);
}

@Test
void shouldFindAnimalsByRescueCenterCode() {

    RescueCenter car = new RescueCenter();
    car.setCode("DB-CAR");
    car.setName("DeepBlue Caribbean Center");
    car.setCity("Santa Marta");

    RescueCenter pac = new RescueCenter();
    pac.setCode("DB-PAC");
    pac.setName("DeepBlue Pacific Center");
    pac.setCity("Buenaventura");

    rescueCenterRepository.saveAll(List.of(car, pac));

    RescueCase caseCar = new RescueCase();
    caseCar.setCaseCode("RES-CAR-001");
    caseCar.setRescueDate(LocalDate.of(2026, 9, 1));
    caseCar.setRescueLocation("Santa Marta");
    caseCar.setStatus(RescueStatus.ADMITTED);
    caseCar.setRescueCenter(car);

    RescueCase casePac = new RescueCase();
    casePac.setCaseCode("RES-PAC-001");
    casePac.setRescueDate(LocalDate.of(2026, 9, 2));
    casePac.setRescueLocation("Buenaventura");
    casePac.setStatus(RescueStatus.ADMITTED);
    casePac.setRescueCenter(pac);

    rescueCaseRepository.saveAll(List.of(caseCar, casePac));

    Animal animalCar = new Animal();
    animalCar.setAnimalCode("AN-CAR-001");
    animalCar.setCommonName("Green Sea Turtle");
    animalCar.setScientificName("Chelonia mydas");
    animalCar.setSex(AnimalSex.UNKNOWN);
    caseCar.assignAnimal(animalCar);

    Animal animalPac = new Animal();
    animalPac.setAnimalCode("AN-PAC-001");
    animalPac.setCommonName("Green Sea Turtle");
    animalPac.setScientificName("Chelonia mydas");
    animalPac.setSex(AnimalSex.UNKNOWN);
    casePac.assignAnimal(animalPac);

    animalRepository.saveAll(List.of(animalCar, animalPac));

    List<Animal> results =
            animalRepository.findByRescueCaseRescueCenterCode("DB-CAR");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getAnimalCode())
            .isEqualTo("AN-CAR-001");
}

@Test
void shouldFindActiveSpecialistsByExpertise() {

    Expertise trauma = expertiseRepository
            .findByNameIgnoreCase("Trauma")
            .orElseThrow();

    Expertise rehabilitation = expertiseRepository
            .findByNameIgnoreCase("Rehabilitation")
            .orElseThrow();

    Expertise marineMammals = expertiseRepository
            .findByNameIgnoreCase("Marine Mammals")
            .orElseThrow();

    Expertise marineBirds = expertiseRepository
            .findByNameIgnoreCase("Marine Birds")
            .orElseThrow();

    Specialist elena = new Specialist();
    elena.setProfessionalCode("PRO-001");
    elena.setFirstName("Elena");
    elena.setLastName("Vargas");
    elena.setEmail("elena.vargas@deepblue.com");
    elena.setActive(true);
    elena.addExpertise(trauma);
    elena.addExpertise(rehabilitation);

    Specialist mateo = new Specialist();
    mateo.setProfessionalCode("PRO-002");
    mateo.setFirstName("Mateo");
    mateo.setLastName("Gomez");
    mateo.setEmail("mateo.gomez@deepblue.com");
    mateo.setActive(true);
    mateo.addExpertise(marineMammals);
    mateo.addExpertise(rehabilitation);

    Specialist sofia = new Specialist();
    sofia.setProfessionalCode("PRO-003");
    sofia.setFirstName("Sofia");
    sofia.setLastName("Rodriguez");
    sofia.setEmail("sofia.rodriguez@deepblue.com");
    sofia.setActive(true);
    sofia.addExpertise(marineBirds);
    sofia.addExpertise(trauma);

    specialistRepository.saveAll(
            List.of(elena, mateo, sofia)
    );

    List<Specialist> results =
            specialistRepository.findActiveByExpertise("Trauma");

    assertThat(results)
            .extracting(Specialist::getFirstName)
            .containsExactly("Elena", "Sofia");
}

@Test
void shouldCreateTreatments() {

    Animal animal = new Animal();
    animal.setAnimalCode("AN-2026-003");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    animalRepository.save(animal);

    Specialist elena = new Specialist();
    elena.setProfessionalCode("PRO-001");
    elena.setFirstName("Elena");
    elena.setLastName("Vargas");
    elena.setEmail("elena.vargas@deepblue.com");
    elena.setActive(true);

    Specialist mateo = new Specialist();
    mateo.setProfessionalCode("PRO-002");
    mateo.setFirstName("Mateo");
    mateo.setLastName("Gomez");
    mateo.setEmail("mateo.gomez@deepblue.com");
    mateo.setActive(true);

    specialistRepository.saveAll(List.of(elena, mateo));

    Treatment treatment1 = new Treatment();
    treatment1.setAnimal(animal);
    treatment1.setSpecialist(elena);
    treatment1.setPerformedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
    treatment1.setType(TreatmentType.WOUND_CARE);
    treatment1.setDescription("Wound care");

    Treatment treatment2 = new Treatment();
    treatment2.setAnimal(animal);
    treatment2.setSpecialist(elena);
    treatment2.setPerformedAt(LocalDateTime.of(2026, 9, 1, 12, 0));
    treatment2.setType(TreatmentType.HYDRATION);
    treatment2.setDescription("Hydration");

    Treatment treatment3 = new Treatment();
    treatment3.setAnimal(animal);
    treatment3.setSpecialist(mateo);
    treatment3.setPerformedAt(LocalDateTime.of(2026, 9, 1, 14, 0));
    treatment3.setType(TreatmentType.OBSERVATION);
    treatment3.setDescription("Observation");

    treatmentRepository.saveAll(
            List.of(treatment1, treatment2, treatment3)
    );

    assertThat(treatmentRepository.count())
            .isEqualTo(3);
}

@Test
void shouldFindTreatmentsByAnimalOrderedChronologically() {

    Animal animal = new Animal();
    animal.setAnimalCode("AN-2026-004");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    animalRepository.save(animal);

    Specialist specialist = new Specialist();
    specialist.setProfessionalCode("PRO-004");
    specialist.setFirstName("Elena");
    specialist.setLastName("Vargas");
    specialist.setEmail("elena.vargas4@deepblue.com");
    specialist.setActive(true);

    specialistRepository.save(specialist);

    Treatment treatment1 = new Treatment();
    treatment1.setAnimal(animal);
    treatment1.setSpecialist(specialist);
    treatment1.setPerformedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
    treatment1.setType(TreatmentType.WOUND_CARE);

    Treatment treatment2 = new Treatment();
    treatment2.setAnimal(animal);
    treatment2.setSpecialist(specialist);
    treatment2.setPerformedAt(LocalDateTime.of(2026, 9, 1, 12, 0));
    treatment2.setType(TreatmentType.HYDRATION);

    Treatment treatment3 = new Treatment();
    treatment3.setAnimal(animal);
    treatment3.setSpecialist(specialist);
    treatment3.setPerformedAt(LocalDateTime.of(2026, 9, 1, 14, 0));
    treatment3.setType(TreatmentType.OBSERVATION);

    treatmentRepository.saveAll(
            List.of(treatment1, treatment2, treatment3)
    );

    List<Treatment> results =
            treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(
                    animal.getId()
            );

    assertThat(results)
            .hasSize(3);

    assertThat(results.get(0).getType())
            .isEqualTo(TreatmentType.WOUND_CARE);

    assertThat(results.get(1).getType())
            .isEqualTo(TreatmentType.HYDRATION);

    assertThat(results.get(2).getType())
            .isEqualTo(TreatmentType.OBSERVATION);
}

@Test
void shouldFindTreatmentsByDateInterval() {

    Animal animal = new Animal();
    animal.setAnimalCode("AN-2026-005");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    animalRepository.save(animal);

    Specialist specialist = new Specialist();
    specialist.setProfessionalCode("PRO-005");
    specialist.setFirstName("Elena");
    specialist.setLastName("Vargas");
    specialist.setEmail("elena.vargas5@deepblue.com");
    specialist.setActive(true);

    specialistRepository.save(specialist);

    Treatment treatment1 = new Treatment();
    treatment1.setAnimal(animal);
    treatment1.setSpecialist(specialist);
    treatment1.setPerformedAt(LocalDateTime.of(2026, 8, 1, 10, 0));
    treatment1.setType(TreatmentType.WOUND_CARE);

    Treatment treatment2 = new Treatment();
    treatment2.setAnimal(animal);
    treatment2.setSpecialist(specialist);
    treatment2.setPerformedAt(LocalDateTime.of(2026, 8, 10, 10, 0));
    treatment2.setType(TreatmentType.HYDRATION);

    Treatment treatment3 = new Treatment();
    treatment3.setAnimal(animal);
    treatment3.setSpecialist(specialist);
    treatment3.setPerformedAt(LocalDateTime.of(2026, 8, 20, 10, 0));
    treatment3.setType(TreatmentType.OBSERVATION);

    treatmentRepository.saveAll(
            List.of(treatment1, treatment2, treatment3)
    );

    List<Treatment> results =
            treatmentRepository.findByPerformedAtBetween(
                    LocalDateTime.of(2026, 8, 5, 0, 0),
                    LocalDateTime.of(2026, 8, 15, 23, 59)
            );

    assertThat(results)
            .hasSize(1);

    assertThat(results.get(0).getPerformedAt())
            .isEqualTo(LocalDateTime.of(2026, 8, 10, 10, 0));
}

@Test
void shouldRejectDuplicateAnimalCode() {

    Animal animal1 = new Animal();
    animal1.setAnimalCode("AN-100");
    animal1.setCommonName("Green Sea Turtle");
    animal1.setScientificName("Chelonia mydas");
    animal1.setSex(AnimalSex.UNKNOWN);

    animalRepository.saveAndFlush(animal1);

    Animal animal2 = new Animal();
    animal2.setAnimalCode("AN-100");
    animal2.setCommonName("Hawksbill Turtle");
    animal2.setScientificName("Eretmochelys imbricata");
    animal2.setSex(AnimalSex.UNKNOWN);

    assertThatThrownBy(() ->
            animalRepository.saveAndFlush(animal2)
    ).isInstanceOf(DataIntegrityViolationException.class);
}

@Test
void shouldRejectRescueCaseWithNonexistentRescueCenter() {

    assertThatThrownBy(() -> jdbcTemplate.update(
            """
            INSERT INTO rescue_cases
                (case_code, rescue_date, rescue_location, status, rescue_center_id)
            VALUES (?, ?, ?, ?, ?)
            """,
            "RES-INVALID-CENTER",
            LocalDate.of(2026, 9, 28),
            "Santa Marta",
            "ADMITTED",
            -99999L
    )).isInstanceOf(DataIntegrityViolationException.class);
}

@Test
void shouldRejectRescueCaseWithStatusOutsideCheckConstraint() {

    RescueCenter center = new RescueCenter();
    center.setCode("DB-CHECK");
    center.setName("Check Constraint Test Center");
    center.setCity("Santa Marta");
    RescueCenter savedCenter = rescueCenterRepository.saveAndFlush(center);

    assertThatThrownBy(() -> jdbcTemplate.update(
            """
            INSERT INTO rescue_cases
                (case_code, rescue_date, rescue_location, status, rescue_center_id)
            VALUES (?, ?, ?, ?, ?)
            """,
            "RES-INVALID-STATUS",
            LocalDate.of(2026, 9, 28),
            "Santa Marta",
            "INVALID_STATUS",
            savedCenter.getId()
    )).isInstanceOf(DataIntegrityViolationException.class);
}

}

/*
.
.
.
.
BASICAMENTE QUEDAMOS EN EL PASO 63.
.
.
.
.
.
.
*/
