import org.springframework.data.jpa.repository.JpaRepository;

public interface RescueCenterRepository extends JpaRepository<RescueCenter, Long> {
    Optional<RescueCenter> findByCode(String code);
}