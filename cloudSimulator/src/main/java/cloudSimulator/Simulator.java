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
import utils.Utils;
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
		logger.info("Simulator started");

		URL resource = Simulator.class.getResource("/config.ini");
		parser.doParse(resource.getPath());

		ElasticityManager em = new ElasticityManager();
		em.setAlgorithm(parser.getMigrationAlgorithm());
		em.setDataCenters(parser.getDataCenters());

		Long start = System.currentTimeMillis();
		
		for (int i = 0; i < simulatedMinutes; i++) {
			if ((i % 43200) == 0) {
				logger.info("Current time is " + Utils.getCurrentTime(i));
			}
			em.simulate(i);
		}
		
		logger.debug("Took : " + (System.currentTimeMillis() - start) / 1000 + " seconds" );

		logger.info("Simulation ended!");
	}

}
