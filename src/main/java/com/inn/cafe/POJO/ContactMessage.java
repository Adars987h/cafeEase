package com.inn.cafe.POJO;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A message from the landing page's contact form. Stored rather than only emailed: the
 * host blocks outbound SMTP, so mail alone would silently drop every message.
 */
@Data
@Entity
@Table(name = "contact_message")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
