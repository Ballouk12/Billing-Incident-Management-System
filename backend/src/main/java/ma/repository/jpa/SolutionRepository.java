package ma.repository.jpa;


import ma.entity.Incident;
import ma.entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {

    List<Solution> findByIncidentOrderByCreatedAtDesc(Incident incident);

    List<Solution> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);

    List<Solution> findByValidatedTrue();

    List<Solution> findByCreatedById(Long userId);
}