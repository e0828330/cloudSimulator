/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudSimulator.weather;

import java.util.Date;
import lombok.Data;

@Data
public class Weather {
  private Date timestamp;
  private Location locatioin;
  private float currentTemperature;
}
