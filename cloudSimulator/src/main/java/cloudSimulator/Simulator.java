package cloudSimulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.google.gson.Gson;

@Configuration
@ComponentScan({"cloudSimulator", "simulation", "algorithms"})
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
	
	@Autowired
	private ElasticityManager elasticityManager;
	
	public void run(String... args) throws Exception {
		logger.info("Simulator started");

		String configFile = null;
		for (String arg : args) {
			configFile = arg;
		}
		if (configFile == null) {
			URL resource = Simulator.class.getResource("/config.ini");
			parser.doParse(resource.getPath());
		}
		else {
			parser.doParse(configFile);
		}

		elasticityManager.setAlgorithm(parser.getMigrationAlgorithm());
		elasticityManager.setDataCenters(parser.getDataCenters());

		Long start = System.currentTimeMillis();
		
		for (int i = 0; i < simulatedMinutes; i++) {
			if ((i % 43200) == 0) {
				logger.info("Current time is " + Utils.getCurrentTime(i));
			}
			elasticityManager.simulate(i);
		}
		
		logger.debug("Took : " + (System.currentTimeMillis() - start) / 1000 + " seconds" );

		Gson gson = new Gson();
		/* Dump energy cost json to temp file */
		File temp = File.createTempFile("energy-costs-" + parser.getAlgorithmName() + "-", ".json");
		FileWriter fw = new FileWriter(temp.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(gson.toJson(elasticityManager.getEnergyCostList()));
		bw.close();
		System.out.println("Wrote energy-costs data to " + temp.getAbsolutePath());

		/* Dump sla cost json to temp file */
		temp = File.createTempFile("sla-costs-" + parser.getAlgorithmName() + "-", ".json");
		fw = new FileWriter(temp.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write(gson.toJson(elasticityManager.getSlaCostList()));
		bw.close();
		System.out.println("Wrote sla-costs data to " + temp.getAbsolutePath());
		
		/* Dump vm counts json to temp file */
		temp = File.createTempFile("vm-counts-" + parser.getAlgorithmName() + "-", ".json");
		fw = new FileWriter(temp.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write(gson.toJson(elasticityManager.getVmCounts()));
		bw.close();
		System.out.println("Wrote sla-costs data to " + temp.getAbsolutePath());
		logger.info("Simulation ended!");
	}

}
