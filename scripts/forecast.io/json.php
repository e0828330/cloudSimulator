<?
header("Content-type: application/json");
$m = new MongoClient();
$db = $m->weather2;
$coll = $db->forecast;
$cursor = $coll->find();
echo json_encode(iterator_to_array($cursor));