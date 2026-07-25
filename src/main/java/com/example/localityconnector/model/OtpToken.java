package com.example.localityconnector.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "otp_tokens")
public class OtpToken {

    @Id
    private String id;

    private String target; // email or phone number

    private String targetType; // EMAIL or PHONE

    private String otpCode; // 4-digit code (e.g., "4829")

    private Date expiresAt;

    private boolean verified = false;

    private Date createdAt;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
