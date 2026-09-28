@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "animal_id",
        nullable = false,
        unique = true
    )
    private Animal animal;

    @Column(name = "initial_weight", nullable = false, precision = 6, scale = 2)
    private BigDecimal initialWeight;

    @Column(name = "initial_condition", nullable = false, length = 100)
    private String initialCondition;

    @Column(length = 500)
    private String injuries;

    @Column(length = 500)
    private String observations;
}