package com.example.openfeign.order.client;

import com.example.openfeign.common.User;
import com.example.openfeign.order.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST 기반 UserClient 구현
 * OpenFeign을 사용하여 실제 HTTP 호출을 수행합니다.
 *
 * 사용법:
 * --spring.profiles.active=order-service,rest
 * 또는
 * user.client.type=rest
 */
@FeignClient(
    name = "user-service",
    url = "${user.service.url}",
    configuration = FeignConfig.class
)
@Profile("rest")
public interface RestUserClient extends UserClient {
}
