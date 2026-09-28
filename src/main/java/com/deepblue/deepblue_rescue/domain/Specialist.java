import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialists")
public class Specialist {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "professional_code", nullable = false, unique = true, length = 50)
    private String professionalCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany
    @JoinTable(
        name = "specialist_expertise",
        joinColumns = @JoinColumn(name = "specialist_id"),
        inverseJoinColumns = @JoinColumn(name = "expertise_id")
    )
    private Set<Expertise> expertiseAreas = new HashSet<>();

    public void addExpertise(Expertise expertise) {
        expertiseAreas.add(expertise);
        expertise.getSpecialists().add(this);
    }

    @OneToMany(mappedBy = "specialist")
    private List<Treatment> treatments = new ArrayList<>();

}