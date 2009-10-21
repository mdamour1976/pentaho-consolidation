package org.pentaho.reporting.platform.plugin;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.io.UnsupportedEncodingException;

import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.libraries.base.config.Configuration;

public class PentahoReportEnvironment extends DefaultReportEnvironment
{
  public PentahoReportEnvironment(final Configuration configuration)
  {
    super(configuration);
  }

  /**
   * Returns the text encoding that should be used to encode URLs.
   *
   * @return the encoding for URLs.
   */
  public String getURLEncoding()
  {
    return LocaleHelper.getSystemEncoding();
  }

  public String getEnvironmentProperty(final String key)
  {
    if (key == null)
    {
      throw new NullPointerException();
    }

    if (PentahoSystem.getApplicationContext() == null) {
      return key;
    }
    
    final String pentahoBaseURL = PentahoSystem.getApplicationContext().getBaseUrl();

    String property = null;
    if ("serverBaseURL".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      property = getBaseServerURL(pentahoBaseURL);
    } 
    else if ("pentahoBaseURL".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      property = pentahoBaseURL;
    } 
    else if ("solutionRoot".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      property = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
    } 
    else if ("hostColonPort".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      property = getHostColonPort(pentahoBaseURL);
    } 
    else if ("username".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      property = PentahoSessionHolder.getSession().getName();
    } 
    else if ("roles".equalsIgnoreCase(key)) //$NON-NLS-1$
    {
      final IUserDetailsRoleListService roleListService = PentahoSystem.getUserDetailsRoleListService();
      //noinspection unchecked
      final List<String> roles =
          (List<String>) roleListService.getRolesForUser(PentahoSessionHolder.getSession().getName());
      if (roles != null && roles.size() > 0)
      {
        property = roles.get(0);
        for (int i = 1; i < roles.size(); i++)
        {
          property += ", " + roles.get(i); //$NON-NLS-1$
        }
      }
    }

    if (property == null)
    {
      return super.getEnvironmentProperty(key);
    } 
    else
    {
      return property;
    }
  }

  private String getBaseServerURL(final String pentahoBaseURL)
  {
    try
    {
      final URL url = new URL(pentahoBaseURL);
      return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (Exception e)
    {
      // ignored
    }
    return pentahoBaseURL;
  }

  private String getHostColonPort(final String pentahoBaseURL)
  {
    try
    {
      final URL url = new URL(pentahoBaseURL);
      return url.getHost() + ":" + url.getPort();//$NON-NLS-1$ 
    }
    catch (Exception e)
    {
      // ignored 
    }
    return pentahoBaseURL;
  }

  public Locale getLocale()
  {
    return LocaleHelper.getLocale();
  }
}
