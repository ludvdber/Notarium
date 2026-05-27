package be.freenote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = DataRedisRepositoriesAutoConfiguration.class)
@EnableScheduling
@EnableAsync
public class FreenoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreenoteApplication.class, args);
    }
}
