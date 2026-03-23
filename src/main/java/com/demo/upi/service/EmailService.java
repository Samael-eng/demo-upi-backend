package com.demo.upi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp, String purpose) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("PayFlow – OTP Verification");

            // 🔹 Dynamic message
            String actionText;

            if ("REGISTER".equals(purpose)) {
                actionText = "Use the OTP below to complete your registration.";
            } else if ("LOGIN".equals(purpose)) {
                actionText = "Use the OTP below to login to your PayFlow account.";
            } else if ("RESET_PASSWORD".equals(purpose)) {
                actionText = "Use the OTP below to reset your PayFlow account password.";
            } else {
                actionText = "Use the OTP below to verify your action.";
            }

            String htmlContent =
                    "<div style='margin:0;padding:0;background:#020617;"
                  + "font-family:Segoe UI,Arial,sans-serif;padding:40px 0;'>"

                  + "  <table width='100%' cellpadding='0' cellspacing='0' border='0'>"
                  + "    <tr>"
                  + "      <td align='center'>"

                  + "        <table width='480' cellpadding='0' cellspacing='0' border='0' "
                  + "          style='background:#0f172a;border-radius:16px;"
                  + "          border:1px solid #1e293b;padding:32px;"
                  + "          box-shadow:0 0 25px rgba(139,92,246,0.25);"
                  + "          text-align:center;'>"

                  + "          <tr>"
                  + "            <td>"
                  + "              <h2 style='color:#8b5cf6;margin-bottom:8px;'>PayFlow Verification</h2>"
                  + "              <p style='color:#cbd5e1;font-size:14px;margin:0;'>Hello,</p>"
                  + "              <p style='color:#e2e8f0;font-size:14px;margin-top:8px;'>"
                  +                    actionText
                  + "              </p>"
                  + "            </td>"
                  + "          </tr>"

                  + "          <tr>"
                  + "            <td style='padding:28px 0;'>"
                  + "              <div style='display:inline-block;"
                  + "                font-size:30px;letter-spacing:6px;"
                  + "                padding:14px 28px;border-radius:10px;"
                  + "                background:#1e1b4b;color:#8b5cf6;"
                  + "                font-weight:bold;"
                  + "                box-shadow:0 0 18px rgba(139,92,246,0.45);'>"
                  +                      otp
                  + "              </div>"
                  + "            </td>"
                  + "          </tr>"

                  + "          <tr>"
                  + "            <td>"
                  + "              <p style='color:#94a3b8;font-size:13px;margin:0;'>"
                  + "                This OTP is valid for <b>5 minutes</b>."
                  + "              </p>"
                  + "              <p style='color:#64748b;font-size:12px;margin-top:16px;'>"
                  + "                Do not share this OTP with anyone."
                  + "              </p>"
                  + "            </td>"
                  + "          </tr>"

                  + "          <tr>"
                  + "            <td>"
                  + "              <hr style='border:none;border-top:1px solid #1e293b;margin:24px 0;'/>"
                  + "              <p style='color:#475569;font-size:11px;margin:0;'>"
                  + "                © 2026 PayFlow • Secure UPI Demo"
                  + "              </p>"
                  + "            </td>"
                  + "          </tr>"

                  + "        </table>"

                  + "      </td>"
                  + "    </tr>"
                  + "  </table>"

                  + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }
}