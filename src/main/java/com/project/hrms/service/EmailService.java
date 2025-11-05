// src/main/java/com/project/hrms/service/EmailService.java (SỬA LẠI)
package com.project.hrms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Tự động @Autowired cho JavaMailSender
public class EmailService {

    private final JavaMailSender mailSender;

    // Lấy username email từ file .yml
    @Value("${spring.mail.username}")
    private String fromEmail;

    // Lấy URL frontend từ file .yml
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Dịch vụ email "thật" để gửi link kích hoạt.
     */
    public void sendActivationEmail(String toEmail, String token) {

        // Tạo link kích hoạt đầy đủ
        // Ví dụ: http://localhost:3000/activate-account?token=abc-123
        String activationLink = frontendUrl + "/activate-account?token=" + token;

        // Tạo nội dung email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[HRMS] Vui lòng kích hoạt tài khoản của bạn");

        // Nội dung thư
        String text = String.format(
                "Chào bạn,\n\n" +
                        "Một tài khoản đã được tạo cho bạn trên hệ thống HRMS.\n" +
                        "Vui lòng nhấn vào đường dẫn bên dưới để kích hoạt tài khoản và đặt mật khẩu:\n\n" +
                        "%s\n\n" + // Dùng %s để chèn link
                        "Lưu ý: Đường dẫn này sẽ hết hạn sau 3 ngày.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ HRMS.",
                activationLink // Biến được chèn vào %s
        );
        message.setText(text);

        try {
            // Gửi email
            mailSender.send(message);
            System.out.println("--- [EMAIL SERVICE] ---");
            System.out.println("Đã gửi email kích hoạt tới: " + toEmail);
            System.out.println("--- [END EMAIL SERVICE] ---");
        } catch (MailException e) {
            // Xử lý lỗi nếu gửi thất bại
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            // Bạn nên ném một exception tùy chỉnh ở đây
            throw new RuntimeException("Không thể gửi email kích hoạt.");
        }
    }
}