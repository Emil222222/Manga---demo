package az.manga.demo.common.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String username;
    private boolean isActive;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getPassword() { return null; }
    @Override
    public boolean isAccountNonLocked() { return isActive; }
    @Override
    public boolean isEnabled() {  return isActive; }
}
