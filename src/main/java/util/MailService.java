package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.*;
import java.util.stream.Collectors;
import model.Task;

public class MailService {
    public static void sendTaskReminder(String toEmail, List<Task> tasks) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            // Use your Gmail address and app password here
            final String username = "quocthien049@gmail.com"; // replace with your Gmail
            final String password = "nbmh eotu nqjt cjxc";    // replace with your Gmail App Password

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@yourdomain.com")); // visible sender
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Task Reminder");
            String body = tasks == null || tasks.isEmpty()
                ? "You have no tasks today!"
                : tasks.stream().map(Task::toString).collect(java.util.stream.Collectors.joining("\n"));
            message.setText(body);
            Transport.send(message);
            System.out.println("Reminder sent to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
