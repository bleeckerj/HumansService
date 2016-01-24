var count = 0
var master_count = 0;
db.getCollectionNames().forEach(function (collname) {
    // find the last item in a collection
    if (collname.indexOf("_snapshot-counts") < 1 ) {
        print(collname);
        db.getCollection(collname)
            .find({}).forEach(function (doc) {
            if(doc['analytics'] != null) {
                var foo = doc['analytics']['engagement-analytics-meta']
                if (foo.hasOwnProperty('rate-posts-per-day') == false) {
                    // Do something
                    count++;
                    //print(collname);
                }
            }
        })
        if (count > 0) {
            master_count++;
            print(collname);
        }
        count = 0;

    }
})
print(master_count);