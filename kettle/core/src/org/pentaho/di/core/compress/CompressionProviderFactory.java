package org.pentaho.di.core.compress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

public class CompressionProviderFactory implements CompressionProviderFactoryInterface {

  protected static CompressionProviderFactory INSTANCE = new CompressionProviderFactory();

  private CompressionProviderFactory() {
  }

  public static CompressionProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public CompressionProvider createCompressionProviderInstance( String name ) {

    CompressionProvider provider = null;

    List<PluginInterface> providers = getPlugins();
    if ( providers != null ) {
      for ( PluginInterface plugin : providers ) {
        if ( name != null && name.equalsIgnoreCase( plugin.getName() ) ) {
          try {
            return PluginRegistry.getInstance().loadClass( plugin, CompressionProvider.class );
          } catch ( Exception e ) {
            provider = null;
          }
        }
      }
    }

    return provider;
  }

  @Override
  public Collection<CompressionProvider> getCompressionProviders() {
    Collection<CompressionProvider> providerClasses = new ArrayList<CompressionProvider>();

    List<PluginInterface> providers = getPlugins();
    if ( providers != null ) {
      for ( PluginInterface plugin : providers ) {
        try {
          providerClasses.add( PluginRegistry.getInstance().loadClass( plugin, CompressionProvider.class ) );
        } catch ( Exception e ) {
          // Do nothing here, if we can't load the provider, don't add it to the list
        }
      }
    }
    return providerClasses;
  }

  @Override
  public String[] getCompressionProviderNames() {
    ArrayList<String> providerNames = new ArrayList<String>();

    List<PluginInterface> providers = getPlugins();
    if ( providers != null ) {
      for ( PluginInterface plugin : providers ) {
        try {
          CompressionProvider provider = PluginRegistry.getInstance().loadClass( plugin, CompressionProvider.class );
          if ( provider != null ) {
            providerNames.add( provider.getName() );
          }
        } catch ( Exception e ) {
          // Do nothing here, if we can't load the provider, don't add it to the list
        }
      }
    }
    return providerNames.toArray( new String[providerNames.size()] );
  }

  @Override
  public CompressionProvider getCompressionProviderByName( String name ) {
    if ( name == null ) {
      return null;
    }
    CompressionProvider foundProvider = null;
    List<PluginInterface> providers = getPlugins();
    if ( providers != null ) {
      for ( PluginInterface plugin : providers ) {
        try {
          CompressionProvider provider = PluginRegistry.getInstance().loadClass( plugin, CompressionProvider.class );
          if ( provider != null && name.equals( provider.getName() ) ) {
            foundProvider = provider;
          }
        } catch ( Exception e ) {
          // Do nothing here, if we can't load the provider, don't add it to the list
        }
      }
    }
    return foundProvider;
  }

  protected List<PluginInterface> getPlugins() {
    return PluginRegistry.getInstance().getPlugins( CompressionPluginType.class );
  }
}
