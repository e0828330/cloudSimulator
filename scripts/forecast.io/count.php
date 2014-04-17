<?
header("Content-type: text/plain");
$m = new MongoClient();
$db = $m->weather;
$coll = $db->forecast;

$cursor = $coll->find();

echo "Entries in DB: ".$cursor->count()."\n\n";



// iterate through the results
foreach ($cursor as $document) {
	echo $document['latitude'] . "/" . $document['longitude'] . " - " . date("Y-m-d H:i:s",$document['currently']['time']) . ' - ' . $document['currently']['summary'] ."\n";
}