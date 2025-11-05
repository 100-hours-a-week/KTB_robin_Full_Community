package ktb3.fullstack.week4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Week4Application {

	public static void main(String[] args) {
		SpringApplication.run(Week4Application.class, args);
	}

}
