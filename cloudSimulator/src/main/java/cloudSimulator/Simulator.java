package cloudSimulator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import simulation.DataCenter;
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

		List<DataCenter> dataCenters = new ArrayList<DataCenter>();
		
		ConfigParser parser = new ConfigParser();
		URL resource = Simulator.class.getResource("/config.ini");
		parser.doParse(resource.getPath());
		
		/* This is just a test */
		/* TODO: Read config file and build from there */
		DataCenter dc1 = new DataCenter();
		dc1.setName("DataCenter Vienna");
		dataCenters.add(dc1);

		DataCenter dc2 = new DataCenter();
		dc2.setName("DataCenter New York");
		dataCenters.add(dc2);

		DataCenter dc3 = new DataCenter();
		dc3.setName("DataCenter Tokio");
		dataCenters.add(dc3);
	
		ElasticityManager em = new ElasticityManager();
		em.setDataCenters(dataCenters);
		
		for (int i = 0; i < simulatedMinutes; i++) {
			//em.simulate();
		}

		System.out.println("Simulation ended!");
	}

}
