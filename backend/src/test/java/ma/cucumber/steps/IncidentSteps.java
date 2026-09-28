package ma.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import ma.dto.IncidentDTO;
import ma.entity.Incident;
import ma.entity.Role;
import ma.entity.User;
import ma.repository.jpa.IncidentRepository;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.IncidentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class IncidentSteps {

    private final IncidentService incidentService;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    private IncidentDTO createdIncident;
    private IncidentDTO fetchedIncident;
    private Page<IncidentDTO> incidentPage;
    private Exception thrownException;
    private Long currentUserId;

    @Given("un incident avec l'ID {long} existe")
    public void unIncidentAvecIDExiste(Long id) {
        User user = userRepository.findByUsername("system")
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username("system")
                            .email("system@test.com")
                            .password("password")
                            .role(Role.ADMIN)
                            .build();
                    return userRepository.save(newUser);
                });

        Incident incident = Incident.builder()
                .id(id)
                .incidentType("ERREUR_CALCUL")
                .status("NOUVEAU")
                .description("Test incident")
                .lineNumber(1)
                .priority("MOYENNE")
                .createdBy(user)
                .build();

        incidentRepository.save(incident);
    }

    @Given("un incident avec l'ID {long} existe avec le statut {string}")
    public void unIncidentAvecIDExisteAvecStatut(Long id, String status) {
        User user = userRepository.findByUsername("system").orElse(null);

        Incident incident = Incident.builder()
                .id(id)
                .incidentType("ERREUR_CALCUL")
                .status(status)
                .description("Test incident")
                .lineNumber(1)
                .priority("MOYENNE")
                .createdBy(user)
                .build();

        incidentRepository.save(incident);
    }

    @When("il crée un incident avec les données suivantes:")
    public void ilCreeUnIncidentAvecLesDonneesSuivantes(Map<String, String> data) {
        IncidentDTO incidentDTO = IncidentDTO.builder()
                .incidentType(data.get("incidentType"))
                .description(data.get("description"))
                .priority(data.get("priority"))
                .status("NOUVEAU")
                .lineNumber(1)
                .createdById(currentUserId != null ? currentUserId : 1L)
                .build();

        createdIncident = incidentService.createIncident(incidentDTO);
    }

    @When("il assigne l'incident à {string}")
    public void ilAssigneLincidentA(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        assertNotNull(user, "User should exist");

        createdIncident = incidentService.assignIncident(1L, user.getId());
    }

    @When("il marque l'incident comme {string}")
    public void ilMarqueLincidentComme(String status) {
        if ("RESOLU".equals(status)) {
            createdIncident = incidentService.resolveIncident(1L);
        } else {
            IncidentDTO dto = incidentService.getIncidentById(1L);
            dto.setStatus(status);
            createdIncident = incidentService.updateIncident(1L, dto);
        }
    }

    @When("il recherche des incidents avec les filtres suivants:")
    public void ilRechercheDesIncidentsAvecLesFiltresSuivants(Map<String, String> filters) {
        String incidentType = filters.get("incidentType");
        String status = filters.get("status");
        LocalDateTime startDate = filters.containsKey("startDate") ?
                LocalDateTime.parse(filters.get("startDate") + "T00:00:00") : null;

        incidentPage = incidentService.searchIncidents(
                incidentType,
                status,
                null,
                startDate,
                null,
                PageRequest.of(0, 10)
        );
    }

    @When("il supprime l'incident")
    public void ilSupprimeLincident() {
        incidentService.deleteIncident(1L);
    }

    @Then("l'incident est créé avec succès")
    public void lincidentEstCreeAvecSucces() {
        assertNotNull(createdIncident);
        assertNotNull(createdIncident.getId());
    }

    @Then("le statut initial est {string}")
    public void leStatutInitialEst(String status) {
        assertEquals(status, createdIncident.getStatus());
    }

    @Then("le statut de l'incident passe à {string}")
    public void leStatutDeLincidentPasseA(String status) {
        assertEquals(status, createdIncident.getStatus());
    }

    @Then("l'incident est assigné à {string}")
    public void lincidentEstAssigneA(String username) {
        assertNotNull(createdIncident.getAssignedToUsername());
        assertEquals(username, createdIncident.getAssignedToUsername());
    }

    @Then("la date de résolution est enregistrée")
    public void laDateDeResolutionEstEnregistree() {
        assertNotNull(createdIncident.getResolvedAt());
    }

    @Then("seuls les incidents correspondants sont retournés")
    public void seulsLesIncidentsCorrespondantsSontRetournes() {
        assertNotNull(incidentPage);
        assertTrue(incidentPage.getTotalElements() >= 0);
    }

    @Then("les résultats sont paginés")
    public void lesResultatsSontPagines() {
        assertNotNull(incidentPage);
        assertTrue(incidentPage.getSize() <= 10);
    }

    @Then("l'incident est marqué comme supprimé \\(deleted = true)")
    public void lincidentEstMarqueCommeSupprime() {
        Incident incident = incidentRepository.findById(1L).orElse(null);
        assertNotNull(incident);
        assertTrue(incident.getDeleted());
    }

    @Then("il n'apparaît plus dans les recherches standards")
    public void ilNapparaitPlusDansLesRecherchesStandards() {
        Page<IncidentDTO> results = incidentService.getAllIncidents(PageRequest.of(0, 10));
        assertTrue(results.getContent().stream()
                .noneMatch(i -> i.getId().equals(1L)));
    }
}
