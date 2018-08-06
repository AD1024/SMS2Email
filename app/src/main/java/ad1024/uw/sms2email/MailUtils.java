package ad1024.uw.sms2email;

import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtils {
    public static MimeMessage createNewEmail(Session session, String from, String title,
                                             String content, String to) {
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from, "SMS2Email", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public static void sendEmail(String from, String password, Session session, MimeMessage mail) throws Exception {
        Transport transport = session.getTransport();
        transport.connect(from, password);
        transport.sendMessage(mail, mail.getAllRecipients());
        transport.close();
    }
}
