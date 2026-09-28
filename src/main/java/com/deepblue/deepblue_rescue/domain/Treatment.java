import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatments")
public class Treatment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TreatmentType type;

    @Column(length = 500)
    private String description;
}