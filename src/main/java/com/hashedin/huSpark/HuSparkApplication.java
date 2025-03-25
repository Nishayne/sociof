package com.hashedin.huSpark;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.github.cdimascio.dotenv.Dotenv;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Main application class for SOCIO social network
 * Enables caching, scheduling, and batch processing
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableBatchProcessing
public class HuSparkApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context  = SpringApplication.run(HuSparkApplication.class,args);
        ConfigurableEnvironment environment = context.getEnvironment();

        System.out.println("Active profiles: " + java.util.Arrays.toString(environment.getActiveProfiles()));
        System.out.println("Property spring.datasource.url: " + environment.getProperty("spring.datasource.url"));
    
        if (environment.getActiveProfiles()[0] == "local")
		{
			Dotenv dotenv = Dotenv.configure().load(); //for local Load the .env file.
		}
	}

	/**
	 * Creates a password encoder bean for secure password storage
	 * @return BCryptPasswordEncoder instance
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Configures Swagger documentation for the API
	 * @return Docket instance with API configuration
	 */
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.hashedin.huSpark"))
				.paths(PathSelectors.any())
				.build()
				.securitySchemes(Arrays.asList(apiKey()))
				.securityContexts(Arrays.asList(securityContext()))
				.apiInfo(apiInfo());
	}

	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
				.build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
				"SOCIO API",
				"Social Network API",
				"1.0",
				"Terms of service",
				null,
				"License of API",
				"API license URL",
				Collections.emptyList());
	}

	/**
	 * Configures cache manager for optimized performance
	 * @return CacheManager instance with defined cache names
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(
				"users",
				"posts",
				"reports",
				"groups_",
				"userStats",
				"postStats",
				"reportStats",
				"groupStats"
		);
	}
}
