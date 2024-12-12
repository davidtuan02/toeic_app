package com.toeic.toeic_app.repository;

import com.toeic.toeic_app.model.History;
import com.toeic.toeic_app.model.QuizUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizUserRepo extends MongoRepository<QuizUser, ObjectId> {
}
