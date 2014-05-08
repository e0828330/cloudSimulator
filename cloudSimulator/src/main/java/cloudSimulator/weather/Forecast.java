package cloudSimulator.weather;

import java.util.Date;

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

  public Weather getForecast(Date date, Location location) {
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

    DBObject last = weather.find(criteriaLast)
            .sort(new BasicDBObject("currently.time", -1)).next();

    tmpWeather.setCurrentTemperature(interpolate(
            date,
            Float.valueOf(((BasicDBObject) last.get("currently")).get("temperature").toString()),
            Float.valueOf(((BasicDBObject) next.get("currently")).get("temperature").toString()),
            Integer.valueOf(((BasicDBObject) last.get("currently")).get("time").toString()),
            Integer.valueOf(((BasicDBObject) next.get("currently")).get("time").toString())
    ));

    return tmpWeather;

  }

  private float interpolate(Date date, float lastTemp, float nextTemp, int lastTime, int nextTime) {

    float tempDiff = nextTemp - lastTemp;
    float timeDiff = nextTime - lastTime;
    float timeOffset = (float) ((int) (date.getTime() / 1000) - lastTime);

    return (lastTemp + (tempDiff * (timeOffset / timeDiff)));
  }



}