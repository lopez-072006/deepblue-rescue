import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "case_code", nullable = false, unique = true, length = 50)
    private String caseCode;

    @Column(name = "rescue_date", nullable = false)
    private LocalDate rescueDate;

    @Column(name = "rescue_location", nullable = false, length = 150)
    private String rescueLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RescueStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_center_id", nullable = false)
    private RescueCenter rescueCenter;

    @OneToOne(
        mappedBy = "rescueCase",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )

    private Animal animal;

    public void assignAnimal(Animal animal) {
        this.animal = animal;
        animal.setRescueCase(this);
    }

}