package ${package}.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.urls")
public class SecurityUrlProperties {
  private String signIn;
  private String signOut;
  private String defaultSuccessUrl;

  private List<String> secured;



  public List<String> getSecured() {
    return secured;
  }

  public void setSecured(List<String> secured) {
    this.secured = secured;
  }

  public String getSignIn() {
    return signIn;
  }

  public void setSignIn(String signIn) {
    this.signIn = signIn;
  }

  public String getSignOut() {
    return signOut;
  }

  public void setSignOut(String signOut) {
    this.signOut = signOut;
  }

  public String getDefaultSuccessUrl() {
      return defaultSuccessUrl;
  }

  public void setDefaultSuccessUrl(String defaultSuccessUrl) {
      this.defaultSuccessUrl = defaultSuccessUrl;
  }

}
