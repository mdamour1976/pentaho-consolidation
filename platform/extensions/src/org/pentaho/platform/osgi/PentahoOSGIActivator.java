package org.pentaho.platform.osgi;

import org.osgi.framework.BundleContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * Standard OSGI Activator class which is called when the OSGI environment is started. Work to integrate the OSGI
 * container with the PentahoSystem is started from this class
 */
public class PentahoOSGIActivator {

  private Logger logger = LoggerFactory.getLogger(getClass());

  public void setBundleContext( BundleContext bundleContext ) throws Exception {
    logger.debug("Registering OSGIObjectFactory");

    OSGIObjectFactory objectFactory = new OSGIObjectFactory( bundleContext );
    PentahoSystem.registerObjectFactory( objectFactory );
    logger.debug("OSGIObjectFactory installed");

  }

}
