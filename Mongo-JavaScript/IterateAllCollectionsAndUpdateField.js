/**
 * Created by julian on 1/17/16.
 */


var count = 0
db.getCollectionNames().forEach(function(collname) {
    // find the last item in a collection
    if(collname.indexOf("_snapshot-counts") < 1) {
        count++;
        print(collname);
        db.getCollection(collname)
            .update({}, {$rename:{"analytics.engagement-analytics-meta.posts-per-day":"analytics.engagement-analytics-meta.rate-posts-per-day"}},
                false, true);
        db.getCollection(collname)
            .update({}, {$rename:{"analytics.engagement-analytics-meta.days-per-post":"analytics.engagement-analytics-meta.rate-days-per-post"}},
                false, true);
        db.getCollection(collname)
            .update({}, {$rename:{"analytics.engagement-analytics-meta.posts-per-week":"analytics.engagement-analytics-meta.rate-posts-per-week"}},
                false, true);
    }
})
// check that it's not empty

print(count)



