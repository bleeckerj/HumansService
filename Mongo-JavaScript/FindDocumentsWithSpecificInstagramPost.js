/**
 * Created by julian on 12/30/15.
 */


db.getCollection('iamtedking_174583746').find({
        //"snapshot-coverage-period" : "P1D",
        "posts-count-in-coverage-period" : { $gte : 1 },
        "analytics.status-json" :  { $elemMatch : {
            version: { $eq : 1},
            id : "1149620237155077655_174583746"

        }}
    },
    {"snapshot-date" : 1,
        "analytics.status-json" : 1,
        "analytics.status-json.likes" : 1,
        "analytics.status-json.id" : 1}).sort({"snapshot-date" : -1})


