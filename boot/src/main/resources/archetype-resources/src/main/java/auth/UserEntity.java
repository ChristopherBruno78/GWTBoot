package ${package}.auth;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Users")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  @Column(unique = true, nullable = false)
  protected String username;

  @Column(nullable = false)
  protected String password;

  @Column(nullable = false)
  protected Date dateCreated;

  @Column(nullable = true)
  protected Date lastLogin;

  @Column(nullable = false)
  protected Boolean isActive = true;

  public UserEntity() {
    dateCreated = new Date();
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public Boolean isActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }
}
