package com.ecommerce.deliveryperson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.ecommerce.library.*", "com.ecommerce.delivery.*", "com.ecommerce.*"})
@EnableJpaRepositories(value = "com.ecommerce.library.repository")
@EntityScan(value = "com.ecommerce.library.model")
public class DeliveryApplication {
 
	public static void main(String[] args) {
		SpringApplication.run(DeliveryApplication.class, args);
	}

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      