package ma.service.interfaces;

import ma.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO getUserById(Long id);
    Page<UserDTO> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
    List<UserDTO> getUsersByRole(String role);
    UserDTO activateUser(Long id);
    UserDTO deactivateUser(Long id);
    String resetPassword(Long id);
}
