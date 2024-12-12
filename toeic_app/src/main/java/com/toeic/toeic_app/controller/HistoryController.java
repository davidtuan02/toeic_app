package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.repository.HistoryRepo;
import com.toeic.toeic_app.repository.QuizQuestionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
public class HistoryController {
    @Autowired
    private HistoryRepo historyRepo;

}
