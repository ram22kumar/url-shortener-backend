package org.hibernate.url_shortener_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${REDIS_URL:redis://localhost:6379}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            // Parse Redis URL: redis://default:PASSWORD@host:port
            String url = redisUrl.replace("redis://", "").replace("rediss://", "");

            String host = "localhost";
            int port = 6379;
            String password = null;

            if (url.contains("@")) {
                String[] parts = url.split("@");
                String[] auth = parts[0].split(":");
                if (auth.length > 1) {
                    password = auth[1];
                }

                String[] hostPort = parts[1].split(":");
                host = hostPort[0];
                if (hostPort.length > 1) {
                    port = Integer.parseInt(hostPort[1].split("/")[0]); // Handle trailing path
                }
            }

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }

            System.out.println("✅ Redis connection: " + host + ":" + port);

            return new LettuceConnectionFactory(config);

        } catch (Exception e) {
            System.err.println("⚠️ Redis connection failed, using fallback: " + e.getMessage());
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