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
            StringBuilder body = new StringBuilder();
            if (tasks == null || tasks.isEmpty()) {
                body.append("You have no tasks scheduled for today!\n");
            } else {
                body.append("<h2 style='color:#2196f3;'>Today's Tasks</h2>");
                body.append("<ul style='font-size:14px;'>");
                for (Task task : tasks) {
                    body.append("<li><b>Title:</b> ").append(task.getTitle()).append("<br>");
                    body.append("<b>Description:</b> ").append(task.getDescription() != null ? task.getDescription() : "-").append("<br>");
                    body.append("<b>Priority:</b> ").append(task.getPriority()).append("<br>");
                    body.append("<b>Start Day:</b> ").append(task.getStartDay() != null ? task.getStartDay() : "-").append("<br>");
                    body.append("<b>Due Date:</b> ").append(task.getDueDate() != null ? task.getDueDate() : "-").append("</li><br>");
                }
                body.append("</ul>");
            }
            message.setContent(body.toString(), "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Reminder sent to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send an email with a PNG image attachment (dashboard)
    public static void sendDashboardWithAttachment(String toEmail, java.io.File imageFile) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            final String username = "quocthien049@gmail.com";
            final String password = "nbmh eotu nqjt cjxc";

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@yourdomain.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Statistics Dashboard Report");

            // Create multipart message
            Multipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(
                "<h2 style='color:#2196f3;'>Goal & Task Management - Statistics Dashboard</h2>" +
                "<p>Please find attached the latest statistics dashboard report for your goals and tasks.</p>" +
                "<p>This report includes:</p>" +
                "<ul>" +
                "<li>Task completion overview</li>" +
                "<li>Goals progress summary</li>" +
                "<li>Task priority distribution</li>" +
                "<li>Goals categorization</li>" +
                "<li>Recent completion timeline</li>" +
                "</ul>" +
                "<p>Best regards,<br>Goal & Task Management System</p>",
                "text/html; charset=utf-8"
            );
            multipart.addBodyPart(textPart);

            // Attachment part
            if (imageFile != null && imageFile.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(imageFile);
                attachmentPart.setFileName("statistics_dashboard.png");
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Dashboard email sent to " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
