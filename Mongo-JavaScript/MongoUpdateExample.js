/**
 * Created by julian on 12/27/15.
 */
db.getCollection('iamtedking_174583746').update(
    {"analytics.engagement-analytics-meta.earliest-in-period" : {$regex : ".*Dec 17.*"}},
    {$set : {"analytics.engagement-analytics-meta.earliest-in-period-date":"121715"}},
    {upsert:false,
        multi:true}

);