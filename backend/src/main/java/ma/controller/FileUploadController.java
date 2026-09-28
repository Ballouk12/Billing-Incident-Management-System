package ma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.ApiResponse;
import ma.dto.FileUploadResponse;
import ma.dto.IncidentDTO;
import ma.service.interfaces.FileExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileUploadController {

    private final FileExtractionService fileExtractionService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadRollbackFile(
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading rollback file: {}", file.getOriginalFilename());

        try {
            // Récupérer l'utilisateur authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getUserIdFromAuthentication(authentication);

            // Extraire les incidents
            List<IncidentDTO> incidents = fileExtractionService.extractIncidentsFromFile(file, userId);

            FileUploadResponse response = FileUploadResponse.builder()
                    .filename(file.getOriginalFilename())
                    .incidentsExtracted(incidents.size())
                    .incidentsCreated(incidents.size())
                    .status("SUCCESS")
                    .message("Fichier traité avec succès")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Fichier uploadé et traité avec succès", response));

        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage());
            FileUploadResponse response = FileUploadResponse.builder()
                    .filename(file.getOriginalFilename())
                    .status("ERROR")
                    .message("Erreur: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors du traitement du fichier"));
        }
    }

    @PostMapping("/process-directory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processRollbackDirectory() {
        log.info("Processing rollback directory");

        try {
            fileExtractionService.processRollbackDirectory();
            return ResponseEntity.ok(
                    ApiResponse.success("Répertoire traité avec succès", null)
            );
        } catch (Exception e) {
            log.error("Error processing directory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du traitement du répertoire"));
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Cette méthode devrait extraire l'ID utilisateur du token JWT
        // Pour l'instant, retourne null (à implémenter avec JWT)
        return 1L; // Placeholder
    }
}

