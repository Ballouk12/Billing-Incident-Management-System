package ma.repository.elasticsearch;

import ma.entity.Incident;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentElasticsearchRepository extends ElasticsearchRepository<Incident, Long> {
    List<Incident> findByIncidentType(String incidentType);
    List<Incident> findByStatus(String status);
    List<Incident> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Incident> findByDescriptionContaining(String keyword);
}