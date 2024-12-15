package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.History;
import com.toeic.toeic_app.repository.HistoryRepo;
import com.toeic.toeic_app.repository.QuizQuestionRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/history")
public class HistoryController {
    @Autowired
    private HistoryRepo historyRepo;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<History>> getHistoriesByUserId(@PathVariable String userId) {
        List<History> histories = historyRepo.findByUserId(userId);
        return ResponseEntity.ok(histories);
    }


    @GetMapping("/all")
    public ResponseEntity<List<History>> getAllHistories() {
        List<History> histories = historyRepo.findAll();
        return ResponseEntity.ok(histories);
    }

    // Get history by ID
    @GetMapping("/{id}")
    public ResponseEntity<History> getHistoryById(@PathVariable ObjectId id) {
        Optional<History> history = historyRepo.findById(id);
        return history.map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build());
    }

    // Create a new history
    @PostMapping("/create")
    public ResponseEntity<History> createHistory(@RequestBody History history) {
        history.setDate(new Date());
        History savedHistory = historyRepo.save(history);
        return ResponseEntity.status(HttpStatus.OK).body(savedHistory);
    }

    // Update history by ID
    @PutMapping("/edit/{id}")
    public ResponseEntity<History> updateHistory(@PathVariable ObjectId id, @RequestBody History history) {
        if (!historyRepo.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        history.setId(id);
        History updatedHistory = historyRepo.save(history);
        return ResponseEntity.ok(updatedHistory);
    }

    // Delete history by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable ObjectId id) {
        if (!historyRepo.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        historyRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
