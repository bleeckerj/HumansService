/**
 * Created by julian on 12/31/15.
 */
db.getCollection("omata_la_1572694762").aggregate([
    { $match:
    {
        "snapshot-coverage-period" : {$in : ["P1M"]},

        "posts-count-in-coverage-period" : { $gte : 1 },

    }
    },
    { $sort : {"snapshot-time-millis" : -1}},
    { $project:
    {
        "snapshot-coverage-period" : 1,
        "snapshot-date" : 1,
        "snapshot-run-time" : 1,
        "snapshot-coverage-period" : 1,
        "posts-count-in-coverage-period" : 1,
        "snapshot-coverage-period" : 1, "snapshot-run-time-millis" : 1,
        "analytics.engagement-analytics-meta.avg-likes" : 1

    }
    }


])
