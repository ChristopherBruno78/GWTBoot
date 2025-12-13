package ${package}.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsService
  implements org.springframework.security.core.userdetails.UserDetailsService {
  private final UserRepository userRepository;

  public UserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username)
    throws UsernameNotFoundException {
    final Optional<UserEntity> oUserEntity = userRepository.findByUsername(
      username
    );
    if (oUserEntity.isPresent()) {
      UserEntity userEntity = oUserEntity.get();
      return User
        .withUsername(userEntity.getUsername())
        .password(userEntity.getPassword())
        .build();
    }
    throw new BadCredentialsException("Invalid username or password.");
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
