@Entity
@Table(name = "rescue_centers")
public class RescueCenter {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @OneToMany(mappedBy = "rescueCenter")
    private List<RescueCase> cases = new ArrayList<>();

    public void addCase(RescueCase rescueCase) {
        cases.add(rescueCase);
        rescueCase.setRescueCenter(this);
    }
}