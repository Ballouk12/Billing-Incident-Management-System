package ma.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import ma.dto.LoginRequest;
import ma.dto.LoginResponse;
import ma.dto.UserDTO;
import ma.entity.Role;
import ma.entity.User;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class AuthenticationSteps {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private LoginResponse loginResponse;
    private Exception thrownException;
    private UserDTO createdUser;

    @Given("un utilisateur {string} existe avec le mot de passe {string}")
    public void unUtilisateurExisteAvecLeMotDePasse(String username, String password) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode(password))
                .role(Role.TECHNICIEN)
                .active(true)
                .build();

        userRepository.save(user);
    }

    @Given("un utilisateur {string} existe")
    public void unUtilisateurExiste(String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.TECHNICIEN)
                .active(true)
                .build();

        userRepository.save(user);
    }

    @When("il se connecte avec ses identifiants")
    public void ilSeConnecteAvecSesIdentifiants() {
        try {
            LoginRequest request = new LoginRequest("john.doe", "SecurePass123");
            loginResponse = authService.authenticate(request);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("il tente de se connecter avec un mot de passe incorrect")
    public void ilTenteDeSeConnecterAvecUnMotDePasseIncorrect() {
        try {
            LoginRequest request = new LoginRequest("john.doe", "wrongpassword");
            loginResponse = authService.authenticate(request);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("il crée un nouvel utilisateur avec les données:")
    public void ilCreeUnNouvelUtilisateurAvecLesDonnees(Map<String, String> data) {
        UserDTO userDTO = UserDTO.builder()
                .username(data.get("username"))
                .email(data.get("email"))
                .role(data.get("role"))
                .firstName(data.get("firstName"))
                .lastName(data.get("lastName"))
                .password("DefaultPassword123!")
                .build();

        createdUser = authService.register(userDTO);
    }

    @Then("un token JWT est généré")
    public void unTokenJWTEstGenere() {
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertTrue(loginResponse.getToken().length() > 0);
    }

    @Then("le token contient le username et le rôle")
    public void leTokenContientLeUsernameEtLeRole() {
        assertNotNull(loginResponse.getUsername());
        assertNotNull(loginResponse.getRole());
    }

    @Then("la durée de validité est de {int} heures")
    public void laDureeDeValiditeEstDeHeures(int hours) {
        assertNotNull(loginResponse.getExpiresIn());
        long expectedMillis = hours * 60 * 60 * 1000L;
        assertEquals(expectedMillis, loginResponse.getExpiresIn());
    }

    @Then("une réponse de succès est retournée")
    public void uneReponseDeSuccesEstRetournee() {
        assertNotNull(loginResponse);
        assertNull(thrownException);
    }

    @Then("la connexion est refusée")
    public void laConnexionEstRefusee() {
        assertNotNull(thrownException);
    }

    @Then("aucun token n'est généré")
    public void aucunTokenNestGenere() {
        assertNull(loginResponse);
    }

    @Then("un message d'erreur {string} est retourné")
    public void unMessageDerreurEstRetourne(String expectedMessage) {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("invalide") ||
                thrownException.getMessage().contains("Identifiants"));
    }

    @Then("l'utilisateur est créé avec succès")
    public void lutilisateurEstCreeAvecSucces() {
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
    }

    @Then("le mot de passe est chiffré avec BCrypt")
    public void leMotDePasseEstChiffreAvecBCrypt() {
        User user = userRepository.findByUsername(createdUser.getUsername()).orElse(null);
        assertNotNull(user);
        assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"));
    }

    @Then("l'utilisateur est actif par défaut")
    public void lutilisateurEstActifParDefaut() {
        assertTrue(createdUser.getActive());
    }
}