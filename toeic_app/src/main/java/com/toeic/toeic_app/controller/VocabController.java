package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.Vocabulary;
import com.toeic.toeic_app.repository.UserRepo;
import com.toeic.toeic_app.repository.VocabRepo;
import com.toeic.toeic_app.wrapper.ResponseWrapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/vocab")
public class VocabController {
    @Autowired
    private VocabRepo vocabRepo;

    @PostMapping("save")
    public ResponseEntity<Map<String, Object>> addVocabulary(@RequestBody Vocabulary vocabulary) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (vocabulary == null) {
                response.put("code", HttpStatus.OK.value());
                response.put("message", "Vocabulary data is missing");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            Vocabulary createdVocabulary = vocabRepo.save(vocabulary);
            response.put("code", HttpStatus.OK.value());
            response.put("message", "Vocabulary saved successfully");
            response.put("data", createdVocabulary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", HttpStatus.OK.value());
            response.put("message", "An error occurred while saving the vocabulary");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<Map<String, Object>> deleteVocabularies(@RequestParam List<String> ids) {
        Map<String, Object> response = new HashMap<>();
        List<String> deletedIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        try {
            for (String id : ids) {
                // Convert the String ID to ObjectId
                ObjectId objectId;
                try {
                    objectId = new ObjectId(id); // Convert String to ObjectId
                } catch (IllegalArgumentException e) {
                    notFoundIds.add(id); // If invalid, add to notFoundIds
                    continue; // Skip this ID
                }

                Optional<Vocabulary> vocabulary = vocabRepo.findById(objectId);
                if (vocabulary.isPresent()) {
                    vocabRepo.deleteById(objectId);
                    deletedIds.add(id); // Successfully deleted
                } else {
                    notFoundIds.add(id); // Not found
                }
            }

            response.put("code", HttpStatus.OK.value());
            response.put("message", "Deletion process completed");
            response.put("deletedIds", deletedIds);
            response.put("notFoundIds", notFoundIds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", HttpStatus.OK.value());
            response.put("message", "An error occurred while deleting vocabularies");
            response.put("deletedIds", deletedIds);
            response.put("notFoundIds", notFoundIds);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }





    @PutMapping("edit/{id}")
    public ResponseEntity<Map<String, Object>> editVocabulary(@PathVariable("id") String id, @RequestBody Vocabulary vocabulary) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check if vocabulary with the given id exists
            ObjectId objectId = new ObjectId(id);

            Optional<Vocabulary> existingVocabulary = vocabRepo.findById(objectId);
            if (existingVocabulary.isEmpty()) {
                response.put("code", HttpStatus.OK.value());
                response.put("message", "Vocabulary not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            // Update the vocabulary details
            Vocabulary vocabToUpdate = existingVocabulary.get();
            vocabToUpdate.setText(vocabulary.getText());  // Example field
            vocabToUpdate.setPronunciation(vocabulary.getPronunciation());
            vocabToUpdate.setExample(vocabulary.getExample());
            vocabToUpdate.setMeaning(vocabulary.getMeaning());
            vocabToUpdate.setTopic(vocabulary.getTopic());
            // Set other fields here as necessary

            Vocabulary updatedVocabulary = vocabRepo.save(vocabToUpdate);

            response.put("code", HttpStatus.OK.value());
            response.put("message", "Vocabulary updated successfully");
            response.put("data", updatedVocabulary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", HttpStatus.OK.value());
            response.put("message", "An error occurred while updating the vocabulary");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }



    @GetMapping("all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(vocabRepo.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<?>> searchVocabulary(
            @RequestParam("key") String key,
            @RequestParam(value = "topic", required = false) Integer topic) {  // Sửa thành Integer
        try {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.ok(new ResponseWrapper<>(vocabRepo.findAll(), 1));
            }
            List<Vocabulary> results;
            if (topic != null) {
                results = vocabRepo.findByTextAndTopic(key, topic);
            } else {
                results = vocabRepo.findByText(key);
            }
            return ResponseEntity.ok(new ResponseWrapper<>(results, 1));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 2));
        }
    }

    @GetMapping("/random")
    public ResponseEntity<ResponseWrapper<?>> getRandomVocabularyByTopic(
            @RequestParam("limit") int limit,
            @RequestParam("topic") Integer topic) {
        try {
            // Nếu limit <= 0, trả về danh sách rỗng
            if (limit <= 0) {
                return ResponseEntity.ok(new ResponseWrapper<>(new ArrayList<>(), 2));
            }

            // Truy vấn MongoDB để lấy các từ vựng theo topic ngẫu nhiên
            List<Vocabulary> randomVocab = vocabRepo.findRandomByTopic(topic, limit);

            // Trả về danh sách từ vựng ngẫu nhiên
            return ResponseEntity.ok(new ResponseWrapper<>(randomVocab, 1));
        } catch (Exception e) {
            // Xử lý lỗi và trả về response lỗi
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }


}