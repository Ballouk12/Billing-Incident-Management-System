package ma.service.interfaces;

import ma.dto.SolutionDTO;

import java.util.List;

public interface SolutionService {
    SolutionDTO createSolution(SolutionDTO solutionDTO);
    SolutionDTO updateSolution(Long id, SolutionDTO solutionDTO);
    SolutionDTO getSolutionById(Long id);
    List<SolutionDTO> getSolutionsByIncidentId(Long incidentId);
    void deleteSolution(Long id);
    SolutionDTO validateSolution(Long id);
}