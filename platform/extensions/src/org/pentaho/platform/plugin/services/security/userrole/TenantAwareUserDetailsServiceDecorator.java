package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * User: nbaker
 * Date: 8/1/13
 */
public class TenantAwareUserDetailsServiceDecorator implements UserDetailsService {

  private final UserDetailsService delegate;
  private final ITenantedPrincipleNameResolver nameResolver;
  public TenantAwareUserDetailsServiceDecorator(UserDetailsService delegate, ITenantedPrincipleNameResolver nameResolver){
    this.delegate = delegate;
    this.nameResolver = nameResolver;
  }

  @Override
  public UserDetails loadUserByUsername(final String s) throws UsernameNotFoundException, DataAccessException {
    return this.delegate.loadUserByUsername(extractUsername(s));
  }
  private String extractUsername(final String composite){
    return nameResolver.getPrincipleName(composite);
  }
}
