package jp.levtech.rookie.attendance;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AttendanceApplication {

	public static void main(String[] args) {
		String password =
				new BCryptPasswordEncoder().encode("password123");
		
		System.out.println(password);
		SpringApplication.run(AttendanceApplication.class, args);
	}

}
