package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.QuizUser;
import com.toeic.toeic_app.repository.QuizUserRepo;
import com.toeic.toeic_app.repository.SubjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz-user")
public class QuizUserController {
    @Autowired
    private QuizUserRepo userRepo;

}
