## Job goal
In this example we used Ooso to implement an ad hoc query.

The dataset we used contains records about yellow taxi trips in New York City.
These records include fields capturing pick-up and drop-off dates/times, pick-up and drop-off locations, trip distances, itemized fares, rate types, payment types, and driver-reported passenger counts.

The objective is to count the trips that have similar characteristics in terms of passenger count, pickup year and trip distance.

The job is similar to the following sql query:
```sql
SELECT passenger_count,
       Extract(year FROM pickup_datetime) AS pickup_year,
       Cast(trip_distance AS INT)         AS distance,
       Count(*)                           AS the_count
FROM   trips
GROUP  BY passenger_count,
          pickup_year,
          distance

```

## Where to find the data ?
The data is available at the NYC Taxi & Limousine Commission [website](http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml).

Note that it is ot possible to use the data splits as is, due to their size.

Please take a look at [this article](http://techblog.d2-si.eu/2017/06/27/ooso-serverless-mapreduce.html) for a walkthrough and tips on how to prepare and use the data.