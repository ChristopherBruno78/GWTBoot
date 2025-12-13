package ${package}.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class AuthController {
  private final SecurityContextRepository securityContextRepository;
    private final SecurityUrlProperties securityUrlProperties;

  public AuthController(SecurityContextRepository securityContextRepository, SecurityUrlProperties securityProperties) {
    this.securityContextRepository = securityContextRepository;
    this.securityUrlProperties = securityProperties;
  }

  @GetMapping({ "/signin", "/signin/" })
  public String signIn(
    @RequestParam(defaultValue = "false") boolean error,
    Model model
  ) {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken) {
      if (error) {
        model.addAttribute("error", "true");
      }
      return "auth/signin";
    }
    return "redirect:" + securityUrlProperties.getDefaultSuccessUrl();
  }
}
