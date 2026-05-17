package com.draftly.draftly.service;



import com.draftly.draftly.entity.Draft;
import com.draftly.draftly.entity.Email;
import com.draftly.draftly.entity.Status;
import com.draftly.draftly.repository.DraftRepository;
import com.draftly.draftly.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {

    @Autowired
    private DraftRepository draftRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private GmailService gmailService;

    @Async
    public void createDraftAsync(String messageId) {

        System.out.println("Async draft generation started: "
                + Thread.currentThread().getName());

        if (draftRepository.existsByMessageId(messageId)) {
            return;
        }

        Email email = emailRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String reply = aiService.generateReply(email.getBody());

        Draft draft = Draft.builder()
                .messageId(email.getMessageId())
                .subject(email.getSubject())
                .body(email.getBody())
                .replyContent(reply)
                .status(Status.PENDING)
                .build();

        draftRepository.save(draft);

        System.out.println("Draft created for message: " + messageId);
    }

    public Draft sendDraft(Long id, String toEmail) throws Exception {

        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        if (draft.getStatus() != Status.APPROVED) {
            throw new RuntimeException("Only APPROVED drafts can be sent");
        }

        gmailService.sendEmail(
                toEmail,
                "Re: " + draft.getSubject(),
                draft.getReplyContent()
        );

        draft.setStatus(Status.SENT);

        return draftRepository.save(draft);
    }

    public List<Draft> getAllDrafts() {
        return draftRepository.findAll();
    }

    public Draft approveDraft(Long id) throws Exception {
        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        draft.setStatus(Status.APPROVED);

        Email email = emailRepository.findById(draft.getMessageId()).orElseThrow(() -> new RuntimeException("email not found in DB"));

        gmailService.sendEmail(email.getFromEmail(),"Re:" + email.getSubject(), draft.getReplyContent());

        draft.setStatus(Status.SENT);

        return draftRepository.save(draft);
    }

    public Draft rejectDraft(Long id) {

        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        draft.setStatus(Status.REJECTED);

        return draftRepository.save(draft);
    }
}
