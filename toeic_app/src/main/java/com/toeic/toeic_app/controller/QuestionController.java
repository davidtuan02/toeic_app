package com.toeic.toeic_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_app.model.Question;
import com.toeic.toeic_app.repository.QuestionRepo;
import com.toeic.toeic_app.wrapper.ResponseWrapper;
import jakarta.annotation.Resource;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionRepo questionRepo;

    private static final String AUDIO_DIRECTORY = "/data/uploads/audio"; // Đường dẫn lưu trữ file âm thanh
    private static final String IMG_DIRECTORY = "/data/uploads/img";     // Đường dẫn lưu trữ hình ảnh

    @PostMapping("/save")
    public ResponseEntity<?> saveQuestion(@RequestParam(value = "file", required = false) MultipartFile file,
                                          @RequestParam(value = "questionImg", required = false) MultipartFile questionImg,
                                          @RequestParam("test") String test,
                                          @RequestParam("part") String part,
                                          @RequestParam("questionText") String questionText,
                                          @RequestParam("options") String optionsJson,
                                          @RequestParam("stt") String stt) {
        try {
            // Địa chỉ gốc của máy chủ
            String serverBaseUrl = "http://3.139.56.242:8081";

            // Tạo đối tượng Question
            Question question = new Question();
            question.setTest(test);
            question.setPart(part);
            question.setQuestionText(questionText);
            question.setStt(stt);

            // Xử lý file âm thanh (nếu có)
            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    String audioFileName = new ObjectId().toString() + "_" + sanitizedFileName;
                    Path audioFilePath = Paths.get(AUDIO_DIRECTORY + File.separator + audioFileName);
                    Files.write(audioFilePath, file.getBytes());
                    // Tạo URL đầy đủ cho file âm thanh
                    String audioFileUrl = serverBaseUrl + "/audio/" + audioFileName;
                    question.setQuestionAudio(audioFileUrl); // Lưu URL đầy đủ của audio
                }
            } else {
                question.setQuestionAudio(null); // Nếu không có file âm thanh, đặt giá trị null
            }

            // Xử lý ảnh câu hỏi (nếu có)
            if (questionImg != null && !questionImg.isEmpty()) {
                String originalImgName = questionImg.getOriginalFilename();
                if (originalImgName != null) {
                    String sanitizedImgName = originalImgName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    String imgFileName = new ObjectId().toString() + "_" + sanitizedImgName;
                    Path imgFilePath = Paths.get(IMG_DIRECTORY + File.separator + imgFileName);
                    Files.write(imgFilePath, questionImg.getBytes());
                    // Tạo URL đầy đủ cho ảnh câu hỏi
                    String imgFileUrl = serverBaseUrl + "/img/" + imgFileName;
                    question.setQuestionImg(imgFileUrl); // Lưu URL đầy đủ của hình ảnh
                }
            } else {
                question.setQuestionImg(null); // Nếu không có ảnh, đặt giá trị null
            }

            // Chuyển đổi chuỗi JSON thành danh sách options
            ObjectMapper mapper = new ObjectMapper();
            List<com.toeic.toeic_app.model.Question.Option> options =
                    Arrays.asList(mapper.readValue(optionsJson, com.toeic.toeic_app.model.Question.Option[].class));
            question.setOptions(options);

            Date currentDate = new Date();
            question.setCreatedDate(currentDate);
            question.setUpdatedDate(currentDate);

            // Lưu câu hỏi vào database
            Question savedQuestion = questionRepo.save(question);

            // Trả về câu hỏi đã lưu dưới dạng JSON
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", 1, "data", savedQuestion));

        } catch (IOException e) {
            // Trả về lỗi nội bộ với thông điệp JSON
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("status", 3, "message", "Internal server error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Trả về lỗi yêu cầu không hợp lệ với thông điệp JSON
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("status", 2, "message", "Bad request: " + e.getMessage()));
        }
    }




    @PostMapping("/randomByPart")
    public ResponseEntity<?> getRandomQuestionsByPart(@RequestParam("part") String part, // Đổi thành String
                                                      @RequestParam("limit") int limit) {
        try {
            // Kiểm tra giá trị của part
            System.out.println("Received part: " + part);

            // Lấy tất cả câu hỏi dựa theo part (String)
            List<Question> allQuestions = questionRepo.findAllByPart(part); // Kiểm tra kiểu của part

            if (allQuestions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2)); // Không tìm thấy câu hỏi
            }

            // Các logic tiếp theo vẫn giữ nguyên
            if (part.equals("3") || part.equals("4") || part.equals("6") || part.equals("7")) {
                if (limit <= 0 || limit % 3 != 0) {
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ResponseWrapper<>(null, 2)); // Kiểm tra limit phải là bội số của 3
                }

                Map<String, List<Question>> groupedQuestions = allQuestions.stream()
                        .collect(Collectors.groupingBy(Question::getStt));

                List<List<Question>> resultGroups = new ArrayList<>();

                for (List<Question> group : groupedQuestions.values()) {
                    Collections.shuffle(group);
                    for (int i = 0; i < group.size(); i += 3) {
                        if (i + 3 <= group.size()) {
                            resultGroups.add(group.subList(i, i + 3)); // Thêm nhóm 3 câu hỏi
                        }
                    }
                }

                if (resultGroups.size() < limit / 3) {
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ResponseWrapper<>(null, 2)); // Không đủ nhóm câu hỏi
                }

                List<List<Question>> limitedGroups = resultGroups.subList(0, limit / 3);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(limitedGroups, 1));

            } else {
                Collections.shuffle(allQuestions);
                List<Question> limitedQuestions = allQuestions.stream()
                        .limit(limit)
                        .collect(Collectors.toList());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(limitedQuestions, 1));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3)); // Lỗi không xác định
        }
    }



//    @PostMapping("/save")
//    public ResponseEntity<?> saveQuestion(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("questionImg") MultipartFile questionImg,
//            @RequestParam("test") Number test,
//            @RequestParam("part") Number part,
//            @RequestParam("questionText") String questionText,
//            @RequestParam("options") String optionsJson) {
//        try {
//            String serverBaseUrl = "http://18.216.169.143:8081";
//            String audioFileUrl = null;
//
//            // Nếu part là 3, kiểm tra xem đã có URL audio chưa
//            if (part.intValue() == 3) {
//                // Tìm các câu hỏi Part 3 đã lưu và lấy URL audio của chúng
//                List<Question> part3Questions = questionRepo.findByTestAndPart(test, part);
//
//                if (part3Questions != null && !part3Questions.isEmpty()) {
//                    // Nếu đã có câu hỏi Part 3 trước đó, lấy URL âm thanh của câu hỏi trước
//                    audioFileUrl = part3Questions.get(0).getQuestionAudio();
//                } else {
//                    // Nếu chưa có câu hỏi Part 3 nào, xử lý upload file âm thanh mới
//                    String originalFileName = file.getOriginalFilename();
//                    if (originalFileName == null) {
//                        return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//                    }
//                    String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
//                    String audioFileName = new ObjectId().toString() + "_" + sanitizedFileName;
//                    Path audioFilePath = Paths.get(AUDIO_DIRECTORY + File.separator + audioFileName);
//                    Files.write(audioFilePath, file.getBytes());
//                    audioFileUrl = serverBaseUrl + "/audio/" + audioFileName;
//                }
//            } else {
//                // Nếu không phải Part 3, upload âm thanh như bình thường
//                String originalFileName = file.getOriginalFilename();
//                if (originalFileName == null) {
//                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//                }
//                String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
//                String audioFileName = new ObjectId().toString() + "_" + sanitizedFileName;
//                Path audioFilePath = Paths.get(AUDIO_DIRECTORY + File.separator + audioFileName);
//                Files.write(audioFilePath, file.getBytes());
//                audioFileUrl = serverBaseUrl + "/audio/" + audioFileName;
//            }
//
//            // Xử lý ảnh câu hỏi
//            String originalImgName = questionImg.getOriginalFilename();
//            if (originalImgName == null) {
//                return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//            }
//            String sanitizedImgName = originalImgName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
//            String imgFileName = new ObjectId().toString() + "_" + sanitizedImgName;
//            Path imgFilePath = Paths.get(IMG_DIRECTORY + File.separator + imgFileName);
//            Files.write(imgFilePath, questionImg.getBytes());
//            String imgFileUrl = serverBaseUrl + "/img/" + imgFileName;
//
//            // Chuyển đổi JSON options thành danh sách Option
//            ObjectMapper mapper = new ObjectMapper();
//            List<com.toeic.toeic_app.model.Question.Option> options = Arrays.asList(mapper.readValue(optionsJson, com.toeic.toeic_app.model.Question.Option[].class));
//
//            // Tạo câu hỏi mới và lưu
//            Question question = new Question();
//            question.setTest(test);
//            question.setPart(part);
//            question.setQuestionText(questionText);
//            question.setQuestionAudio(audioFileUrl); // Sử dụng audioFileUrl từ logic trên
//            question.setQuestionImg(imgFileUrl);
//            question.setOptions(options);
//
//            Date currentDate = new Date();
//            question.setCreatedDate(currentDate);
//            question.setUpdatedDate(currentDate);
//
//            Question savedQuestion = questionRepo.save(question);
//
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(savedQuestion, 1));
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 3));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//        }
//    }


//    @PostMapping("/save")
//    public ResponseEntity<?> saveQuestion(@RequestParam("file") MultipartFile file,
//                                          @RequestParam("questionImg") MultipartFile questionImg,
//                                          @RequestParam("test") Number test,
//                                          @RequestParam("part") Number part,
//                                          @RequestParam("questionText") String questionText,
//                                          @RequestParam("options") String optionsJson) {
//        try {
//            // Xử lý file âm thanh
//            String originalFileName = file.getOriginalFilename();
//            if (originalFileName == null) {
//                return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//            }
//            String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
//            String audioFileName = new ObjectId().toString() + "_" + sanitizedFileName;
//            Path audioFilePath = Paths.get(AUDIO_DIRECTORY + File.separator + audioFileName);
//            Files.write(audioFilePath, file.getBytes());
//
//            // Xử lý ảnh câu hỏi
//            String originalImgName = questionImg.getOriginalFilename();
//            if (originalImgName == null) {
//                return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//            }
//            String sanitizedImgName = originalImgName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
//            String imgFileName = new ObjectId().toString() + "_" + sanitizedImgName;
//            Path imgFilePath = Paths.get(IMG_DIRECTORY + File.separator + imgFileName);
//            Files.write(imgFilePath, questionImg.getBytes());
//
//            ObjectMapper mapper = new ObjectMapper();
//            List<com.toeic.toeic_app.model.Question.Option> options = Arrays.asList(mapper.readValue(optionsJson, com.toeic.toeic_app.model.Question.Option[].class));
//
//            Question question = new Question();
//
//            // Chuyển đổi Number về Integer trước khi lưu
//            question.setTest(test.intValue());
//            question.setPart(part.intValue());
//
//            question.setQuestionText(questionText);
//            question.setQuestionAudio(audioFilePath.toString());
//            question.setQuestionImg(imgFilePath.toString());
//            question.setOptions(options);
//
//            Date currentDate = new Date();
//            question.setCreatedDate(currentDate);
//            question.setUpdatedDate(currentDate);
//
//            Question savedQuestion = questionRepo.save(question);
//
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(savedQuestion, 1));
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 3));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseWrapper<>(null, 2));
//        }
//    }


    @GetMapping("/audio/{id}")
    public ResponseEntity<?> getAudioByQuestionId(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);

            Optional<Question> optionalQuestion = questionRepo.findById(objectId);

            if (!optionalQuestion.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(null);
            }

            Question question = optionalQuestion.get();
            String audioFilePath = question.getQuestionAudio();

            // Kiểm tra xem file có tồn tại không
            Path path = Paths.get(audioFilePath);
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.OK).body(null);
            }

            FileSystemResource fileResource = new FileSystemResource(path.toFile());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(fileResource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }


    @GetMapping("/image/{id}")
    public ResponseEntity<?> getImageByQuestionId(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);

            Optional<Question> optionalQuestion = questionRepo.findById(objectId);

            if (!optionalQuestion.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body("không có question");
            }

            Question question = optionalQuestion.get();
            String imgFilePath = question.getQuestionImg();

            // Tạo đối tượng FileSystemResource
            Path path = Paths.get(imgFilePath);
            FileSystemResource fileResource = new FileSystemResource(path.toFile());

            if (fileResource.exists() && fileResource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                        .contentType(MediaType.IMAGE_JPEG) // Hoặc MediaType.IMAGE_PNG nếu là PNG
                        .body(fileResource);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("không có file ảnh");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Invalid ID format");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllQuestions() {
        try {
            List<Question> questions = questionRepo.findAll();
            if (questions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body("No questions found.");
            }
            return ResponseEntity.status(HttpStatus.OK).body(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("An error occurred while retrieving questions.");
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") String id) {
        try {
            if (!ObjectId.isValid(id)) {
                return ResponseEntity.status(HttpStatus.OK).body("Invalid ID format.");
            }
            Optional<Question> question = questionRepo.findById(new ObjectId(id));
            if (question.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(question.get());
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("Question not found.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Invalid data provided.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("An error occurred while retrieving the question.");
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable String id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "questionImg", required = false) MultipartFile questionImg,
            @RequestParam("test") String test,
            @RequestParam("part") String part,
            @RequestParam("questionText") String questionText,
            @RequestParam("options") String optionsJson,
            @RequestParam("stt") String stt) {
        try {
            // Base server URL
            String serverBaseUrl = "http://18.216.169.143:8081";

            // Fetch the existing question
            ObjectId objectId = new ObjectId(id);
            Question question = questionRepo.findById(objectId)
                    .orElseThrow(() -> new IllegalArgumentException("Question with ID " + id + " not found"));

            // Update question properties
            question.setTest(test);
            question.setPart(part);
            question.setQuestionText(questionText);
            question.setStt(stt);

            // Process audio file if provided
            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    String audioFileName = new ObjectId().toString() + "_" + sanitizedFileName;
                    Path audioFilePath = Paths.get(AUDIO_DIRECTORY + File.separator + audioFileName);
                    Files.write(audioFilePath, file.getBytes());
                    String audioFileUrl = serverBaseUrl + "/audio/" + audioFileName;
                    question.setQuestionAudio(audioFileUrl);
                }
            }

            // Process question image if provided
            if (questionImg != null && !questionImg.isEmpty()) {
                String originalImgName = questionImg.getOriginalFilename();
                if (originalImgName != null) {
                    String sanitizedImgName = originalImgName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    String imgFileName = new ObjectId().toString() + "_" + sanitizedImgName;
                    Path imgFilePath = Paths.get(IMG_DIRECTORY + File.separator + imgFileName);
                    Files.write(imgFilePath, questionImg.getBytes());
                    String imgFileUrl = serverBaseUrl + "/img/" + imgFileName;
                    question.setQuestionImg(imgFileUrl);
                }
            }

            // Update options
            ObjectMapper mapper = new ObjectMapper();
            List<com.toeic.toeic_app.model.Question.Option> options =
                    Arrays.asList(mapper.readValue(optionsJson, com.toeic.toeic_app.model.Question.Option[].class));
            question.setOptions(options);

            // Set updated date
            question.setUpdatedDate(new Date());

            // Save the updated question to the database
            Question updatedQuestion = questionRepo.save(question);

            // Return the updated question
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", 1, "data", updatedQuestion));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("status", 3, "message", "Internal server error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("status", 2, "message", "Bad request: " + e.getMessage()));
        }
    }




    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteQuestions(@RequestParam("ids") List<String> ids) {
        List<ObjectId> objectIds = ids.stream()
                .map(ObjectId::new) // Chuyển đổi danh sách String thành danh sách ObjectId
                .collect(Collectors.toList());

        // Tìm tất cả câu hỏi với các ObjectId
        List<Question> questionsToDelete = questionRepo.findAllById(objectIds);

        if (!questionsToDelete.isEmpty()) {
            questionRepo.deleteAll(questionsToDelete); // Xóa tất cả câu hỏi tìm thấy

            // Tạo phản hồi JSON
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", questionsToDelete.size() + " question(s) deleted.");
            return ResponseEntity.ok(response); // Trả về trạng thái 200 OK với phản hồi JSON
        } else {
            // Tạo phản hồi JSON cho trường hợp không tìm thấy câu hỏi
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No questions found with the provided IDs.");
            return ResponseEntity.status(HttpStatus.OK).body(response); // Trả về trạng thái 404 với phản hồi JSON
        }
    }
}
