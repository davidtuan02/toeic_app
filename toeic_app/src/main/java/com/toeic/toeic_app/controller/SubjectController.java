package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.Subject;
import com.toeic.toeic_app.model.User;
import com.toeic.toeic_app.repository.QuizQuestionRepo;
import com.toeic.toeic_app.repository.SubjectRepo;
import com.toeic.toeic_app.repository.UserRepo;
import com.toeic.toeic_app.util.JwtUtil;
import com.toeic.toeic_app.wrapper.ResponseWrapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectRepo subjectRepo;

    @Autowired
    private QuizQuestionRepo questionRepo;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ResponseWrapper<List<Subject>>> getSubjectsByUserId(@PathVariable("userId") String userId) {
        try {
            List<Subject> subjects = subjectRepo.findByUserId(userId);
            if (!subjects.isEmpty()) {
                return ResponseEntity.ok(new ResponseWrapper<>(subjects, 1));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseWrapper<List<Subject>>> getAllSubjects() {
        try {
            List<Subject> subjects = subjectRepo.findAll();
            return ResponseEntity.ok(new ResponseWrapper<>(subjects, 1));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Create a new subject
    @PostMapping("/create")
    public ResponseEntity<ResponseWrapper<Subject>> createSubject(@RequestBody Subject subject) {
        try {
            Subject savedSubject = subjectRepo.save(subject);
            return ResponseEntity.ok(new ResponseWrapper<>(savedSubject, 1));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Get details of a specific subject by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Subject>> getSubjectById(@PathVariable("id") String id) {
        try {
            Optional<Subject> subject = subjectRepo.findById(new ObjectId(id));
            if (subject.isPresent()) {
                return ResponseEntity.ok(new ResponseWrapper<>(subject.get(), 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Update an existing subject
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseWrapper<Subject>> updateSubject(@PathVariable("id") String id,
                                                                  @RequestBody Subject updatedSubject) {
        try {
            Optional<Subject> existingSubject = subjectRepo.findById(new ObjectId(id));
            if (existingSubject.isPresent()) {
                Subject subject = existingSubject.get();
                subject.setSubjectName(updatedSubject.getSubjectName());
                Subject savedSubject = subjectRepo.save(subject);
                return ResponseEntity.ok(new ResponseWrapper<>(savedSubject, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

    // Delete a subject by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteSubject(@PathVariable("id") String id) {
        try {
            Optional<Subject> subject = subjectRepo.findById(new ObjectId(id));
            if (subject.isPresent()) {
                subjectRepo.deleteById(new ObjectId(id));
                return ResponseEntity.ok(new ResponseWrapper<>(null, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }
}
