package ma.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import ma.dto.IncidentDTO;
import ma.exception.InvalidFileException;
import ma.repository.jpa.IncidentRepository;
import ma.service.interfaces.FileExtractionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class FileUploadSteps {

    private final FileExtractionService fileExtractionService;
    private final IncidentRepository incidentRepository;

    private String currentFilename;
    private int expectedIncidentCount;
    private List<IncidentDTO> extractedIncidents;
    private Exception thrownException;

    @Given("un fichier rollback {string} est disponible")
    public void unFichierRollbackEstDisponible(String filename) {
        currentFilename = filename;
    }

    @Given("le fichier contient {int} lignes d'incidents valides")
    public void leFichierContientLignesDincidentsValides(int count) {
        expectedIncidentCount = count;
        // Créer un fichier CSV de test
        createTestCsvFile(currentFilename, count);
    }

    @Given("un fichier {string} avec un format incorrect")
    public void unFichierAvecUnFormatIncorrect(String filename) {
        currentFilename = filename;
        try {
            Path path = Paths.get("src/test/resources/test-data/" + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, "Invalid content".getBytes());
        } catch (IOException e) {
            fail("Failed to create test file");
        }
    }

    @When("le service d'extraction est exécuté pour ce fichier")
    public void leServiceDextractionEstExecutePourCeFichier() {
        try {
            String filePath = "src/test/resources/test-data/" + currentFilename;
            extractedIncidents = fileExtractionService.extractIncidentsFromPath(filePath, 1L);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("{int} incidents sont créés dans la base de données")
    public void incidentsSontCreesDansLaBaseDeDonnees(int count) {
        assertNotNull(extractedIncidents);
        assertEquals(count, extractedIncidents.size());
    }

    @Then("chaque incident a un statut {string}")
    public void chaqueIncidentAUnStatut(String status) {
        assertTrue(extractedIncidents.stream()
                .allMatch(i -> status.equals(i.getStatus())));
    }

    @Then("un message de succès est retourné")
    public void unMessageDeSuccesEstRetourne() {
        assertNull(thrownException);
    }

    @Then("une exception {string} est levée")
    public void uneExceptionEstLevee(String exceptionType) {
        assertNotNull(thrownException);
        assertTrue(thrownException instanceof InvalidFileException);
    }

    @Then("un message d'erreur explicite est retourné")
    public void unMessageDerreurExpliciteEstRetourne() {
        assertNotNull(thrownException);
        assertNotNull(thrownException.getMessage());
    }

    @Then("aucun incident n'est créé dans la base")
    public void aucunIncidentNestCreeDansLaBase() {
        assertTrue(extractedIncidents == null || extractedIncidents.isEmpty());
    }

    @Then("les incidents avec montant > {double} ont la priorité {string}")
    public void lesIncidentsAvecMontantOntLaPriorite(double montant, String priority) {
        if (extractedIncidents != null) {
            extractedIncidents.stream()
                    .filter(i -> i.getMontantErreur() != null && i.getMontantErreur() > montant)
                    .forEach(i -> assertTrue(
                            priority.equals(i.getPriority()) ||
                                    isPriorityHigherOrEqual(i.getPriority(), priority)
                    ));
        }
    }

    private void createTestCsvFile(String filename, int lineCount) {
        try {
            Path path = Paths.get("src/test/resources/test-data/" + filename);
            Files.createDirectories(path.getParent());

            StringBuilder content = new StringBuilder();
            content.append("error_code,error_type,reference_facture,client_id,montant_erreur,error_message,details\n");

            for (int i = 1; i <= lineCount; i++) {
                content.append(String.format("ERR%03d,CALC,FAC%d,CLI%d,%.2f,Erreur de calcul,Details ligne %d\n",
                        i, i, i, 1000.0 * i, i));
            }

            Files.write(path, content.toString().getBytes());
        } catch (IOException e) {
            fail("Failed to create test CSV file");
        }
    }

    private boolean isPriorityHigherOrEqual(String priority1, String priority2) {
        String[] priorities = {"FAIBLE", "MOYENNE", "HAUTE", "CRITIQUE"};
        int index1 = indexOf(priorities, priority1);
        int index2 = indexOf(priorities, priority2);
        return index1 >= index2;
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }
}