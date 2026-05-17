package com.draftly.draftly.service;


import com.draftly.draftly.entity.Email;
import com.draftly.draftly.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailWorkflowService {
    @Autowired
    private GmailService gmailService;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private DraftService draftService;

    public List<Email> fetchEmailsAndGenerateDrafts() throws Exception {

        List<Email> emails = gmailService.getLatestEmails();

        for (Email email : emails) {

            if (!emailRepository.existsById(email.getMessageId())) {

                emailRepository.save(email);

                draftService.createDraftAsync(email.getMessageId());
            }
        }

        return emails;
    }
}
