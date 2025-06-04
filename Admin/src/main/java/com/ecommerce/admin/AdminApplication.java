package com.ecommerce.admin;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.ecommerce.library.*", "com.ecommerce.admin.*", "com.ecommerce"})
@EnableJpaRepositories(value = "com.ecommerce.library.repository")
@EntityScan(value = "com.ecommerce.library.model")
public class AdminApplication {
 
	public static void main(String[] args) {


		// .env is in EcommerceApp (root) folder so path is explicitely given
		Dotenv dotenv = Dotenv.configure()
				.directory("C:/Users/user/Downloads/springboot-ecommerce-step-by-step-main/EcommerceApp") // absolute path
				.ignoreIfMissing() // optional: avoid crash if missing
				.load();


		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
			//System.out.println("System set: " +entry.getKey()+" : "+entry.getValue());
		});

		SpringApplication.run(AdminApplication.class, args);
	}
}
