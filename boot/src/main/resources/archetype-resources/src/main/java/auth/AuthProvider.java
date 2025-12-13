package ${package}.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class AuthProvider implements AuthenticationProvider {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private final Logger logger = Logger.getLogger(AuthProvider.class.getName());
  private final UserDetailsService userDetailsService;

  public AuthProvider(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    UserDetailsService userDetailsService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Authentication authenticate(Authentication authentication)
    throws AuthenticationException {
    logger.info("Authenticating user");
    UserDetails user = userDetailsService.loadUserByUsername(
      authentication.getName()
    );
    if (user != null) {
      if (
        passwordEncoder.matches(
          authentication.getCredentials().toString(),
          user.getPassword()
        )
      ) {
        logger.info("Authentication successful");
        return new UsernamePasswordAuthenticationToken(
          user,
          user.getPassword(),
          Collections.singleton(new SimpleGrantedAuthority("Auth"))
        );
      }
    }
    logger.info("Authentication failed");
    throw new BadCredentialsException("Invalid username or password.");
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  public UserEntity activeUser() {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();
    if (authentication instanceof UsernamePasswordAuthenticationToken) {
      User user = (User) authentication.getPrincipal();
      if (user != null) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByUsername(
          user.getUsername()
        );
        if (optionalUserEntity.isPresent()) {
          return optionalUserEntity.get();
        }
      }
    }
    return null;
  }
}
