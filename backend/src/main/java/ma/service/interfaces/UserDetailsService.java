package ma.service.interfaces;

import ma.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface UserDetailsService {

    public UserDetails loadUserByUsername(String username) ;

    public Collection<? extends GrantedAuthority> getAuthorities(User user) ;
}
