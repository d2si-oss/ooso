## Job goal
In this example we used Ooso to enrich data at rest.

The dataset we used contains records about yellow taxi trips in New York City.
These records include fields capturing pick-up and drop-off dates/times, pick-up and drop-off locations, trip distances, itemized fares, rate types, payment types, and driver-reported passenger counts.

The objective is to replace the ratecode with a textual value using an external data source that maps each ratecode to its real value.
## Where to find the data ?
The data is available at the NYC Taxi & Limousine Commission [website](http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml).

Note that it is ot possible to use the data splits as is, due to their size.

Please take a look at [this article](http://techblog.d2-si.eu/2017/06/27/ooso-serverless-mapreduce.html) for a walkthrough and tips on how to prepare and use the data.