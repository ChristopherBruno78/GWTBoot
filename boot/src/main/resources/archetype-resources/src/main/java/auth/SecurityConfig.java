package ${package}.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final AuthProvider authenticationProvider;
  private final UserDetailsService userDetailsService;
  private final SecurityUrlProperties securityUrlProperties;

  public SecurityConfig(
    AuthProvider authenticationProvider,
    UserDetailsService userDetailsService,
    SecurityUrlProperties securityUrlProperties
  ) {
    this.authenticationProvider = authenticationProvider;
    this.userDetailsService = userDetailsService;
    this.securityUrlProperties = securityUrlProperties;
  }

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    http
      .csrf(
        csrf ->
          csrf.ignoringRequestMatchers(
            "/h2-console/**",
            securityUrlProperties.getSignIn(),
            securityUrlProperties.getSignOut()
          )
      ) // Disable CSRF for H2 console
      .authorizeHttpRequests(
        requests ->
          requests
            .requestMatchers(
              securityUrlProperties.getSecured().toArray(new String[0])
            )
            .authenticated()
            .anyRequest()
            .permitAll()
      )
      .authenticationProvider(authenticationProvider)
      .userDetailsService(userDetailsService)
      .formLogin(
        formConfig ->
          formConfig
            .loginPage(securityUrlProperties.getSignIn())
                  .defaultSuccessUrl(securityUrlProperties.getDefaultSuccessUrl())
            .failureUrl(securityUrlProperties.getSignIn() + "?error=true")
      )
      .logout(
        logoutConfig ->
          logoutConfig
            .logoutUrl(securityUrlProperties.getSignOut())
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
      )
      .sessionManagement(
        sessionManagement ->
          sessionManagement
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
      )
      .headers(
        headers -> {
          headers.frameOptions(
            HeadersConfigurer.FrameOptionsConfig::sameOrigin
          );
        }
      );
    return http.build();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }
}
