package com.toeic.toeic_app.repository;

import com.toeic.toeic_app.model.History;
import com.toeic.toeic_app.model.Subject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepo extends MongoRepository<History, ObjectId> {
}
