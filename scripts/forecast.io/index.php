<?
header("Content-type: text/plain");
$m = new MongoClient();
$db = $m->weather;
$coll = $db->forecast;


$time_format = "Y-m-d\TH:i:s";




$api_key = "e712c7b161f4a8fa429fed37022412e4";
$locations = array(
	'munich' => array(
		'lat' => 48.2403103,
		'lng' => 11.7016773,
	),
	'dublin' => array(
		'lat' => 53.3243201,
		'lng' => -6.251695,
	),
	'chicago' => array(
		'lat' => 41.92349,
		'lng' => -87.918273
	),
	'ruemlang' => array(
		'lat' => 47.448642,
		'lng' => 8.540427
	),
	'la' => array(
		'lat' => 34.058102,
		'lng' => -118.235338,
	),
	
);

//json_decode(file_get_contents())

foreach($locations as $name => $loc){
	$start = file_exists($name.".txt") ? (int)file_get_contents($name.".txt") : strtotime("2013-01-01 00:00:00");
	
	if($start > strtotime("2014-01-01 00:00:00")){
		echo "$name is done...\n";
		continue;
	}
	
	for($i = 0; $i < 10; $i++){
		$url = "https://api.forecast.io/forecast/{$api_key}/{$loc['lat']},{$loc['lng']},".date($time_format, $start + ($i * 3600));
		$json = json_decode(file_get_contents($url));
		if($json){
			$coll->insert($json);
		}
	}
	
	file_put_contents($name.".txt", $start + ($i * 3600));
	
}

echo "--------\n";

$cursor = $coll->find();

// iterate through the results
foreach ($cursor as $document) {
	echo $document['latitude'] . "/" . $document['longitude'] . " - " . date("Y-m-d H:i:s",$document['currently']['time']) . ' - ' . $document['currently']['summary'] ."\n";
}
