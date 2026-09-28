package ma.service.interfaces;

import ma.entity.Incident;

import java.util.Map;

public interface ElasticsearchIndexService {
    void indexIncident(Incident incident);
    void updateIndex(Incident incident);
    void deleteFromIndex(Long incidentId);
    void reindexAllIncidents();
    Map<String, Object> getDashboardData();
}