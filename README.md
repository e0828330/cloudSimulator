cloudSimulator
=========

Building
----

The code can be compiled using maven:

```sh
cd cloudSimulator
mvn package
```

Running
-----------

You need a local monogdb instance for the simulator to work. The database with the weather forecast data can be found in the *weather.mongodump* directory. 

Once mongodb is running and have the data imported into it you can start the simulation:

```sh
cd cloudSimulator
java -jar target/cloudSimulator-0.0.1-SNAPSHOT.jar src/main/resources/config.ini
```

You can either edit the config file in place or pass a different config file as parameter.


Viewing the results
--------------

Once the run is finished three json files will be generated in a temporary location. The simulator will print out the locations of the files at the end of the run.

The files (energy-costs and sla-costs) can be visualized using the HTML5 tool in *visualizer/index.html* . It can be opened by any HTML5 capable browser like recent firefox or google chrome.

The vmcount file which contains the vm migrations can be viewed using *visualizer/vmcount.html*
