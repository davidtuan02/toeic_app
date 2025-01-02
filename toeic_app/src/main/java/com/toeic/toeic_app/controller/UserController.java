package com.toeic.toeic_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_app.model.User;
import com.toeic.toeic_app.repository.UserRepo;
import com.toeic.toeic_app.util.AESUtil;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.SecretKey;

@RestController
@RequestMapping("/user")
public class UserController {
    private final SecretKey secretKey;
    private final UserRepo userRepo;

    @Autowired
    public UserController(SecretKey secretKey, UserRepo userRepo) {
        this.secretKey = secretKey;
        this.userRepo = userRepo;
    }

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendResetCode(@RequestParam String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        User user = optionalUser.get();
        String code = generateVerificationCode();
        sendEmail(email, code);
        user.setResetCode(code);
        user.setResetCodeExpiry(new Date(System.currentTimeMillis() + 2 * 60 * 1000));
        userRepo.save(user);
        Map<String, String> content = new HashMap<>();
        content.put("code", code);
        ResponseWrapper<Map<String, String>> response = new ResponseWrapper<>(null, 1);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/auth/send-code")
    public ResponseEntity<?> sendAuthCode(@RequestParam String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        User user = optionalUser.get();
        String code = generateVerificationCode();
        sendAuthCode(email, code);
        user.setResetCode(code);
        user.setResetCodeExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
        userRepo.save(user);
        Map<String, String> content = new HashMap<>();
        content.put("code", code);
        ResponseWrapper<Map<String, String>> response = new ResponseWrapper<>(null, 1);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code);
        emailSender.send(message);
    }

    private void sendAuthCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verification Code");
        message.setText(
                "Hello,\n\n" +
                        "You have requested a verification code. Please use the following code to proceed:\n\n" +
                        code + "\n\n" +
                        "This code is valid for 15 minutes. If you did not request this, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Your Support Team"
        );
        emailSender.send(message);
    }

//    @PostMapping("/reset-password")
//    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
//        Optional<User> optionalUser = userRepo.findByEmail(email);
//        if (optionalUser.isEmpty()) {
//            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//
//        User user = optionalUser.get();
//        if (new Date().after(user.getResetCodeExpiry())) {
//            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//
//        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
//        user.setResetCode(null);
//        user.setResetCodeExpiry(null);
//        userRepo.save(user);
//
//        ResponseWrapper<?> response = new ResponseWrapper<>(null, 1);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }


//    @PostMapping("/login")
//    public ResponseEntity<ResponseWrapper<Object>> login(@RequestBody User loginRequest) {
//        try {
//            Optional<User> userOptional = userRepo.findByEmail(loginRequest.getEmail());
//            if (userOptional.isPresent()) {
//                User user = userOptional.get();
//                String inputPassword = loginRequest.getPassword();
//
//                // Kiểm tra nếu mật khẩu đầu vào đã là mã băm
//                String passwordToCompare;
//                if (isMD5Hash(inputPassword)) {
//                    passwordToCompare = inputPassword; // Mật khẩu đã băm, dùng trực tiếp
//                } else {
//                    passwordToCompare = DigestUtils.md5DigestAsHex(inputPassword.getBytes()); // Băm mật khẩu trước khi so sánh
//                }
//
//                // So sánh mật khẩu
//                if (passwordToCompare.equals(user.getPassword())) {
//                    String token = JwtUtil.generateToken(user.getEmail());
//                    Map<String, Object> responsee = new HashMap<>();
//                    responsee.put("token", token);
//
//                    // Đổi kiểu generic của ResponseWrapper thành Object
//                    ResponseWrapper<Object> response = new ResponseWrapper<>(responsee, 1); // Thành công
//                    return ResponseEntity.status(HttpStatus.OK).body(response);
//
//                } else {
//                    // Sai mật khẩu
//                    ResponseWrapper<Object> response = new ResponseWrapper<>(null, 2); // Sai mật khẩu
//                    return ResponseEntity.status(HttpStatus.OK).body(response);
//                }
//            } else {
//                // Không tìm thấy người dùng
//                ResponseWrapper<Object> response = new ResponseWrapper<>(null, 2); // Không tìm thấy user
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//        } catch (Exception e) {
//            // Lỗi hệ thống
//            ResponseWrapper<Object> response = new ResponseWrapper<>(null, 3); // Lỗi hệ thống
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseWrapper<String>> resetPassword(@RequestParam String email, @RequestParam String newPassword) throws Exception {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 2));
        }

        User user = optionalUser.get();
        if (new Date().after(user.getResetCodeExpiry())) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 2));
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepo.save(user);

        SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");
        ObjectMapper objectMapper = new ObjectMapper();
        String rawContent = objectMapper.writeValueAsString(user);
        String encryptedContent = AESUtil.encrypt(rawContent, secretKey);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseWrapper<>(encryptedContent, 1));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<String>> login(@RequestBody User loginRequest) {
        try {
            Optional<User> userOptional = userRepo.findByEmail(loginRequest.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String inputPassword = loginRequest.getPassword();

                // Mã hóa mật khẩu đầu vào để so sánh
                String passwordToCompare = DigestUtils.md5DigestAsHex(inputPassword.getBytes());
                if (passwordToCompare.equals(user.getPassword())) {
                    SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");

                    ObjectMapper objectMapper = new ObjectMapper();
                    String rawContent = objectMapper.writeValueAsString(user);
                    String encryptedContent = AESUtil.encrypt(rawContent, secretKey);

                    ResponseWrapper<String> response = new ResponseWrapper<>(encryptedContent, 1);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ResponseWrapper<>(null, 2)); // Sai mật khẩu
                }
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2)); // Không tìm thấy user
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3)); // Lỗi server
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ResponseWrapper<Object>> decryptContent(@RequestBody Map<String, String> requestBody) {
        try {
            // Lấy content mã hóa từ RequestBody
            String encryptedContent = requestBody.get("content");

            // Tạo khóa AES (phải giống với khóa dùng để mã hóa)
            SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");

            // Giải mã content
            String decryptedContent = AESUtil.decrypt(encryptedContent, secretKey);

            // Parse JSON đã giải mã về Object (hoặc bất kỳ model nào bạn muốn)
            ObjectMapper objectMapper = new ObjectMapper();
            Object originalContent = objectMapper.readValue(decryptedContent, Object.class);

            // Trả về response với content đã giải mã
            ResponseWrapper<Object> response = new ResponseWrapper<>(originalContent, 1);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Xử lý lỗi
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(null, 3)); // Lỗi giải mã hoặc dữ liệu không hợp lệ
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> saveUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check if the user already exists by email
            Optional<User> existingUser = userRepo.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                response.put("status", 2);
                response.put("message", "User with this email already exists");
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }

            // Validate required fields
            if (user.getName() == null || user.getName().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Name is required");
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Email is required");

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Password is required");
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }

            // Set dates and encrypt password
            Date currentDate = new Date();
            user.setCreatedDate(currentDate);
            user.setUpdatedDate(currentDate);
            String password = user.getPassword();
            if (!isMD5Hash(password)) {
                password = DigestUtils.md5DigestAsHex(password.getBytes());
            }
            user.setPassword(password);
            user.setRole("user");

            User savedUser = userRepo.save(user);

            SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");
            ObjectMapper objectMapper = new ObjectMapper();
            String rawContent = objectMapper.writeValueAsString(savedUser);
            String encryptedContent = AESUtil.encrypt(rawContent, secretKey);
            ResponseWrapper<String> responsee = new ResponseWrapper<>(encryptedContent, 1);
            return ResponseEntity.status(HttpStatus.OK).body(responsee);
        } catch (Exception e) {
            response.put("status", 3);
            response.put("message", "An error occurred during registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 3));
        }
    }

//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody User loginRequest) {
//        try {
//            Optional<User> userOptional = userRepo.findByEmail(loginRequest.getEmail());
//            if (userOptional.isPresent()) {
//                User user = userOptional.get();
//                String inputPassword = loginRequest.getPassword();
//
//                // Kiểm tra nếu mật khẩu đầu vào đã là mã băm
//                String passwordToCompare;
//                if (isMD5Hash(inputPassword)) {
//                    passwordToCompare = inputPassword; // Mật khẩu đã băm, dùng trực tiếp
//                } else {
//                    passwordToCompare = DigestUtils.md5DigestAsHex(inputPassword.getBytes()); // Băm mật khẩu trước khi so sánh
//                }
//
//                // So sánh mật khẩu
//                ResponseWrapper<User> response;
//                if (passwordToCompare.equals(user.getPassword())) {
//                    response = new ResponseWrapper<>(user, 1);
//                } else {
//                    response = new ResponseWrapper<>(null, 2);
//                }
//
//                // Chuyển phản hồi thành JSON
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(response);
//
//                // Mã hóa JSON bằng AES
//                String encryptedResponse = AESUtil.encrypt(jsonResponse, secretKey);
//
//                // Trả về chuỗi mã hóa
//                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
//            } else {
//                ResponseWrapper<User> response = new ResponseWrapper<>(null, 2);
//
//                // Chuyển phản hồi thành JSON
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(response);
//
//                // Mã hóa JSON bằng AES
//                String encryptedResponse = AESUtil.encrypt(jsonResponse, secretKey);
//
//                // Trả về chuỗi mã hóa
//                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
//            }
//        } catch (Exception e) {
//            try {
//                ResponseWrapper<User> response = new ResponseWrapper<>(null, 3);
//
//                // Chuyển phản hồi thành JSON
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(response);
//
//                // Mã hóa JSON bằng AES
//                String encryptedResponse = AESUtil.encrypt(jsonResponse, secretKey);
//
//                // Trả về chuỗi mã hóa
//                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
//            } catch (Exception ex) {
//                // Trả về lỗi nếu mã hóa thất bại
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while encrypting the response");
//            }
//        }
//    }


//    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody User user) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // Check if the user already exists by email
//            Optional<User> existingUser = userRepo.findByEmail(user.getEmail());
//            if (existingUser.isPresent()) {
//                response.put("status", 2);
//                response.put("message", "User with this email already exists");
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//
//            // Validate required fields
//            if (user.getName() == null || user.getName().isEmpty()) {
//                response.put("status", 2);
//                response.put("message", "Name is required");
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//            if (user.getEmail() == null || user.getEmail().isEmpty()) {
//                response.put("status", 2);
//                response.put("message", "Email is required");
//
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//            if (user.getPassword() == null || user.getPassword().isEmpty()) {
//                response.put("status", 2);
//                response.put("message", "Password is required");
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//
//            // Set dates and encrypt password
//            Date currentDate = new Date();
//            user.setCreatedDate(currentDate);
//            user.setUpdatedDate(currentDate);
//            String password = user.getPassword();
//            if (!isMD5Hash(password)) {
//                password = DigestUtils.md5DigestAsHex(password.getBytes());
//            }
//            user.setPassword(password);
//            user.setRole("user");
//
//            // Save the new user
//            User savedUser = userRepo.save(user);
//            response.put("status", 1);
//            response.put("message", "User registered successfully");
//            response.put("user", savedUser);
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        } catch (Exception e) {
//            response.put("status", 3);
//            response.put("message", "An error occurred during registration: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//    }

    private boolean isMD5Hash(String str) {
        return str != null && str.matches("^[a-fA-F0-9]{32}$");
    }


    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepo.findAll();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(null);
            }

            // Reverse the list order
            Collections.reverse(users);

            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }


//    @GetMapping("/{id}")
//    public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
//        try {
//            Optional<User> user = userRepo.findById(new ObjectId(id));
//            if (user.isPresent()) {
//                return ResponseEntity.status(HttpStatus.OK).body(user.get());
//            } else {
//                return ResponseEntity.status(HttpStatus.OK).body("User not found.");
//            }
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.OK).body("Invalid user ID format.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.OK).body("An error occurred while fetching the user.");
//        }
//    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
        try {
            Optional<User> user = userRepo.findById(new ObjectId(id));
            if (user.isPresent()) {
                SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");
                ObjectMapper objectMapper = new ObjectMapper();
                String rawContent = objectMapper.writeValueAsString(user);
                String encryptedContent = AESUtil.encrypt(rawContent, secretKey);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(encryptedContent, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Invalid user ID format.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("An error occurred while fetching the user.");
        }
    }


//    @PutMapping("/update/{id}")
//    public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody User userDetails) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            Optional<User> userOptional = userRepo.findById(new ObjectId(id));
//            if (userOptional.isPresent()) {
//                User user = userOptional.get();
//
//                if (userDetails.getEmail() != null) {
//                    user.setEmail(userDetails.getEmail());
//                }
//                if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
//                    if (userDetails.getPassword().length() == 32) {
//                        user.setPassword(userDetails.getPassword());
//                    } else {
//                        user.setPassword(DigestUtils.md5DigestAsHex(userDetails.getPassword().getBytes()));
//                    }
//                }
//                if (userDetails.getName() != null) {
//                    user.setName(userDetails.getName());
//                }
//                if (userDetails.getPhone() != null) {
//                    user.setPhone(userDetails.getPhone());
//                }
//
//                user.setUpdatedDate(new Date());
//                User updatedUser = userRepo.save(user);
//
//                // Prepare the success response
//                response.put("status", HttpStatus.OK.value());
//                response.put("message", "User updated successfully");
//                response.put("updatedUser", updatedUser);
//
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            } else {
//                response.put("status", HttpStatus.OK.value());
//                response.put("message", "User not found.");
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//        } catch (IllegalArgumentException e) {
//            response.put("status", HttpStatus.OK.value());
//            response.put("message", "Invalid user ID format or invalid data.");
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        } catch (Exception e) {
//            response.put("status", HttpStatus.OK.value());
//            response.put("message", "An error occurred while updating the user.");
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }
//    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody User userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOptional = userRepo.findById(new ObjectId(id));
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (userDetails.getEmail() != null) {
                    user.setEmail(userDetails.getEmail());
                }
                if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                    if (userDetails.getPassword().length() == 32) {
                        user.setPassword(userDetails.getPassword());
                    } else {
                        user.setPassword(DigestUtils.md5DigestAsHex(userDetails.getPassword().getBytes()));
                    }
                }
                if (userDetails.getName() != null) {
                    user.setName(userDetails.getName());
                }
                if (userDetails.getPhone() != null) {
                    user.setPhone(userDetails.getPhone());
                }

                user.setUpdatedDate(new Date());
                User updatedUser = userRepo.save(user);

                // Prepare the success response
                response.put("status", HttpStatus.OK.value());
                response.put("message", "User updated successfully");
                response.put("updatedUser", updatedUser);

                SecretKey secretKey = AESUtil.generateKeyFromString("Tuandz99");
                ObjectMapper objectMapper = new ObjectMapper();
                String rawContent = objectMapper.writeValueAsString(updatedUser);
                String encryptedContent = AESUtil.encrypt(rawContent, secretKey);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(encryptedContent, 1));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseWrapper<>(null, 2));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 2));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseWrapper<>(null, 2));
        }
    }




    @DeleteMapping("/deleteUsers")
    public ResponseEntity<Map<String, Object>> deleteUsers(@RequestParam List<String> ids) {
        Map<String, Object> response = new HashMap<>();
        List<ObjectId> objectIds = ids.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<User> usersToDelete = userRepo.findAllById(objectIds);

        if (!usersToDelete.isEmpty()) {
            userRepo.deleteAll(usersToDelete);
            response.put("status", "success");
            response.put("message", usersToDelete.size() + " users deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "No users found for the provided IDs.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }





}
