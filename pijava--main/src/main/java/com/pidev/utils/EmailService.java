package com.pidev.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Email service using Gmail SMTP.
 *
 * SETUP (one-time):
 *  1. Go to https://myaccount.google.com/apppasswords
 *  2. Create an App Password for "Mail" + "Windows Computer"
 *  3. Replace SENDER_EMAIL and SENDER_APP_PASSWORD below with your values.
 *
 * The service sends emails on a background thread so the UI never freezes.
 */
public class EmailService {

    // ── Configuration — change these to your Gmail credentials ──────────────
    private static final String SENDER_EMAIL    = "ademjazi472@gmail.com";
    private static final String SENDER_APP_PASSWORD = "wpnv kcwp yfgr aftz"; // 16-char App Password
    // ────────────────────────────────────────────────────────────────────────

    private static EmailService instance;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "email-thread");
        t.setDaemon(true);
        return t;
    });

    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    /**
     * Send an email asynchronously.
     *
     * @param toEmail   recipient address
     * @param subject   email subject
     * @param htmlBody  HTML content of the email
     * @param onSuccess callback run on success (may be null)
     * @param onError   callback run on failure with the error message (may be null)
     */
    public void sendAsync(String toEmail, String subject, String htmlBody,
                          Runnable onSuccess, java.util.function.Consumer<String> onError) {
        executor.submit(() -> {
            try {
                send(toEmail, subject, htmlBody);
                if (onSuccess != null) javafx.application.Platform.runLater(onSuccess);
            } catch (Exception e) {
                System.err.println("Email error: " + e.getMessage());
                if (onError != null)
                    javafx.application.Platform.runLater(() -> onError.accept(e.getMessage()));
            }
        });
    }

    /**
     * Send synchronously (blocking). Use sendAsync for UI calls.
     */
    public void send(String toEmail, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();

        // ── Try STARTTLS on port 587 first, fallback to SSL on 465 ──────────
        props.put("mail.smtp.auth",                "true");
        props.put("mail.smtp.starttls.enable",     "true");
        props.put("mail.smtp.starttls.required",   "true");
        props.put("mail.smtp.host",                "smtp.gmail.com");
        props.put("mail.smtp.port",                "587");
        props.put("mail.smtp.ssl.trust",           "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols",       "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout",   "10000");  // 10s connect
        props.put("mail.smtp.timeout",             "10000");  // 10s read
        props.put("mail.smtp.writetimeout",        "10000");  // 10s write
        props.put("mail.debug",                    "false");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            sendWithSession(session, toEmail, subject, htmlBody);
        } catch (MessagingException e) {
            // Fallback: try SSL on port 465
            System.err.println("⚠️ STARTTLS failed, trying SSL port 465: " + e.getMessage());
            Properties sslProps = new Properties();
            sslProps.put("mail.smtp.auth",                "true");
            sslProps.put("mail.smtp.ssl.enable",          "true");
            sslProps.put("mail.smtp.host",                "smtp.gmail.com");
            sslProps.put("mail.smtp.port",                "465");
            sslProps.put("mail.smtp.ssl.trust",           "smtp.gmail.com");
            sslProps.put("mail.smtp.ssl.protocols",       "TLSv1.2 TLSv1.3");
            sslProps.put("mail.smtp.connectiontimeout",   "10000");
            sslProps.put("mail.smtp.timeout",             "10000");
            sslProps.put("mail.smtp.writetimeout",        "10000");

            Session sslSession = Session.getInstance(sslProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
                }
            });
            sendWithSession(sslSession, toEmail, subject, htmlBody);
        }
    }

    private void sendWithSession(Session session, String toEmail, String subject, String htmlBody)
            throws MessagingException {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "PiDev Medical", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Encoding error: " + e.getMessage(), e);
        }
    }

    // ── Pre-built email templates ────────────────────────────────────────────

    /**
     * Sends a password-reset OTP email.
     */
    public void sendPasswordResetOTP(String toEmail, String firstName, String otp,
                                     Runnable onSuccess, java.util.function.Consumer<String> onError) {
        String subject = "PiDev Medical — Réinitialisation de mot de passe";
        String body = buildOTPEmailHtml(firstName, otp);
        sendAsync(toEmail, subject, body, onSuccess, onError);
    }

    /**
     * Sends a welcome email after registration.
     */
    public void sendWelcomeEmail(String toEmail, String firstName,
                                 Runnable onSuccess, java.util.function.Consumer<String> onError) {
        String subject = "Bienvenue sur PiDev Medical !";
        String body = buildWelcomeEmailHtml(firstName);
        sendAsync(toEmail, subject, body, onSuccess, onError);
    }

    /**
     * Sends an appointment confirmation email.
     */
    public void sendAppointmentConfirmation(String toEmail, String patientName, 
                                           String doctorName, String date, String time,
                                           Runnable onSuccess, java.util.function.Consumer<String> onError) {
        String subject = "Confirmation de Rendez-vous — PiDev Medical";
        String body = buildAppointmentConfirmationHtml(patientName, doctorName, date, time);
        sendAsync(toEmail, subject, body, onSuccess, onError);
    }

    /**
     * Sends an appointment cancellation email.
     */
    public void sendAppointmentCancellation(String toEmail, String patientName, 
                                           String doctorName, String date, String time,
                                           Runnable onSuccess, java.util.function.Consumer<String> onError) {
        String subject = "Annulation de Rendez-vous — PiDev Medical";
        String body = buildAppointmentCancellationHtml(patientName, doctorName, date, time);
        sendAsync(toEmail, subject, body, onSuccess, onError);
    }

    // ── HTML Templates ───────────────────────────────────────────────────────

    private String buildOTPEmailHtml(String firstName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#080b14;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#080b14;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#0e1220;border-radius:14px;border:1px solid #252d42;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:#141826;padding:32px 40px;text-align:center;
                                 border-bottom:1px solid #252d42;">
                        <div style="font-size:32px;margin-bottom:8px;">⚕</div>
                        <div style="color:#f1f5f9;font-size:20px;font-weight:700;">PiDev Medical</div>
                        <div style="color:#475569;font-size:13px;margin-top:4px;">Réinitialisation de mot de passe</div>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="color:#94a3b8;font-size:15px;margin:0 0 8px 0;">
                          Bonjour <strong style="color:#f1f5f9;">%s</strong>,
                        </p>
                        <p style="color:#94a3b8;font-size:14px;margin:0 0 28px 0;line-height:1.6;">
                          Vous avez demandé la réinitialisation de votre mot de passe.<br>
                          Utilisez le code ci-dessous. Il est valable <strong style="color:#f1f5f9;">10 minutes</strong>.
                        </p>

                        <!-- OTP Box -->
                        <div style="background:#141826;border:1px solid #252d42;border-radius:12px;
                                    padding:28px;text-align:center;margin-bottom:28px;">
                          <div style="color:#475569;font-size:12px;font-weight:600;
                                      letter-spacing:2px;margin-bottom:12px;">CODE DE VÉRIFICATION</div>
                          <div style="color:#818cf8;font-size:40px;font-weight:700;
                                      letter-spacing:12px;font-family:monospace;">%s</div>
                        </div>

                        <p style="color:#475569;font-size:12px;margin:0;line-height:1.6;">
                          Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.<br>
                          Votre mot de passe ne sera pas modifié.
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#080b14;padding:20px 40px;text-align:center;
                                 border-top:1px solid #252d42;">
                        <div style="color:#475569;font-size:11px;">
                          © 2026 PiDev Medical — Tous droits réservés
                        </div>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(firstName, otp);
    }

    private String buildWelcomeEmailHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#080b14;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#080b14;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#0e1220;border-radius:14px;border:1px solid #252d42;overflow:hidden;">

                    <tr>
                      <td style="background:#141826;padding:32px 40px;text-align:center;
                                 border-bottom:1px solid #252d42;">
                        <div style="font-size:32px;margin-bottom:8px;">⚕</div>
                        <div style="color:#f1f5f9;font-size:20px;font-weight:700;">PiDev Medical</div>
                        <div style="color:#475569;font-size:13px;margin-top:4px;">Bienvenue !</div>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="color:#94a3b8;font-size:15px;margin:0 0 16px 0;">
                          Bonjour <strong style="color:#f1f5f9;">%s</strong> 👋
                        </p>
                        <p style="color:#94a3b8;font-size:14px;margin:0 0 24px 0;line-height:1.6;">
                          Votre compte PiDev Medical a été créé avec succès.<br>
                          Vous pouvez maintenant accéder au forum communautaire,
                          consulter vos rendez-vous et gérer votre profil.
                        </p>
                        <div style="background:#141826;border:1px solid rgba(91,110,245,0.3);
                                    border-radius:10px;padding:16px 20px;">
                          <div style="color:#818cf8;font-size:13px;font-weight:600;">
                            Plateforme de gestion médicale
                          </div>
                          <div style="color:#475569;font-size:12px;margin-top:4px;">
                            Forum · Rendez-vous · Profil · Spécialités
                          </div>
                        </div>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#080b14;padding:20px 40px;text-align:center;
                                 border-top:1px solid #252d42;">
                        <div style="color:#475569;font-size:11px;">
                          © 2026 PiDev Medical — Tous droits réservés
                        </div>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(firstName);
    }

    private String buildAppointmentConfirmationHtml(String patientName, String doctorName, 
                                                    String date, String time) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#080b14;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#080b14;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#0e1220;border-radius:14px;border:1px solid #252d42;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:#141826;padding:32px 40px;text-align:center;
                                 border-bottom:1px solid #252d42;">
                        <div style="font-size:32px;margin-bottom:8px;">📅</div>
                        <div style="color:#f1f5f9;font-size:20px;font-weight:700;">PiDev Medical</div>
                        <div style="color:#475569;font-size:13px;margin-top:4px;">Confirmation de Rendez-vous</div>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="color:#94a3b8;font-size:15px;margin:0 0 8px 0;">
                          Bonjour <strong style="color:#f1f5f9;">%s</strong>,
                        </p>
                        <p style="color:#94a3b8;font-size:14px;margin:0 0 28px 0;line-height:1.6;">
                          Votre rendez-vous a été confirmé avec succès ! 🎉<br>
                          Voici les détails de votre consultation :
                        </p>

                        <!-- Appointment Details Box -->
                        <div style="background:#141826;border:1px solid #252d42;border-radius:12px;
                                    padding:24px;margin-bottom:28px;">
                          
                          <!-- Doctor -->
                          <div style="margin-bottom:20px;">
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">MÉDECIN</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              👨‍⚕️ %s
                            </div>
                          </div>

                          <!-- Date -->
                          <div style="margin-bottom:20px;">
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">DATE</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              📅 %s
                            </div>
                          </div>

                          <!-- Time -->
                          <div>
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">HEURE</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              🕐 %s
                            </div>
                          </div>
                        </div>

                        <!-- Status Badge -->
                        <div style="background:rgba(34,197,94,0.1);border:1px solid rgba(34,197,94,0.3);
                                    border-radius:8px;padding:12px 16px;text-align:center;margin-bottom:24px;">
                          <div style="color:#22c55e;font-size:13px;font-weight:600;">
                            ✓ STATUT : EN ATTENTE
                          </div>
                        </div>

                        <!-- Important Note -->
                        <div style="background:rgba(251,191,36,0.1);border-left:3px solid #fbbf24;
                                    padding:14px 16px;border-radius:6px;margin-bottom:20px;">
                          <div style="color:#fbbf24;font-size:12px;font-weight:600;margin-bottom:4px;">
                            ⚠️ IMPORTANT
                          </div>
                          <div style="color:#94a3b8;font-size:12px;line-height:1.5;">
                            Veuillez arriver 10 minutes avant l'heure prévue.<br>
                            En cas d'empêchement, annulez votre rendez-vous depuis votre espace patient.
                          </div>
                        </div>

                        <p style="color:#475569;font-size:12px;margin:0;line-height:1.6;">
                          Vous pouvez consulter et gérer vos rendez-vous depuis votre espace patient.<br>
                          Merci de votre confiance ! 💙
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#080b14;padding:20px 40px;text-align:center;
                                 border-top:1px solid #252d42;">
                        <div style="color:#475569;font-size:11px;">
                          © 2026 PiDev Medical — Tous droits réservés
                        </div>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(patientName, doctorName, date, time);
    }

    private String buildAppointmentCancellationHtml(String patientName, String doctorName, 
                                                    String date, String time) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#080b14;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#080b14;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#0e1220;border-radius:14px;border:1px solid #252d42;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:#141826;padding:32px 40px;text-align:center;
                                 border-bottom:1px solid #252d42;">
                        <div style="font-size:32px;margin-bottom:8px;">❌</div>
                        <div style="color:#f1f5f9;font-size:20px;font-weight:700;">PiDev Medical</div>
                        <div style="color:#475569;font-size:13px;margin-top:4px;">Annulation de Rendez-vous</div>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="color:#94a3b8;font-size:15px;margin:0 0 8px 0;">
                          Bonjour <strong style="color:#f1f5f9;">%s</strong>,
                        </p>
                        <p style="color:#94a3b8;font-size:14px;margin:0 0 28px 0;line-height:1.6;">
                          Votre rendez-vous a été <strong style="color:#ef4444;">annulé avec succès</strong>.<br>
                          Voici les détails du rendez-vous annulé :
                        </p>

                        <!-- Appointment Details Box -->
                        <div style="background:#141826;border:1px solid #252d42;border-radius:12px;
                                    padding:24px;margin-bottom:28px;">
                          
                          <!-- Doctor -->
                          <div style="margin-bottom:20px;">
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">MÉDECIN</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              👨‍⚕️ %s
                            </div>
                          </div>

                          <!-- Date -->
                          <div style="margin-bottom:20px;">
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">DATE</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              📅 %s
                            </div>
                          </div>

                          <!-- Time -->
                          <div>
                            <div style="color:#475569;font-size:11px;font-weight:600;
                                        letter-spacing:1px;margin-bottom:6px;">HEURE</div>
                            <div style="color:#f1f5f9;font-size:16px;font-weight:600;">
                              🕐 %s
                            </div>
                          </div>
                        </div>

                        <!-- Status Badge -->
                        <div style="background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);
                                    border-radius:8px;padding:12px 16px;text-align:center;margin-bottom:24px;">
                          <div style="color:#ef4444;font-size:13px;font-weight:600;">
                            ❌ STATUT : ANNULÉ
                          </div>
                        </div>

                        <!-- Info Note -->
                        <div style="background:rgba(59,130,246,0.1);border-left:3px solid #3b82f6;
                                    padding:14px 16px;border-radius:6px;margin-bottom:20px;">
                          <div style="color:#3b82f6;font-size:12px;font-weight:600;margin-bottom:4px;">
                            ℹ️ INFORMATION
                          </div>
                          <div style="color:#94a3b8;font-size:12px;line-height:1.5;">
                            Vous pouvez prendre un nouveau rendez-vous à tout moment depuis l'annuaire des médecins.<br>
                            Nous espérons vous revoir bientôt !
                          </div>
                        </div>

                        <p style="color:#475569;font-size:12px;margin:0;line-height:1.6;">
                          Si vous avez des questions, n'hésitez pas à nous contacter.<br>
                          Prenez soin de vous ! 💙
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#080b14;padding:20px 40px;text-align:center;
                                 border-top:1px solid #252d42;">
                        <div style="color:#475569;font-size:11px;">
                          © 2026 PiDev Medical — Tous droits réservés
                        </div>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(patientName, doctorName, date, time);
    }
}
