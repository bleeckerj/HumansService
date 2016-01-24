var count = 0
db.getCollectionNames().forEach(function(collname) {
    // find the last item in a collection
    if(collname.indexOf("_snapshot-counts") > 1) {


        //print(collname);
//         var c = db.getCollection(collname).find( {$and: [{'snapshot-day-of-year' : '343'}, {'snapshot-coverage-period' : 'P1D'}] }).count()
//         count+=c
//         if(c > 0) {
//             //var x = db.getCollection(collname).find( {$and: [{'snapshot-day-of-year' : '343'}, {'snapshot-coverage-period' : 'P1D'}] })
        db.getCollection(collname).find({'snapshot-date' : '122415'}).
        forEach( function(myDoc) {
            //printjson(myDoc["snapshot-coverage-period"] )
            //printjson(myDoc.analytics["engagement-analytics-meta"])
            var x = myDoc//["engagement-analytics-meta"]
            printjson(x["username"]);
            //db.getCollection(collname).remove({'snapshot-date' : '122415'});
            count++
            //printjson(myDoc.analytics["status-json"])
            //printjson(x["status-json"])
            //printjson(collname)
        });

        //       }
    }
})
// check that it's not empty

print(count)