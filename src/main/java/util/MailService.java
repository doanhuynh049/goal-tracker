package util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import java.util.stream.Collectors;
import model.Task;

public class MailService {
    public static void sendTaskReminder(String toEmail, List<Task> tasks) throws MessagingException {
        String subject = "\uD83D\uDCCB Your Tasks for Today";
        String body = tasks.isEmpty()
            ? "You have no tasks scheduled today!"
            : tasks.stream().map(Task::toString).collect(Collectors.joining("\n"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your_email@gmail.com", "your_app_password");
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("your_email@gmail.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
