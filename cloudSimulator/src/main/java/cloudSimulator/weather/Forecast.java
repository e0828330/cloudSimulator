package cloudSimulator.weather;

import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Service
public class Forecast implements InitializingBean {

	@Autowired
	private MongoTemplate tpl;

	private DBCollection weather;

	public void afterPropertiesSet() throws Exception {
		this.weather = this.tpl.getCollection("forecast");
	}

	public Weather getForecast(Date date, Location location, boolean doForecast) {
		Weather tmpWeather = new Weather();
		tmpWeather.setLocatioin(location);
		tmpWeather.setTimestamp(date);

		DBObject criteriaNext = new BasicDBObject();
		criteriaNext.put("currently.time", new BasicDBObject("$gt", (int) (date.getTime() / 1000)));
		criteriaNext.put("latitude", location.getLatitude());
		criteriaNext.put("longitude", location.getLongitude());

		DBObject next = weather.findOne(criteriaNext);

		DBObject criteriaLast = new BasicDBObject();
		criteriaLast.put("currently.time", new BasicDBObject("$lt", (int) (date.getTime() / 1000)));
		criteriaLast.put("latitude", location.getLatitude());
		criteriaLast.put("longitude", location.getLongitude());
		DBObject last;
		try {
			last = weather.find(criteriaLast).sort(new BasicDBObject("currently.time", -1)).next();
		} catch (NoSuchElementException e) {
			last = next;
		}
		
		tmpWeather.setCurrentTemperature(interpolate(date, Float.valueOf(((BasicDBObject) last.get("currently")).get("temperature").toString()),
				Float.valueOf(((BasicDBObject) next.get("currently")).get("temperature").toString()), Integer.valueOf(((BasicDBObject) last.get("currently")).get("time").toString()),
				Integer.valueOf(((BasicDBObject) next.get("currently")).get("time").toString())));

		if (doForecast) {
			float sum = 0;
			float div = 0;
			for (int i = 1; i <= 5; i++) {
				div += i;
				Date tmpDate = new Date(date.getTime() + (i * 24 * 60 * 60 * 1000));
				Weather tmpFWeather = this.getForecast(tmpDate, location, false);
				sum += (tmpFWeather.getCurrentTemperature() * i);
			}
			tmpWeather.setForecast((sum / div) / tmpWeather.getCurrentTemperature());

		}

		return tmpWeather;
	}

	private float interpolate(Date date, float lastTemp, float nextTemp, int lastTime, int nextTime) {

		float tempDiff = nextTemp - lastTemp;
		float timeDiff = (nextTime - lastTime) != 0 ? nextTime - lastTime : 0.0001f;
		float timeOffset = (float) ((int) (date.getTime() / 1000) - lastTime);

		return (lastTemp + (tempDiff * (timeOffset / timeDiff)));
	}

}
