package com.gameexpert.chat.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRateLimitService {

    private final StringRedisTemplate redisTemplate;
    private static final RedisScript<Long> SCRIPT = RedisScript.of("""
                   local count = tonumber(redis.call('GET', KEYS[1]) or '0')
                   if count >= 5 then
                       return 0
                   end
                   local updated = redis.call('INCR', KEYS[1])
                   if updated == 1 then
                       redis.call('EXPIRE', KEYS[1], 10)
                   end
                   return 1
                   """, Long.class
    );
    public boolean allow(Long playerId) {
        String key = "chat:limit:" + playerId;
//        String value = redisTemplate.opsForValue().get(key);
//        int count = value == null ? 0 : Integer.parseInt(value);
//        if (count >= 5) {
//            return false;
//        }

        // TODO Lv 19: 횟수 확인부터 최초 만료 설정까지 원자적으로 실행합니다.
        Long result = redisTemplate.execute(SCRIPT, List.of(key));
        return result != null && result == 1L;
    }
}
