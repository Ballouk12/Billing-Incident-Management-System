package ma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.ApiResponse;
import ma.service.interfaces.ElasticsearchIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    private final ElasticsearchIndexService elasticsearchIndexService;

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reindexAllIncidents() {
        log.info("Reindexing all incidents to Elasticsearch");

        try {
            elasticsearchIndexService.reindexAllIncidents();
            return ResponseEntity.ok(
                    ApiResponse.success("Réindexation terminée avec succès", null)
            );
        } catch (Exception e) {
            log.error("Reindexing failed: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Erreur lors de la réindexation"));
        }
    }

    @GetMapping("/dashboard-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardData() {
        log.info("Fetching dashboard data");

        try {
            Map<String, Object> dashboardData = elasticsearchIndexService.getDashboardData();
            return ResponseEntity.ok(ApiResponse.success(dashboardData));
        } catch (Exception e) {
            log.error("Error fetching dashboard data: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Erreur lors de la récupération des données"));
        }
    }
}