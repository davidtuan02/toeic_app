package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.QuizQuestion;
import com.toeic.toeic_app.model.Subject;
import com.toeic.toeic_app.repository.QuizQuestionRepo;
import com.toeic.toeic_app.repository.QuizUserRepo;
import com.toeic.toeic_app.repository.SubjectRepo;
import com.toeic.toeic_app.wrapper.ResponseWrapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/quiz-question")
public class QuizQuestionController {

    @Autowired
    private QuizQuestionRepo questionRepo;

    @Autowired
    private SubjectRepo subjectRepo;

    @GetMapping("/by-subject/{subjectId}")
    public ResponseEntity<ResponseWrapper<List<QuizQuestion>>> getQuizQuestionsBySubjectId(@PathVariable("subjectId") ObjectId subjectId) {
        try {
//            ObjectId subjectObjectId = new ObjectId(subjectId);
            List<QuizQuestion> questions = questionRepo.findBySubjectId(subjectId);
            if (!questions.isEmpty()) {
                return ResponseEntity.ok(new ResponseWrapper<>(questions, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseWrapper<List<QuizQuestion>>> getAllQuizQuestions() {
        try {
            List<QuizQuestion> questions = questionRepo.findAll();
            return ResponseEntity.ok(new ResponseWrapper<>(questions, 1));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Create a new quiz question
    @PostMapping("/create")
    public ResponseEntity<ResponseWrapper<QuizQuestion>> createQuizQuestion(@RequestBody QuizQuestion quizQuestion) {
        try {
            // Save the new quiz question
            QuizQuestion savedQuestion = questionRepo.save(quizQuestion);

            // Update the numberQuestion in the corresponding subject
            if (quizQuestion.getSubjectId() != null) {
                Optional<Subject> subjectOptional = subjectRepo.findById(quizQuestion.getSubjectId());
                if (subjectOptional.isPresent()) {
                    Subject subject = subjectOptional.get();
                    long count = questionRepo.countBySubjectId(subject.getId());
                    subject.setNumberQuestion(String.valueOf(count));
                    subjectRepo.save(subject);
                }
            }

            return ResponseEntity.ok(new ResponseWrapper<>(savedQuestion, 1));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Get details of a specific quiz question by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<QuizQuestion>> getQuizQuestionById(@PathVariable("id") String id) {
        try {
            Optional<QuizQuestion> quizQuestion = questionRepo.findById(new ObjectId(id));
            if (quizQuestion.isPresent()) {
                return ResponseEntity.ok(new ResponseWrapper<>(quizQuestion.get(), 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Update an existing quiz question
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseWrapper<QuizQuestion>> updateQuizQuestion(@PathVariable("id") String id,
                                                                            @RequestBody QuizQuestion updatedQuizQuestion) {
        try {
            Optional<QuizQuestion> existingQuestion = questionRepo.findById(new ObjectId(id));
            if (existingQuestion.isPresent()) {
                QuizQuestion quizQuestion = existingQuestion.get();
                quizQuestion.setSubjectId(updatedQuizQuestion.getSubjectId());
                quizQuestion.setQuestionText(updatedQuizQuestion.getQuestionText());
                quizQuestion.setOptions(updatedQuizQuestion.getOptions());
                QuizQuestion savedQuestion = questionRepo.save(quizQuestion);
                return ResponseEntity.ok(new ResponseWrapper<>(savedQuestion, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Delete a quiz question by ID
    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteQuizQuestion(@PathVariable("id") String id) {
        try {
            Optional<QuizQuestion> quizQuestion = questionRepo.findById(new ObjectId(id));
            if (quizQuestion.isPresent()) {
                questionRepo.deleteById(new ObjectId(id));
                return ResponseEntity.ok(new ResponseWrapper<>(null, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }
}

