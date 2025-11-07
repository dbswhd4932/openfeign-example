package com.example.elkmonitoring.service;

import com.example.elkmonitoring.config.LoggingUtils;
import com.example.elkmonitoring.domain.User;
import com.example.elkmonitoring.dto.UserRequest;
import com.example.elkmonitoring.dto.UserResponse;
import com.example.elkmonitoring.exception.BusinessException;
import com.example.elkmonitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 사용자 서비스
 * 비즈니스 로직 처리 및 구조화된 로깅 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 모든 사용자 조회
     */
    public List<UserResponse> getAllUsers() {
        long startTime = System.currentTimeMillis();

        log.info("Fetching all users");
        List<User> users = userRepository.findAll();

        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_count", users.size());

        LoggingUtils.logPerformance(log, "fetch_all_users", duration, metadata);

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ID로 사용자 조회
     */
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new BusinessException(
                            "User not found with id: " + id,
                            "USER_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        log.info("User found: {}", user.getEmail());
        return UserResponse.from(user);
    }

    /**
     * 사용자 생성
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new BusinessException(
                    "Email already exists: " + request.getEmail(),
                    "DUPLICATE_EMAIL",
                    HttpStatus.CONFLICT
            );
        }

        User user = request.toEntity();
        User savedUser = userRepository.save(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", savedUser.getId());
        eventData.put("user_email", savedUser.getEmail());
        eventData.put("user_name", savedUser.getName());
        eventData.put("user_status", savedUser.getStatus().name());

        LoggingUtils.logBusinessEvent(log, "user_created", eventData);

        log.info("User created successfully with id: {}", savedUser.getId());
        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 수정
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "User not found with id: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        // 이메일 변경 시 중복 체크
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new BusinessException(
                    "Email already exists: " + request.getEmail(),
                    "DUPLICATE_EMAIL",
                    HttpStatus.CONFLICT
            );
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", updatedUser.getId());
        eventData.put("user_email", updatedUser.getEmail());

        LoggingUtils.logBusinessEvent(log, "user_updated", eventData);

        log.info("User updated successfully with id: {}", updatedUser.getId());
        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "User not found with id: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        userRepository.delete(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", id);
        eventData.put("user_email", user.getEmail());

        LoggingUtils.logBusinessEvent(log, "user_deleted", eventData);

        log.info("User deleted successfully with id: {}", id);
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public UserResponse changeUserStatus(Long id, User.UserStatus newStatus) {
        log.info("Changing user status - userId: {}, newStatus: {}", id, newStatus);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "User not found with id: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        User.UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);

        User updatedUser = userRepository.save(user);

        // 상태 변경 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", id);
        eventData.put("old_status", oldStatus.name());
        eventData.put("new_status", newStatus.name());

        LoggingUtils.logBusinessEvent(log, "user_status_changed", eventData);

        log.info("User status changed successfully - userId: {}", id);
        return UserResponse.from(updatedUser);
    }

    /**
     * 의도적인 에러 발생 (테스트용)
     */
    public void simulateError() {
        log.warn("Simulating an error for testing purposes");

        try {
            // 의도적으로 예외 발생
            throw new RuntimeException("This is a simulated error for ELK testing");
        } catch (Exception e) {
            Map<String, Object> errorContext = new HashMap<>();
            errorContext.put("error_type", "simulated");
            errorContext.put("test_purpose", true);

            LoggingUtils.logError(log, "Simulated error occurred", e, errorContext);
            throw e;
        }
    }
}
