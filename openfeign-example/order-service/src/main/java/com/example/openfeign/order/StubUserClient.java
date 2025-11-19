package com.example.openfeign.order;

import com.example.openfeign.common.User;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * UserClient의 Stub 구현체
 * 실제 HTTP 호출 없이 로컬 데이터로 테스트할 수 있습니다.
 *
 * 사용법:
 * --spring.profiles.active=order-service,stub
 */
@Component
@Primary
@Profile("stub")
public class StubUserClient implements UserClient {

    private final Map<Long, User> userDatabase = new HashMap<>();

    public StubUserClient() {
        // Stub 데이터 초기화
        userDatabase.put(1L, new User(1L, "김철수Stub", "kimStub@example.com", "010-1234-5678"));
        userDatabase.put(2L, new User(2L, "이영희Stub", "leeStub@example.com", "010-2345-6789"));
        userDatabase.put(3L, new User(3L, "박민수Stub", "parkStub@example.com", "010-3456-7890"));

        System.out.println("🔧 [STUB MODE] StubUserClient initialized with " + userDatabase.size() + " users");
    }

    @Override
    public User getUserById(Long id) {
        System.out.println("🔧 [STUB] getUserById called with id: " + id);
        User user = userDatabase.get(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        return user;
    }

    @Override
    public Map<Long, User> getAllUsers() {
        System.out.println("🔧 [STUB] getAllUsers called");
        return new HashMap<>(userDatabase);
    }

    @Override
    public User createUser(User user) {
        System.out.println("🔧 [STUB] createUser called: " + user);
        userDatabase.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long id, User user) {
        System.out.println("🔧 [STUB] updateUser called for id: " + id);
        if (!userDatabase.containsKey(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        user.setId(id);
        userDatabase.put(id, user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        System.out.println("🔧 [STUB] deleteUser called for id: " + id);
        userDatabase.remove(id);
    }
}
