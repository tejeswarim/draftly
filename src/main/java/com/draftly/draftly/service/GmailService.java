package com.draftly.draftly.service;

import com.draftly.draftly.config.GmailConfig;
import com.draftly.draftly.entity.Email;
import com.draftly.draftly.repository.EmailRepository;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class GmailService {

    @Autowired
    private EmailRepository emailRepository;

    public List<Email> getLatestEmails() throws Exception {
        Gmail service = GmailConfig.getGmailService();

        ListMessagesResponse response = service.users().messages().list("me")
                .setMaxResults(5L)
                .execute();
        List<Message> messages = response.getMessages();
        List<Email> emails = new ArrayList<>();

        if(messages != null){
            for (Message msg: messages){
                Message fullMessage = service.users().messages()
                        .get("me",msg.getId())
                        .execute();
                String subject = getSubject(fullMessage);
                String body = getBody(fullMessage);
                String fromEmail = getFromEmail(fullMessage);

                Email email = Email.builder()
                        .messageId(fullMessage.getId())
                        .subject(subject)
                        .body(body)
                        .threadId(fullMessage.getThreadId())
                        .fromEmail(fromEmail)
                        .build();

                emails.add(email);
            }
        }



        return emails;
    }

    private String getSubject(Message message) {

        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase("Subject")) {
                return header.getValue();
            }
        }

        return "No Subject";
    }

    private String getBody(Message message) {

        if (message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {

                if (part.getMimeType().equals("text/plain")) {
                    String data = part.getBody().getData();
                    return decodeBase64(data);
                }
            }
        }

        // fallback
        String data = message.getPayload().getBody().getData();
        return decodeBase64(data);
    }

    private String decodeBase64(String data) {
        if (data == null) return "";

        return new String(
                java.util.Base64.getUrlDecoder().decode(data),
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    private String getFromEmail(Message message) {

        for (MessagePartHeader header : message.getPayload().getHeaders()) {

            if (header.getName().equalsIgnoreCase("From")) {
                return header.getValue();
            }
        }

        return "";
    }

    public void sendEmail(String to, String subject, String body) throws Exception {

        Gmail service = GmailConfig.getGmailService();

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(to));

        email.setSubject(subject);
        email.setText(body);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);

        byte[] rawMessageBytes = buffer.toByteArray();

        String encodedEmail = Base64.getUrlEncoder()
                .encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        service.users().messages().send("me", message).execute();
    }
}
