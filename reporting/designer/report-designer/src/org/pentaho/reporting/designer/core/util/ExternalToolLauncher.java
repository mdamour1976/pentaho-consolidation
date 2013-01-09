/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.designer.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.stanford.ejalbert.BrowserLauncher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.designer.core.settings.ExternalToolSettings;
import org.pentaho.reporting.designer.core.settings.SettingsUtil;
import org.pentaho.reporting.designer.core.util.exceptions.UncaughtExceptionsModel;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * User: Martin Date: 01.03.2006 Time: 17:55:15
 */
public class ExternalToolLauncher
{
  private static final Log logger = LogFactory.getLog(ExternalToolLauncher.class);

  private ExternalToolLauncher()
  {
  }


  public static void openURL(final String url) throws IOException
  {
    //noinspection ConstantConditions
    if (url == null)
    {
      throw new IllegalArgumentException("url must not be null");
    }

    final ExternalToolSettings instance = ExternalToolSettings.getInstance();
    if (instance.isUseDefaultBrowser())
    {
      try
      {
        new BrowserLauncher().openURLinBrowser(url);
      }
      catch (Exception e)
      {
        UncaughtExceptionsModel.getInstance().addException(e);
      }
    }
    else
    {
      execute(instance.getCustomBrowserExecutable(),
          instance.getCustomBrowserParameters(), url);
    }
  }


  public static void openPDF(final File file) throws IOException
  {
    //noinspection ConstantConditions
    if (file == null)
    {
      throw new IllegalArgumentException("file must not be null");
    }

    final ExternalToolSettings toolSettings = ExternalToolSettings.getInstance();
    if (toolSettings.isUseDefaultPDFViewer())
    {
      openDefaultViewer(file);
    }
    else
    {
      execute
          (toolSettings.getCustomPDFViewerExecutable(),
              toolSettings.getCustomPDFViewerParameters(),
              file.getCanonicalPath());
    }
  }


  public static void openXLS(final File file) throws IOException
  {
    //noinspection ConstantConditions
    if (file == null)
    {
      throw new IllegalArgumentException("file must not be null");
    }

    final ExternalToolSettings toolSettings = ExternalToolSettings.getInstance();
    if (toolSettings.isUseDefaultXLSViewer())
    {
      try
      {
        openDefaultViewer(file);
      }
      catch (Exception e)
      {
        UncaughtExceptionsModel.getInstance().addException(e);
      }
    }
    else
    {
      execute(
          toolSettings.getCustomXLSViewerExecutable(),
          toolSettings.getCustomXLSViewerParameters(),
          file.getCanonicalPath());
    }
  }


  public static void openRTF(final File file) throws IOException
  {
    //noinspection ConstantConditions
    if (file == null)
    {
      throw new IllegalArgumentException("file must not be null");
    }

    final ExternalToolSettings toolSettings = ExternalToolSettings.getInstance();
    if (toolSettings.isUseDefaultRTFViewer())
    {
      openDefaultViewer(file);
    }
    else
    {
      execute(
          toolSettings.getCustomRTFViewerExecutable(),
          toolSettings.getCustomRTFViewerParameters(),
          file.getCanonicalPath());
    }
  }


  public static void openCSV(final File file) throws IOException
  {
    //noinspection ConstantConditions
    if (file == null)
    {
      throw new IllegalArgumentException("file must not be null");
    }

    final ExternalToolSettings toolSettings = ExternalToolSettings.getInstance();
    if (toolSettings.isUseDefaultCSVViewer())
    {
      openDefaultViewer(file);
    }
    else
    {
      execute(toolSettings.getCustomCSVViewerExecutable(),
          toolSettings.getCustomCSVViewerParameters(),
          file.getCanonicalPath());
    }
  }

  public static boolean execute(final String executable, final String parameters, final String file)
      throws IOException
  {
    // todo: Use a stream tokenizer (well, a custom one, as the builtin one messes up escaped quotes)
    // so that we can handle quoting gracefully ..
    final ArrayList<String> command = new ArrayList<String>();
    command.add(executable);
    for (StringTokenizer tokenizer = new StringTokenizer(parameters); tokenizer.hasMoreTokens();)
    {
      String s = tokenizer.nextToken();
      s = s.replace("{0}", file);
      command.add(s);
    }

    final String osname = safeSystemGetProperty("os.name", "<protected by system security>"); // NON-NLS
    if (StringUtils.startsWithIgnoreCase(osname, "Mac OS X")) // NON-NLS
    {
      logger.debug("Assuming Mac-OS X."); // NON-NLS
      if (executable.endsWith(".app") || executable.endsWith(".app/")) // NON-NLS
      {
        command.add(0, "-a"); // NON-NLS
        command.add(0, "/usr/bin/open"); // NON-NLS
      }
    }

    final Process process = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
    try
    {
      return (process.exitValue() == 0);
    }
    catch (Exception e)
    {
      // process still runs, so assume everything is fine.
      return true;
    }
  }

  protected static String safeSystemGetProperty(final String name,
                                                final String defaultValue)
  {
    try
    {
      return System.getProperty(name, defaultValue);
    }
    catch (final SecurityException se)
    {
      // ignore exception
      return defaultValue;
    }
  }

  public static void openDefaultViewer(final File file) throws IOException
  {

    final String osname = safeSystemGetProperty("os.name", "<protected by system security>"); // NON-NLS
    final String jrepath = safeSystemGetProperty("java.home", "."); // NON-NLS
    final String fs = safeSystemGetProperty("file.separator", File.separator); // NON-NLS

    logger.debug("Running on operating system: " + osname); // NON-NLS

    if (StringUtils.startsWithIgnoreCase(osname, "windows")) // NON-NLS
    {
      logger.debug("Detected Windows."); // NON-NLS
      if (execute("rundll32.exe", "SHELL32.DLL,ShellExec_RunDLL {0}", file.getAbsolutePath()) == false) // NON-NLS
      {
        openURL(file.toURI().toURL().toString());
      }
    }
    else if (StringUtils.startsWithIgnoreCase(osname, "Mac OS X")) // NON-NLS
    {
      logger.debug("Assuming Mac-OS X."); // NON-NLS
      if (execute("/usr/bin/open", "{0}", file.getCanonicalPath()) == false) // NON-NLS
      {
        openURL(file.toURI().toURL().toString());
      }
    }
    else if (StringUtils.startsWithIgnoreCase(osname, "Linux") || // NON-NLS
        StringUtils.startsWithIgnoreCase(osname, "Solaris") || // NON-NLS
        StringUtils.startsWithIgnoreCase(osname, "HP-UX") || // NON-NLS
        StringUtils.startsWithIgnoreCase(osname, "AIX") || // NON-NLS
        StringUtils.startsWithIgnoreCase(osname, "SunOS")) // NON-NLS

    {
      logger.debug("Assuming unix."); // NON-NLS
      final File mailcapExe = new File("/usr/bin/run-mailcap"); // NON-NLS
      if (mailcapExe.exists())
      {
        if (execute("/usr/bin/run-mailcap", "{0}", file.getCanonicalPath()) == false) // NON-NLS
        {
          openURL(file.toURI().toURL().toString());
        }
      }
      else
      {
        // use our private version instead ...
        final File installDir = SettingsUtil.computeInstallationDirectory();
        if (installDir == null)
        {
          logger.debug("Cannot determine installation directory; using browser as generic launcher."); // NON-NLS
          openURL(file.toURI().toURL().toString());
          return;
        }

        final File privateMailCapExe = new File(installDir, "resources/run-mailcap"); // NON-NLS
        if (privateMailCapExe.exists())
        {
          if (execute(privateMailCapExe.getPath(), "{0}", file.getCanonicalPath()) == false)
          {
            openURL(file.toURI().toURL().toString());
          }
        }
        else
        {
          logger.debug("private copy of run-mailcap not found; using browser as generic launcher."); // NON-NLS
          openURL(file.toURI().toURL().toString());
          return;
        }
      }
    }
    else
    {
      logger.debug("Not a known OS-Type; using browser as generic launcher."); // NON-NLS
      openURL(file.toURI().toURL().toString());
    }

  }

}
