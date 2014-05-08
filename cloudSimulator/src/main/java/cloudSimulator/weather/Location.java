package cloudSimulator.weather;

import lombok.Data;

@Data
public class Location {
  private double longitude;
  private double latitude;
  
  public Location(double lat, double lng){
    this.setLatitude(lat);
    this.setLongitude(lng);
  }
}
