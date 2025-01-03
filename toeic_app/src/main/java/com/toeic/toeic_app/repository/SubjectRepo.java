package com.toeic.toeic_app.repository;

import com.toeic.toeic_app.model.QuizQuestion;
import com.toeic.toeic_app.model.Subject;
import com.toeic.toeic_app.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepo extends MongoRepository<Subject, ObjectId> {
    List<Subject> findByUserId(String userId);
    List<Subject> findByUserIdIsNull();

}
