ALTER TABLE animals
    ADD CONSTRAINT chk_animals_sex
    CHECK (sex IN ('MALE', 'FEMALE', 'UNKNOWN'));

ALTER TABLE treatments
    ADD CONSTRAINT chk_treatments_type
    CHECK (type IN (
        'WOUND_CARE',
        'HYDRATION',
        'MEDICATION',
        'SURGERY',
        'NUTRITION',
        'PHYSIOTHERAPY',
        'OBSERVATION'
    ));
