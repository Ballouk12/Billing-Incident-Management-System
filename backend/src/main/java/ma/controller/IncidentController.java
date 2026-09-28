package ma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.ApiResponse;
import ma.dto.IncidentDTO;
import ma.dto.SolutionDTO;
import ma.service.interfaces.IncidentService;
import ma.service.interfaces.SolutionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class IncidentController {

    private final IncidentService incidentService;
    private final SolutionService solutionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<IncidentDTO>> createIncident(
            @Valid @RequestBody IncidentDTO incidentDTO) {
        log.info("Creating new incident");
        IncidentDTO created = incidentService.createIncident(incidentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident créé avec succès", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<Page<IncidentDTO>>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Fetching incidents - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IncidentDTO> incidents = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncidentById(@PathVariable Long id) {
        log.info("Fetching incident with ID: {}", id);
        IncidentDTO incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<IncidentDTO>> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentDTO incidentDTO) {
        log.info("Updating incident with ID: {}", id);
        IncidentDTO updated = incidentService.updateIncident(id, incidentDTO);
        return ResponseEntity.ok(ApiResponse.success("Incident mis à jour avec succès", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteIncident(@PathVariable Long id) {
        log.info("Deleting incident with ID: {}", id);
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident supprimé avec succès", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<Page<IncidentDTO>>> searchIncidents(
            @RequestParam(required = false) String incidentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching incidents with filters");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<IncidentDTO> incidents = incidentService.searchIncidents(
                incidentType, status, assignedToId, startDate, endDate, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<IncidentDTO>> assignIncident(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("Assigning incident {} to user {}", id, userId);
        IncidentDTO assigned = incidentService.assignIncident(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Incident assigné avec succès", assigned));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<IncidentDTO>> resolveIncident(@PathVariable Long id) {
        log.info("Resolving incident with ID: {}", id);
        IncidentDTO resolved = incidentService.resolveIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident résolu avec succès", resolved));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        log.info("Fetching incident statistics");
        Map<String, Long> stats = incidentService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}/solutions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<List<SolutionDTO>>> getIncidentSolutions(@PathVariable Long id) {
        log.info("Fetching solutions for incident: {}", id);
        List<SolutionDTO> solutions = solutionService.getSolutionsByIncidentId(id);
        return ResponseEntity.ok(ApiResponse.success(solutions));
    }

    @PostMapping("/{id}/solutions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'TECHNICIEN')")
    public ResponseEntity<ApiResponse<SolutionDTO>> addSolution(
            @PathVariable Long id,
            @Valid @RequestBody SolutionDTO solutionDTO) {
        log.info("Adding solution to incident: {}", id);
        log.info("XXXXXXXXXXXXXXXXX le dto est sous cette forme XXXXXXXXXXXXXXXXXXXXXXXXXXX : {}",solutionDTO);
        solutionDTO.setIncidentId(id);
        SolutionDTO created = solutionService.createSolution(solutionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solution ajoutée avec succès", created));
    }
}
