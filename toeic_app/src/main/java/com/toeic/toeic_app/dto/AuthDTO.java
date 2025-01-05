package com.toeic.toeic_app.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {
    private ObjectId id;
    public boolean isTwoAuth;
}
