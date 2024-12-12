package com.toeic.toeic_app.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SUBJECT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String subjectName;
}
