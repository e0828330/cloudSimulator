<?
header("Content-type: application/json");
$m = new MongoClient();
$db = $m->weather2;
$coll = $db->forecast;

$lat = $_REQUEST['lat'] ? : 48.2403103;
$lon = $_REQUEST['lon'] ? : 11.7016773;

$coll->ensureIndex(array("currently.time" => 1));
$coll->ensureIndex(array("currently.time" => -1));
$coll->ensureIndex(array("latitude" => 1,"longitude" => 1));

/******************************************************************************/

$m = new MongoClient();
$db = $m->weather2;
$coll = $db->forecast;

$criteria = array('latitude' => $lat, 'longitude' => $lon);
$cursor = $coll->find($criteria);

$min = strtotime("2013-01-01 00:00:00");
$max = $min + ($cursor->count()-1) * 12 * 60 * 60;

$timestamp = $_REQUEST['timestamp']?:rand($min,$max);

//echo "Entries in DB: ".$cursor->count()."\n\n$timestamp\n";

$next = $coll->findOne(array('currently.time' => array('$gt' => $timestamp)));
$last = $coll->find(array('currently.time' => array('$lt' => $timestamp)))->sort(array('currently.time' => -1))->getNext();

$lastTemp = $last['currently']['temperature'];
$nextTemp = $next['currently']['temperature'];
$tempDiff = $nextTemp - $lastTemp;
$timediff = $next['currently']['time'] - $last['currently']['time'];
$timeOffset = $timestamp - $last['currently']['time'];

//echo "Last: $lastTemp - Next: $nextTemp - TempDiff: $tempDiff \nTimeDiff: $timediff - TimeOffset: $timeOffset\n";
$approxtemp = ((float)$lastTemp + ((float)$tempDiff * ((float)$timeOffset/(float)$timediff))) + "\n\n\n";


echo json_encode(array(
	'timestamp' => $timestamp,
	'latitude' => $lat,
	'logitude' => $lon,
	'temperature' => $approxtemp
));

