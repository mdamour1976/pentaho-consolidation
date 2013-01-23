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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.scheduler2.email;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

public class Emailer {

  private static final Log logger = LogFactory.getLog(Emailer.class);
  private static final String MAILER = "smtpsend"; //$NON-NLS-1$

  private Properties props = new Properties();
  private InputStream attachment = null;
  private String attachmentName = null;
  private String attachmentMimeType = null;
  private Authenticator authenticator = null;

  public Emailer() {
  }

  public void setTo(String to) {
    if (to != null && !"".equals(to)) {
      props.put("to", to);
    }
  }

  public void setCc(String cc) {
    if (cc != null && !"".equals(cc)) {
      props.put("cc", cc);
    }
  }

  public void setBcc(String bcc) {
    if (bcc != null && !"".equals(bcc)) {
      props.put("bcc", bcc);
    }
  }

  public void setSubject(String subject) {
    props.put("subject", subject);
  }

  public void setFrom(String from) {
    props.put("mail.from.default", from);
  }

  public void setFromName(String fromName) {
    props.put("mail.from.name", fromName);
  }

  public void setUseAuthentication(boolean useAuthentication) {
    props.put("mail.smtp.auth", "" + useAuthentication);
  }

  public void setSmtpHost(String smtpHost) {
    props.put("mail.smtp.host", smtpHost);
  }

  public void setSmtpPort(int port) {
    props.put("mail.smtp.port", "" + port);
  }

  public void setTransportProtocol(String protocol) {
    props.put("mail.transport.protocol", protocol);
  }

  public void setUseSSL(boolean useSSL) {
    props.put("mail.smtp.ssl", "" + useSSL);
  }

  public void setStartTLS(boolean startTLS) {
    props.put("mail.smtp.starttls.enable", "" + startTLS);
  }

  public void setQuitWait(boolean quitWait) {
    props.put("mail.smtp.quitwait", "" + quitWait);
  }

  public void setAttachment(InputStream attachment) {
    this.attachment = attachment;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentMimeType(String mimeType) {
    this.attachmentMimeType = mimeType;
  }

  public Authenticator getAuthenticator() {
    return authenticator;
  }

  public void setAuthenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
  }

  public void setBody(String body) {
    props.put("body", body);
  }

  public boolean setup() {
    try {
      Document configDocument = PentahoSystem.getSystemSettings().getSystemSettingsDocument("smtp-email/email_config.xml"); //$NON-NLS-1$
      List<?> properties = configDocument.selectNodes("/email-smtp/properties/*"); //$NON-NLS-1$
      Iterator<?> propertyIterator = properties.iterator();
      while (propertyIterator.hasNext()) {
        Node propertyNode = (Node) propertyIterator.next();
        String propertyName = propertyNode.getName();
        String propertyValue = propertyNode.getText();
        props.put(propertyName, propertyValue);
      }
      props.put("mail.from.default", PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.from.default", ""));
      return true;
    } catch (Exception e) {
      logger.error("Email.ERROR_0013_CONFIG_FILE_INVALID", e); //$NON-NLS-1$
    }
    return false;
  }

  public boolean send() {
    String from = props.getProperty("mail.from.default");
    String fromName = props.getProperty("mail.from.name");
    String to = props.getProperty("to");
    String cc = props.getProperty("cc");
    String bcc = props.getProperty("bcc");
    boolean authenticate = "true".equalsIgnoreCase(props.getProperty("mail.smtp.auth"));
    String subject = props.getProperty("subject");
    String body = props.getProperty("body");

    logger.info("Going to send an email to " + to + " from " + from + " with the subject '" + subject + "' and the body " + body);

    try {
      // Get a Session object
      Session session;

      if (authenticate) {
        if (authenticator == null) {
          authenticator = new EmailAuthenticator();
        }
        session = Session.getInstance(props, authenticator);
      } else {
        session = Session.getInstance(props);
      }

      // if debugging is not set in the email config file, then default to false
      if (!props.containsKey("mail.debug")) { //$NON-NLS-1$
        session.setDebug(false);
      }

      // construct the message
      MimeMessage msg = new MimeMessage(session);
      Multipart multipart = new MimeMultipart();

      if (from != null) {
        msg.setFrom(new InternetAddress(from, fromName));
      } else {
        // There should be no way to get here
        logger.error("Email.ERROR_0012_FROM_NOT_DEFINED"); //$NON-NLS-1$
      }

      if ((to != null) && (to.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
      }
      if ((cc != null) && (cc.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
      }
      if ((bcc != null) && (bcc.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
      }

      if (subject != null) {
        msg.setSubject(subject, LocaleHelper.getSystemEncoding());
      }

      if (attachment == null) {
        logger.error("Email.ERROR_0015_ATTACHMENT_FAILED"); //$NON-NLS-1$
        return false;
      }

      ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment, attachmentMimeType);

      if (body != null) {
        MimeBodyPart bodyMessagePart = new MimeBodyPart();
        bodyMessagePart.setText(body, LocaleHelper.getSystemEncoding());
        multipart.addBodyPart(bodyMessagePart);
      }

      // attach the file to the message
      MimeBodyPart attachmentBodyPart = new MimeBodyPart();
      attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
      attachmentBodyPart.setFileName(attachmentName);
      multipart.addBodyPart(attachmentBodyPart);

      // add the Multipart to the message
      msg.setContent(multipart);

      msg.setHeader("X-Mailer", Emailer.MAILER); //$NON-NLS-1$
      msg.setSentDate(new Date());

      Transport.send(msg);

      return true;
    } catch (SendFailedException e) {
      logger.error("Email.ERROR_0011_SEND_FAILED -" + to, e); //$NON-NLS-1$
    } catch (AuthenticationFailedException e) {
      logger.error("Email.ERROR_0014_AUTHENTICATION_FAILED - " + to, e); //$NON-NLS-1$
    } catch (Throwable e) {
      logger.error("Email.ERROR_0011_SEND_FAILED - " + to, e); //$NON-NLS-1$
    }
    return false;
  }

  private class EmailAuthenticator extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      String user = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.userid", null); //$NON-NLS-1$ //$NON-NLS-2$
      String password = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.password", null); //$NON-NLS-1$ //$NON-NLS-2$
      return new PasswordAuthentication(user, password);
    }
  }

}