/**
 * Created by julian on 1/3/16.
 */
db.getCollection("darthjulian_17938246").aggregate([
    { $match:
    {
        //"analytics.status-json.id" : "1142439077521570983_17938246",
        "snapshot-date" : "010216",
        //"snapshot-coverage-period" : {$in : ["P1D"]},
        //"period-coverage-end-date" : "121515",
        //"posts-count-in-coverage-period" : { $gte : 1 },
        "analytics.status-json" : { $elemMatch : {
            "likes.count": { $gt : 90}
        }}

    }
    },
    { $sort : {"snapshot-time-millis" : -1, "analytics.engagement-analytics-meta.avg-likes" : -1}},
    { $project:
    {
        "period-coverage-end-date" : 1,
        "period-coverage-start-date" : 1,
        "snapshot-coverage-period" : 1,
        "snapshot-date" : 1,
        "snapshot-run-time" : 1,
        "snapshot-coverage-period" : 1,
        "posts-count-in-coverage-period" : 1,
        "snapshot-coverage-period" : 1, "snapshot-run-time-millis" : 1,
        "analytics.engagement-analytics-meta.avg-likes" : 1,
        "analytics.engagement-analytics-meta.avg-comments" : 1,
        "analytics.status-json.link" : 1,
        "analytics.status-json.likes.count" : 1

    }
    }


])