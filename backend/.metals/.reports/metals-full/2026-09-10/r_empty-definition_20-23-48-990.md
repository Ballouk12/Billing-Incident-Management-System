error id: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuthServiceImpl.java:org/springframework/security/core/userdetails/UserDetailsService#
file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuthServiceImpl.java
empty definition using pc, found symbol in pc: org/springframework/security/core/userdetails/UserDetailsService#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 700
uri: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuthServiceImpl.java
text:
```scala
package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.LoginRequest;
import ma.dto.LoginResponse;
import ma.dto.UserDTO;
import ma.entity.User;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.AuthService;
import ma.service.interfaces.UserService;
import ma.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.@@UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authentication attempt for user: {}", loginRequest.getUsername());

        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Récupérer l'utilisateur
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier si l'utilisateur est actif
        if (!user.getActive()) {
            throw new RuntimeException("User account is disabled");
        }

        // Générer le token JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        log.info("User authenticated successfully: {}", loginRequest.getUsername());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    @Override
    public UserDTO register(UserDTO userDTO) {
        log.info("Registration attempt for user: {}", userDTO.getUsername());

        // Utiliser le UserService pour créer l'utilisateur
        return userService.createUser(userDTO);
    }

    @Override
    public LoginResponse refreshToken(String token) {
        log.debug("Token refresh attempt");

        // Extraire le token Bearer
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Extraire le username du token
        String username = jwtUtil.extractUsername(token);

        // Charger les détails de l'utilisateur
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Valider le token
        if (!jwtUtil.isTokenValid(token, userDetails)) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Générer un nouveau token
        String newToken = jwtUtil.generateToken(userDetails);

        // Récupérer l'utilisateur
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .token(newToken)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/security/core/userdetails/UserDetailsService#