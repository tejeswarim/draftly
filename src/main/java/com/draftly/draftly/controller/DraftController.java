package com.draftly.draftly.controller;


import com.draftly.draftly.entity.Draft;
import com.draftly.draftly.service.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/draft")
public class DraftController {

    @Autowired
    private DraftService draftService;

    @PostMapping("/{id}/approve")
    public Draft approveDraft(@PathVariable Long id) throws Exception {
        return draftService.approveDraft(id);
    }

    @PostMapping("/{id}/reject")
    public Draft rejectDraft(@PathVariable Long id) {
        return draftService.rejectDraft(id);
    }

    @GetMapping
    public List<Draft> getAllDrafts() {
        return draftService.getAllDrafts();
    }

}
