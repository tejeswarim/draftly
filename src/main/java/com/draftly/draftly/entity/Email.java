package com.draftly.draftly.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    @Id
    private String messageId;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String threadId;

    private String fromEmail;
}
