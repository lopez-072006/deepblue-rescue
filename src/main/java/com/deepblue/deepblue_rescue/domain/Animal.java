@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "animal_code", nullable = false, unique = true, length = 50)
    private String animalCode;

    @Column(name = "common_name", nullable = false, length = 100)
    private String commonName;

    @Column(name = "scientific_name", nullable = false, length = 150)
    private String scientificName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnimalSex sex;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "rescue_case_id",
        nullable = false,
        unique = true
    )
    private RescueCase rescueCase;

    @OneToOne(
        mappedBy = "animal",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
        private MedicalRecord medicalRecord;

    public void assignMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
        medicalRecord.setAnimal(this);
    }

    @OneToMany(mappedBy = "animal")
    private List<Treatment> treatments = new ArrayList<>();

    @Column(name = "tracking_device_code", length = 50, unique = true)
    private String trackingDeviceCode;

}