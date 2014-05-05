package cloudSimulator;

import java.net.URL;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import simulation.ElasticityManager;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Simulator implements CommandLineRunner {

	private final int simulatedMinutes = 60 * 24 * 7; // Should be 525600 (One year)

	public static void main(String[] args) {
		SpringApplication.run(Simulator.class, args);
	}

	public void run(String... arg0) throws Exception {
		System.out.println("Started");
		
		ConfigParser parser = new ConfigParser();
		URL resource = Simulator.class.getResource("/config.ini");
		parser.doParse(resource.getPath());
	
		ElasticityManager em = new ElasticityManager();
		em.setDataCenters(parser.getDataCenters());
		
		for (int i = 0; i < simulatedMinutes; i++) {
			em.simulate();
		}

		System.out.println("Simulation ended!");
	}

}
