package org.hibernate.url_shortener_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${REDIS_URL:redis://localhost:6379}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            // Parse Upstash Redis URL: redis://default:PASSWORD@endpoint.upstash.io:6379
            URI uri = new URI(redisUrl);

            String host = uri.getHost();
            int port = uri.getPort();
            String password = null;

            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");
                if (userInfo.length > 1) {
                    password = userInfo[1];
                }
            }

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }

            return new LettuceConnectionFactory(config);

        } catch (Exception e) {
            // Fallback to localhost for development
            return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
        }
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}