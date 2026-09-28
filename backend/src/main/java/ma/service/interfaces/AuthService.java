package ma.service.interfaces;

import ma.dto.LoginRequest;
import ma.dto.LoginResponse;
import ma.dto.UserDTO;

public interface AuthService {
    LoginResponse authenticate(LoginRequest loginRequest);
    UserDTO register(UserDTO userDTO);
    LoginResponse refreshToken(String token);
}
