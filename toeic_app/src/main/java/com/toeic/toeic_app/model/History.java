package com.toeic.toeic_app.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "HISTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String userId;
    private String subjectId;
    private String numberOfCorrect;
    private Date date;
}
