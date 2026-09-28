package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.UserDTO;
import ma.entity.Role;
import ma.entity.User;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        log.debug("Creating new user: {}", userDTO.getUsername());

        // Vérifier si username existe déjà
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Vérifier si email existe déjà
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userDTO, User.class);

        // Encoder le mot de passe
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            // Mot de passe par défaut
            user.setPassword(passwordEncoder.encode("ChangeMe123!"));
        }

        // Définir le rôle
        user.setRole(Role.valueOf(userDTO.getRole()));
        user.setActive(true);

        User savedUser = userRepository.save(user);

        log.info("User created successfully: {}", savedUser.getUsername());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.debug("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Mettre à jour les champs modifiables
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFirstName() != null) {
            existingUser.setFirstName(userDTO.getFirstName());
        }

        if (userDTO.getLastName() != null) {
            existingUser.setLastName(userDTO.getLastName());
        }

        if (userDTO.getRole() != null) {
            existingUser.setRole(Role.valueOf(userDTO.getRole()));
        }

        // Mettre à jour le mot de passe si fourni
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully: {}", id);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setPassword(null); // Ne jamais retourner le mot de passe

        return userDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);

        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            dto.setPassword(null);
            return dto;
        });
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);

        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(String role) {
        log.debug("Fetching users with role: {}", role);

        Role roleEnum = Role.valueOf(role);
        List<User> users = userRepository.findByRole(roleEnum);

        return users.stream()
                .map(user -> {
                    UserDTO dto = modelMapper.map(user, UserDTO.class);
                    dto.setPassword(null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO activateUser(Long id) {
        log.debug("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(true);
        User updated = userRepository.save(user);

        log.info("User activated: {}", id);
        return modelMapper.map(updated, UserDTO.class);
    }

    @Override
    public UserDTO deactivateUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(false);
        User updated = userRepository.save(user);

        log.info("User deactivated: {}", id);
        return modelMapper.map(updated, UserDTO.class);
    }

    @Override
    public String resetPassword(Long id) {
        log.debug("Resetting password for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Générer un mot de passe temporaire
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));

        userRepository.save(user);

        log.info("Password reset for user: {}", id);

        // Retourner le mot de passe en clair (à envoyer par email)
        return tempPassword;
    }

    private String generateTemporaryPassword() {
        // Générer un mot de passe aléatoire sécurisé
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "Temp" + uuid + "!";
    }
}