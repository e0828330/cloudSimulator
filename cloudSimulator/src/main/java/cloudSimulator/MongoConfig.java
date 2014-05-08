package cloudSimulator;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
	
	@Override
	protected String getDatabaseName() {
		return "weather2";
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient();
	}
}