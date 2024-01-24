package com.unipi.cs.kt.appBackendTest;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import com.unipi.cs.kt.appBackendTest.Services.AppUserService;
import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import com.unipi.cs.kt.appBackendTest.Services.RecommendationService;
import com.unipi.cs.kt.appBackendTest.Services.UserDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@EnableScheduling
public class AppBackendTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppBackendTestApplication.class, args);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(AppUserService appUserService, RecommendationService recommendationService, UserDataService userDataService){
		return args -> {
/*			appUserService.saveUser(new AppUser(null,"john@email.com","John","1234",null),true);
			appUserService.saveUser(new AppUser(null,"jack@email.com","Jack","1234",null),true);
			UserData userData1 = new UserData("566a34ac-27e8-417d-8797-5f2c3537b0ae");
			userData1.setApps(Arrays.asList("One","Two","Three"));
			UserData userData2 = new UserData("48e29511-e878-49cc-b022-d85d75c7c2ac");
			userData2.setApps(Arrays.asList("Four","Five","Six"));*/
			//Recommendation recommendation1 = new Recommendation(null,userData1,"Four","App",-1);
			//Recommendation recommendation2 = new Recommendation(null,userData1,"Five","App",1);
			//Recommendation recommendation3 = new Recommendation(null,userData1,"Six","App",null);
			//Recommendation recommendation4 = new Recommendation(null,userData2,"One","App",1);
			//List<Recommendation> recommendations1 = new ArrayList<>();
			//recommendations1.add(recommendation1);recommendations1.add(recommendation2);recommendations1.add(recommendation3);
			//List<Recommendation> recommendations2 = new ArrayList<>();
			//recommendations2.add(recommendation4);
			//userData1.setRecommendations(recommendations1);
			//userData2.setRecommendations(recommendations2);
			//userDataService.saveData(userData1);
			//userDataService.saveData(userData2);
		};
	}
}
