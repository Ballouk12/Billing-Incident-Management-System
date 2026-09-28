error id: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/listener/IncidentEntityListener.java:_empty_/ElasticsearchIndexService#updateIndex#
file:///D:/Users/HP/Desktop/backend/src/main/java/ma/listener/IncidentEntityListener.java
empty definition using pc, found symbol in pc: _empty_/ElasticsearchIndexService#updateIndex#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1300
uri: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/listener/IncidentEntityListener.java
text:
```scala
package ma.listener;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import ma.entity.Incident;
import ma.service.interfaces.ElasticsearchIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IncidentEntityListener {

    private static ElasticsearchIndexService elasticsearchIndexService;

    @Autowired
    public void setElasticsearchIndexService(ElasticsearchIndexService service) {
        IncidentEntityListener.elasticsearchIndexService = service;
    }

    @PostPersist
    public void onPostPersist(Incident incident) {
        log.debug("PostPersist event for incident: {}", incident.getId());
        if (elasticsearchIndexService != null) {
            try {
                elasticsearchIndexService.indexIncident(incident);
            } catch (Exception e) {
                log.error("Failed to index incident on persist: {}", e.getMessage());
            }
        }
    }

    @PostUpdate
    public void onPostUpdate(Incident incident) {
        log.debug("PostUpdate event for incident: {}", incident.getId());
        if (elasticsearchIndexService != null) {
            try {
                elasticsearchIndexService.@@updateIndex(incident);
            } catch (Exception e) {
                log.error("Failed to update incident index: {}", e.getMessage());
            }
        }
    }

    @PostRemove
    public void onPostRemove(Incident incident) {
        log.debug("PostRemove event for incident: {}", incident.getId());
        if (elasticsearchIndexService != null) {
            try {
                elasticsearchIndexService.deleteFromIndex(incident.getId());
            } catch (Exception e) {
                log.error("Failed to delete incident from index: {}", e.getMessage());
            }
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ElasticsearchIndexService#updateIndex#