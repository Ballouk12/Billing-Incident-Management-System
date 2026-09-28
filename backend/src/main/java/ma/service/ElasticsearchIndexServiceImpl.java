package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.entity.Incident;
import ma.repository.jpa.IncidentRepository;
import ma.repository.elasticsearch.IncidentElasticsearchRepository;
import ma.service.interfaces.ElasticsearchIndexService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexServiceImpl implements ElasticsearchIndexService {

    private final IncidentElasticsearchRepository elasticsearchRepository;
    private final IncidentRepository incidentRepository;
    @Override
    public void indexIncident(Incident incident) {
        try {
            elasticsearchRepository.save(incident);
            log.info("Incident {} indexed successfully in Elasticsearch", incident.getId());
        } catch (Exception e) {
            log.error("Failed to index incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    @Override
    public void updateIndex(Incident incident) {
        indexIncident(incident); // Même opération pour ES
    }

    @Override
    public void deleteFromIndex(Long incidentId) {
        try {
            elasticsearchRepository.deleteById(incidentId);
            log.info("Incident {} deleted from Elasticsearch", incidentId);
        } catch (Exception e) {
            log.error("Failed to delete incident {} from ES: {}", incidentId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void reindexAllIncidents() {
        log.info("Starting full reindexing of incidents");

        List<Incident> incidents = incidentRepository.findAll();

        for (Incident incident : incidents) {
            indexIncident(incident);
        }

        log.info("Reindexing completed: {} incidents", incidents.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData() {
        log.debug("Fetching dashboard data from Elasticsearch");

        Map<String, Object> data = new HashMap<>();

        // Statistiques par type
        List<Incident> allIncidents = (List<Incident>) elasticsearchRepository.findAll();
        Map<String, Long> byType = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getIncidentType, Collectors.counting()));
        data.put("byType", byType);

        // Statistiques par statut
        Map<String, Long> byStatus = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
        data.put("byStatus", byStatus);

        // Statistiques par priorité
        Map<String, Long> byPriority = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getPriority, Collectors.counting()));
        data.put("byPriority", byPriority);

        // Total
        data.put("total", allIncidents.size());

        return data;
    }
}