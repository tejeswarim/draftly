package com.draftly.draftly.controller;


import com.draftly.draftly.config.GmailConfig;
import com.draftly.draftly.entity.Email;
import com.draftly.draftly.service.EmailWorkflowService;
import com.draftly.draftly.service.GmailService;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GmailController {

    @Autowired
    private EmailWorkflowService emailWorkflowService;

    @GetMapping("/emails")
    public List<Email> getEmails() throws Exception {
        return emailWorkflowService.fetchEmailsAndGenerateDrafts();
    }

    @GetMapping("/gmail-test")
    public String testGmail() throws Exception {
        Gmail service = GmailConfig.getGmailService();
        return "Gmail connected successfully!";
    }
}
