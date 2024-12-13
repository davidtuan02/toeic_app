package com.toeic.toeic_app.controller;

import com.toeic.toeic_app.model.User;
import com.toeic.toeic_app.repository.UserRepo;
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

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepo userRepo;

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

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        User user = optionalUser.get();
        if (new Date().after(user.getResetCodeExpiry())) {
            ResponseWrapper<?> response = new ResponseWrapper<>(null, 2);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepo.save(user);

        ResponseWrapper<?> response = new ResponseWrapper<>(null, 1);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


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

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<User>> login(@RequestBody User loginRequest) {
        try {
            Optional<User> userOptional = userRepo.findByEmail(loginRequest.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String inputPassword = loginRequest.getPassword();

// Kiểm tra nếu mật khẩu đầu vào đã là mã băm
                String passwordToCompare;
                if (isMD5Hash(inputPassword)) {
                    passwordToCompare = inputPassword; // Mật khẩu đã băm, dùng trực tiếp
                } else {
                    passwordToCompare = DigestUtils.md5DigestAsHex(inputPassword.getBytes()); // Băm mật khẩu trước khi so sánh
                }

// So sánh mật khẩu
                if (passwordToCompare.equals(user.getPassword())) {
                    ResponseWrapper<User> response = new ResponseWrapper<>(user, 1);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    ResponseWrapper<User> response = new ResponseWrapper<>(null, 2);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }
            } else {
                ResponseWrapper<User> response = new ResponseWrapper<>(null, 2);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            ResponseWrapper<User> response = new ResponseWrapper<>(null, 3);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check if the user already exists by email
            Optional<User> existingUser = userRepo.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                response.put("status", 2);
                response.put("message", "User with this email already exists");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            // Validate required fields
            if (user.getName() == null || user.getName().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Name is required");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Email is required");
                
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                response.put("status", 2);
                response.put("message", "Password is required");
                return ResponseEntity.status(HttpStatus.OK).body(response);
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

            // Save the new user
            User savedUser = userRepo.save(user);
            response.put("status", 1);
            response.put("message", "User registered successfully");
            response.put("user", savedUser);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("status", 3);
            response.put("message", "An error occurred during registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

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


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
        try {
            Optional<User> user = userRepo.findById(new ObjectId(id));
            if (user.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("User not found.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Invalid user ID format.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("An error occurred while fetching the user.");
        }
    }


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

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("status", HttpStatus.OK.value());
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Invalid user ID format or invalid data.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "An error occurred while updating the user.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
