package ma.service.interfaces;

import ma.dto.IncidentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileExtractionService {
    List<IncidentDTO> extractIncidentsFromFile(MultipartFile file, Long userId);
    List<IncidentDTO> extractIncidentsFromPath(String filePath, Long userId);
    void processRollbackDirectory();
}