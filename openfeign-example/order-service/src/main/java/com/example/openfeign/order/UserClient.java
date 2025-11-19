package com.example.openfeign.order;

import com.example.openfeign.common.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User Service 클라이언트 인터페이스
 *
 * 구현체:
 * - RestUserClient: OpenFeign을 사용한 실제 HTTP 호출 (@Profile("rest"))
 * - StubUserClient: 메모리 기반 테스트용 구현체 (@Profile("stub"))
 */
public interface UserClient {

    /**
     * 특정 사용자 조회
     */
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);

    /**
     * 모든 사용자 조회
     */
    @GetMapping("/api/users")
    Map<Long, User> getAllUsers();

    /**
     * 새 사용자 생성
     */
    @PostMapping("/api/users")
    User createUser(@RequestBody User user);

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/api/users/{id}")
    User updateUser(@PathVariable("id") Long id, @RequestBody User user);

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
