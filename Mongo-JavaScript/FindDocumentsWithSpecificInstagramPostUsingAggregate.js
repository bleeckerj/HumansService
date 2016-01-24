db.getCollection("omata_la_1572694762").aggregate([
    {
        $match: {
            "snapshot-coverage-period": "P1M",
            "posts-count-in-coverage-period": {$gte: 1},
            "analytics.status-json": {
                $elemMatch: {
                    version: {$eq: 1},
                    id: "1151506818418765760_1572694762"
                }
            }
        }
    },
    {$sort: {"snapshot-time-millis": -1}},
    {
        $project: {
            "snapshot-coverage-period": 1,
            "snapshot-date": 1,
            "snapshot-coverage-period": 1,
            "posts-count-in-coverage-period": 1,
            "snapshot-coverage-period": 1,

            "analytics.status-json": {
                $filter: {
                    input: "$analytics.status-json",
                    as: 'item',
                    cond: {$eq: ['$$item.id', "1151506818418765760_1572694762"]}
                }
            }
        }
    }


])
         