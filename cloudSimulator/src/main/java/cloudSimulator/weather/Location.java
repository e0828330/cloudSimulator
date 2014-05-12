package cloudSimulator.weather;

import lombok.Data;

@Data
public class Location {
  private double longitude;
  private double latitude;
  
  public Location(double latitude, double longitude){
    this.setLatitude(latitude);
    this.setLongitude(longitude);
  }
}
