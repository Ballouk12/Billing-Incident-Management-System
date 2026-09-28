package ma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories("ma.repository.jpa")
@EnableElasticsearchRepositories("ma.repository.elasticsearch")
public class BackendApplication {

	public static void main(String[] args) {

		// Génération du hash BCrypt
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String password = "Mohamed123";
		String hashedPassword = encoder.encode(password);

		System.out.println("========================================");
		System.out.println("Password : " + password);
		System.out.println("BCrypt   : " + hashedPassword);
		System.out.println("========================================");

		SpringApplication.run(BackendApplication.class, args);
	}
}