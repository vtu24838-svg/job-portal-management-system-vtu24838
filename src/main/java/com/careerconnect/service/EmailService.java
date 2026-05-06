package com.careerconnect.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@careerconnect.com}")
    private String fromEmail;

    // ─── Generic HTML email sender ────────────────────────────────────────────

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "CareerConnect");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            mailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (Exception e) {
            log.warn("Could not send email to {} (SMTP not configured?): {}", to, e.getMessage());
            
            // Save mock email to a file for local testing
            try {
                java.nio.file.Path emailDir = java.nio.file.Paths.get("uploads", "emails");
                if (!java.nio.file.Files.exists(emailDir)) {
                    java.nio.file.Files.createDirectories(emailDir);
                }
                String filename = "email_" + to.replace("@", "_at_") + "_" + System.currentTimeMillis() + ".html";
                java.nio.file.Path emailFile = emailDir.resolve(filename);
                java.nio.file.Files.writeString(emailFile, htmlBody);
                log.info(">>> MOCK EMAIL SAVED TO FILE: {}", emailFile.toAbsolutePath());
            } catch (Exception ex) {
                log.error("Failed to save mock email to file", ex);
            }

            // Development fallback — log the notification so nothing is silently lost
            log.info(">>> MOCK EMAIL TO: {}", to);
            log.info(">>> SUBJECT: {}", subject);
            log.info(">>> (HTML body omitted from console — configure SMTP to send real emails)");
        }
    }

    // ─── Shortlist notification ───────────────────────────────────────────────

    @Async
    public void sendShortlistNotification(String to, String applicantName,
                                          String jobTitle, String companyName) {
        String subject = "🎉 Congratulations! You've been shortlisted — " + jobTitle;
        String html = buildEmailHtml(
            applicantName,
            "You've Been Shortlisted! 🎉",
            "#057642",
            "#e8f5e9",
            "Great news! <strong>" + companyName + "</strong> has reviewed your application for " +
            "<strong>" + jobTitle + "</strong> and has shortlisted you for the next stage.",
            "What happens next:",
            new String[]{
                "The hiring team will contact you via email or phone to schedule an interview.",
                "Review the job description once more so you're fully prepared.",
                "Update your CareerConnect profile to showcase your best skills.",
                "Research " + companyName + " before your interview."
            },
            "View My Applications",
            "https://localhost:8080/applicant/applications",
            "#0A66C2"
        );
        sendHtmlEmail(to, subject, html);
    }

    // ─── Rejection notification ───────────────────────────────────────────────

    @Async
    public void sendRejectionNotification(String to, String applicantName,
                                          String jobTitle, String companyName) {
        String subject = "Update on your application — " + jobTitle + " at " + companyName;
        String html = buildEmailHtml(
            applicantName,
            "Application Status Update",
            "#664d03",
            "#fff9e6",
            "Thank you for applying to <strong>" + jobTitle + "</strong> at " +
            "<strong>" + companyName + "</strong>. After careful consideration, " +
            "the team has decided to move forward with other candidates at this time.",
            "Keep going — here's what to do next:",
            new String[]{
                "Don't be discouraged — every rejection is a step closer to the right opportunity.",
                "Browse new job listings on CareerConnect that match your skills.",
                "Strengthen your profile by adding more skills and experience.",
                "Consider uploading an updated resume for better match recommendations."
            },
            "Explore More Jobs",
            "https://localhost:8080/applicant/jobs",
            "#f59e0b"
        );
        sendHtmlEmail(to, subject, html);
    }

    // ─── HTML email template builder ──────────────────────────────────────────

    private String buildEmailHtml(String name, String headline, String accentColor,
                                  String accentBg, String mainMessage,
                                  String tipsHeading, String[] tips,
                                  String ctaText, String ctaUrl, String ctaColor) {
        StringBuilder tipItems = new StringBuilder();
        for (int i = 0; i < tips.length; i++) {
            tipItems.append(
                "<li style=\"padding:8px 0; border-bottom:" +
                (i < tips.length - 1 ? "1px solid #e8e8e8" : "none") +
                "; font-size:14px; color:#444; line-height:1.6;\">" +
                tips[i] + "</li>"
            );
        }

        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head><meta charset=\"UTF-8\"/>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
            "<title>" + headline + "</title></head>" +
            "<body style=\"margin:0;padding:0;background:#f3f2ef;font-family:'Segoe UI',Arial,sans-serif;\">" +

            // Wrapper
            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f3f2ef;padding:40px 16px;\">" +
            "<tr><td align=\"center\">" +
            "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;\">" +

            // Header bar
            "<tr><td style=\"background:linear-gradient(135deg,#0A66C2,#004182);border-radius:16px 16px 0 0;padding:32px 40px;text-align:center;\">" +
            "<div style=\"font-size:28px;font-weight:800;color:white;letter-spacing:-0.5px;\">CareerConnect</div>" +
            "<div style=\"font-size:13px;color:rgba(255,255,255,0.75);margin-top:4px;\">Your Professional Journey Partner</div>" +
            "</td></tr>" +

            // Main card
            "<tr><td style=\"background:white;padding:40px;\">" +

            // Status badge
            "<div style=\"display:inline-block;background:" + accentBg + ";color:" + accentColor + ";" +
            "padding:8px 20px;border-radius:999px;font-size:13px;font-weight:700;" +
            "margin-bottom:24px;\">" + headline + "</div>" +

            // Greeting
            "<h1 style=\"font-size:22px;font-weight:800;color:#1a1a1a;margin:0 0 12px;\">Hello, " + name + "! 👋</h1>" +

            // Main message
            "<p style=\"font-size:15px;color:#444;line-height:1.7;margin:0 0 28px;\">" + mainMessage + "</p>" +

            // Divider
            "<hr style=\"border:none;border-top:2px solid #f0f0f0;margin:0 0 28px;\"/>" +

            // Tips section
            "<div style=\"background:#f8fafc;border-radius:12px;padding:20px 24px;margin-bottom:32px;\">" +
            "<div style=\"font-size:13px;font-weight:700;color:#888;text-transform:uppercase;" +
            "letter-spacing:0.08em;margin-bottom:14px;\">" + tipsHeading + "</div>" +
            "<ul style=\"margin:0;padding:0;list-style:none;\">" + tipItems + "</ul>" +
            "</div>" +

            // CTA button
            "<div style=\"text-align:center;margin-bottom:32px;\">" +
            "<a href=\"" + ctaUrl + "\" style=\"display:inline-block;background:" + ctaColor + ";" +
            "color:white;font-size:15px;font-weight:700;padding:14px 36px;" +
            "border-radius:999px;text-decoration:none;letter-spacing:0.02em;\">" +
            ctaText + " →</a>" +
            "</div>" +

            // Footer inside card
            "<p style=\"font-size:12px;color:#aaa;text-align:center;line-height:1.6;\">" +
            "You received this email because you applied on CareerConnect.<br/>" +
            "© 2026 CareerConnect. All rights reserved." +
            "</p>" +
            "</td></tr>" +

            // Bottom stripe
            "<tr><td style=\"background:#0A66C2;border-radius:0 0 16px 16px;padding:16px 40px;" +
            "text-align:center;\">" +
            "<span style=\"font-size:12px;color:rgba(255,255,255,0.7);\">Powered by CareerConnect Platform</span>" +
            "</td></tr>" +

            "</table></td></tr></table>" +
            "</body></html>";
    }

    // ─── Legacy plain-text helper (kept for backward compat) ─────────────────

    public void sendEmail(String to, String subject, String body) {
        // Wrap in basic HTML and delegate
        String html = "<pre style=\"font-family:sans-serif;\">" + body + "</pre>";
        sendHtmlEmail(to, subject, html);
    }
}
