@Entity
@Table(name = "expertise")
public class Expertise {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "expertiseAreas")
    private Set<Specialist> specialists = new HashSet<>();
}