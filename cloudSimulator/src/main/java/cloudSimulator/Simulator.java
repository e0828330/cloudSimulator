package cloudSimulator;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import simulation.ElasticityManager;
import cloudSimulator.weather.Forecast;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Simulator implements CommandLineRunner {

	static Logger logger = LoggerFactory.getLogger(Simulator.class);
	
	private final int simulatedMinutes = 524880;

	public static void main(String[] args) {
		SpringApplication.run(Simulator.class, args);
	}

	@Autowired
	private ConfigParser parser;
	
	@Autowired
	private Forecast forecastService;
	
	public void run(String... arg0) throws Exception {
		System.out.println("Started");

		URL resource = Simulator.class.getResource("/config.ini");
		parser.doParse(resource.getPath());

		ElasticityManager em = new ElasticityManager();
		em.setAlgorithm(parser.getMigrationAlgorithm());
		em.setDataCenters(parser.getDataCenters());

		Long start = System.currentTimeMillis();
		
		for (int i = 0; i < simulatedMinutes; i++) {
			em.simulate(i);
		}
		
		logger.debug("Took : " + (System.currentTimeMillis() - start) / 1000 + " seconds" );

		System.out.println("Simulation ended!");
	}

}
