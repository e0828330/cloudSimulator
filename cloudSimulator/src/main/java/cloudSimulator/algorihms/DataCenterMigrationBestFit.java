package cloudSimulator.algorihms;

import algorithms.DataCenterMigration;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Weather;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulation.DataCenter;
import simulation.ElasticityManager;
import utils.Utils;

@Service(value = "migrationBestFit")
public class DataCenterMigrationBestFit implements DataCenterMigration {

    private static Weather currentWeather;

    @Autowired
    private Forecast f;

    public void manageVirtualMachines(ElasticityManager em, int minute) {
        // TODO Auto-generated method stub

        for (DataCenter dc : em.getDataCenters()) {
            //System.out.println(dc.getName());
            if (minute % 60 == 0) {
                //System.out.println("Get Weather... " + minute);
                DataCenterMigrationBestFit.currentWeather = f.getForecast(Utils.getCurrentTime(minute), dc.getLocation(), true);
            }
        }

    }

}
