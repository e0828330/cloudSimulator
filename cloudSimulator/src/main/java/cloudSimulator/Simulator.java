package cloudSimulator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Simulator implements CommandLineRunner {

	public static void main(String[] args) {
		System.out.println("Manager - Console");
		SpringApplication.run(Simulator.class, args);
	}
	
	public void run(String... arg0) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Started");
	}

}
