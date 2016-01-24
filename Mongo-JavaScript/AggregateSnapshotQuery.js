/**
 * Created by julian on 1/5/16.
 */
db.getCollection("omata_la_1572694762").aggregate([
    {
        $match: {
            "snapshot-coverage-period": {$in : ["P1M", "P15D", "P10D", "P7D", "P5D", "P3D", "P2D", "P1D"]},
            "snapshot-date" : "010216",
            // "posts-count-in-coverage-period": {$gte: 1},

        }
    },
    {$sort: {"snapshot-time-millis": -1}},
    {
        $project: {
            "snapshot-coverage-period" : 1,
            "snapshot-coverage-duration-days" : 1,
            "snapshot-date" : 1,
            "snapshot-run-time" : 1,
            "snapshot-coverage-period" : 1,
            "posts-count-in-coverage-period" : 1,
            "snapshot-coverage-period" : 1,
            "snapshot-run-time-millis" : 1,
            "analytics.engagement-analytics-meta.avg-likes" : 1,
            "analytics.engagement-analytics-meta.avg-comments" : 1
        }
    }


])
