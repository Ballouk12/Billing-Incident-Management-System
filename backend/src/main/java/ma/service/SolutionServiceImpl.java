package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.SolutionDTO;
import ma.entity.Incident;
import ma.entity.Solution;
import ma.entity.User;
import ma.repository.jpa.IncidentRepository;
import ma.repository.jpa.SolutionRepository;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.AuditLogService;
import ma.service.interfaces.SolutionService;
import org.modelmapper.ModelMapper;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    public SolutionDTO createSolution(SolutionDTO solutionDTO) {
        log.debug("Creating new solution for incident: {}", solutionDTO.getIncidentId());

        // Récupérer l'incident
        Incident incident = incidentRepository.findById(solutionDTO.getIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        // Récupérer l'utilisateur
        User createdBy = userRepository.findById(solutionDTO.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Solution solution = Solution.builder()
                .incident(incident)
                .createdBy(createdBy)
                .description(solutionDTO.getDescription())
                .solutionType(solutionDTO.getSolutionType())
                .comments(solutionDTO.getComments())
                .validated(false)
                .build();

        Solution savedSolution = solutionRepository.save(solution);

        // Log d'audit
        auditLogService.logAction(incident, "SOLUTION_ADDED",
                "Solution ajoutée par " + createdBy.getUsername());

        log.info("Solution created successfully for incident: {}", incident.getId());

        return mapToDTO(savedSolution);
    }

    @Override
    public SolutionDTO updateSolution(Long id, SolutionDTO solutionDTO) {
        log.debug("Updating solution with ID: {}", id);

        Solution existingSolution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        existingSolution.setDescription(solutionDTO.getDescription());
        existingSolution.setSolutionType(solutionDTO.getSolutionType());
        existingSolution.setComments(solutionDTO.getComments());

        Solution updatedSolution = solutionRepository.save(existingSolution);

        log.info("Solution updated successfully: {}", id);
        return mapToDTO(updatedSolution);
    }

    @Override
    @Transactional(readOnly = true)
    public SolutionDTO getSolutionById(Long id) {
        log.debug("Fetching solution with ID: {}", id);

        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        return mapToDTO(solution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolutionDTO> getSolutionsByIncidentId(Long incidentId) {
        log.debug("Fetching solutions for incident: {}", incidentId);

        List<Solution> solutions = solutionRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);

        return solutions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSolution(Long id) {
        log.debug("Deleting solution with ID: {}", id);

        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solutionRepository.delete(solution);

        log.info("Solution deleted successfully: {}", id);
    }

    @Override
    public SolutionDTO validateSolution(Long id) {
        log.debug("Validating solution with ID: {}", id);

        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solution.setValidated(true);
        Solution validatedSolution = solutionRepository.save(solution);

        // Log d'audit
        auditLogService.logAction(solution.getIncident(), "SOLUTION_VALIDATED",
                "Solution ID " + id + " validée");

        log.info("Solution validated successfully: {}", id);
        return mapToDTO(validatedSolution);
    }

    private SolutionDTO mapToDTO(Solution solution) {
        SolutionDTO dto = modelMapper.map(solution, SolutionDTO.class);
        dto.setIncidentId(solution.getIncident().getId());
        dto.setCreatedById(solution.getCreatedBy().getId());
        dto.setCreatedByUsername(solution.getCreatedBy().getUsername());
        return dto;
    }
}