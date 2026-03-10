package org.hibernate.url_shortener_backend.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
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
            // Parse Redis URL: redis://default:PASSWORD@host:port or rediss://...
            boolean useSsl = redisUrl.startsWith("rediss://");
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
                    port = Integer.parseInt(hostPort[1].split("/")[0]);
                }
            }

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }

            // Configure SSL and client options
            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                    LettuceClientConfiguration.builder();

            if (useSsl) {
                clientConfigBuilder.useSsl();
                System.out.println("✅ Redis SSL enabled");
            }

            // Set client options for better compatibility
            clientConfigBuilder.clientOptions(
                    ClientOptions.builder()
                            .protocolVersion(ProtocolVersion.RESP2)
                            .build()
            );

            LettuceClientConfiguration clientConfig = clientConfigBuilder.build();

            System.out.println("✅ Redis connecting to: " + host + ":" + port + " (SSL: " + useSsl + ")");

            return new LettuceConnectionFactory(config, clientConfig);

        } catch (Exception e) {
            System.err.println("⚠️ Redis configuration error: " + e.getMessage());
            e.printStackTrace();

            // Fallback to localhost
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