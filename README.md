# metering-java-sample
This repo describes in details how to setup the metering-java client, and how interact with it.

The client contains 3 main apis:
1. Ingest - allows you to send meter to Amberflo.
2. Usage - allows you to query Amberflo for statistics regarding your meters.
3. Customer-Details - allows you to easily define your customers in Amberflo.

## Ingest - Basic guidelines
### Step 1: Setup Dev/Prod/S3 client configs
Added three files to the 'Resources' folder of your project:
1. dev-metering.json
2. prod-metering.json
3. s3-metering.json

Recommended - Setup dev to use 'StandardOutputClient' for Dev, 'DirectClient' for prod, and 'S3MeteringCLient' to use S3 Metering client. This way when you are in 
dev you don't have to send real meters.

#### For Dev
```
{
    "clientType": "StandardOutputClient",
    "isAsync": true,
    "params": {
        "maxDelayInSec": 3,
        "maxBatchSize": 1
    }
}
```

Meters are being sent to the end-point (amberflo or std-out) in batches (a near real time fashion). When recording a meter, the metering client looks at two predefined fields:
1. **maxDelayInSec** - Describes the max amount of time the metering-client will wait before trying to send a batch of meters to amberflo. When recording a meter, and if more than 'maxDelayInSec' seconds have passed since the last time we sent a batch to the end-point, the metering client will add the new meter to the batch and send the batch. Otherwise (and if we didn't reach the maxBatchSize) the meter-client will just add the record to the batch.
2. **maxBatchSize** - Describes the max amount of meters in a batch. When recording a meter, if we have 'maxBatchSize' meters in the meter-client's queue, the meter-client will send the current meters batch to the end-point, even if we didn't reach the 'maxDelayInSec'.


Comment:

'isAsync' - This is actually an optional parameter (defaults to true). This parameter tells the meter-client to have a designated thread for queueing and sending the meters. **The meter client isn't thread safe unless isAsync is set to true**.


#### For Prod
```
{
  "clientType": "DirectClient",
  "maxAsyncQueueSize": 20000,
  "params": {
    "maxDelayInSec": 0.5,
    "apiKey": "your-api-key",
    "maxBatchSize": 10,
    "serviceName": "name"
  }
}
```

Change the:
1. **apiKey** - To your amberflo's api-key string.
2. **serviceName** - To your the name of your service. The service name will be added for all of your meters as an extra dimension.
3. **maxAsyncQueueSize** - Discard it (optional Parameter default to 100,000), or change it to a value bigger than 1000. 

### Step 2: Define a 'metering_domain' System env
By default, the metering client uses the 'dev-metering.json' for setting up the metering client. If you want to tell the
metering client context, to use the Prod config, you need to set a 'metering_domain' system environment variable to 
'Prod'.

You can pass the 'metering_domain' system env var directly when launching your app:
```
java -Dmetering_domain="Prod" application_launcher_class
```

You can set it directly from code
```
System.setProperty(MeteringContext.METERING_DOMAIN, Domain.Prod.toString());
```

Or load it from a properties file (see https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html for 
more info).

### Creating a S3Metering client 
There are 2 ways to create a S3Metering client. We can either create a client using config file such as s3-metering.json or we can use create client method and pass the bucket name, access key, secret key to the context.


#### For S3 Metering Clients config file
```
{
  "clientType": "S3MeteringClient",
  "isAsync": false,
  "maxAsyncQueueSize": 20000,
  "params": {
    "maxDelayInSec": 0.5,
    "maxBatchSize": 10,
    "bucketName": "3-amberflo",
    "httpRetriesCount": 5,
    "httpTimeOutSeconds": 30
  }
}
```

### Step 3: Send meters
Use the metering builder, factory or templates to send meters.

**Builder**
```
   final MeterMessage meter = MeterMessageBuilder
       .createInstance(METER_NAME, LocalDateTime.now(), CUSTOMER_ID)
         .setServiceCall(SERVICE_CALL_NAME)
         .setMeterValue(METER_VALUE)
         .build();
    metering().meter(meter);
```

**factory**
```
   final Double meterValue = 3D;
   final LocalDateTime time = LocalDateTime.now();
   final Map<String, String> extraDimensionsMap = null;
   
   metering().meter("customer_id", "meter_name", meterValue, time, extraDimensionsMap);
```

**Templates**
```
   // Service call related meters.
   serviceMetering().call("customer_id", "service_call", LocalDateTime.now()); // The meter name will be 'Call' not "service_call"
   serviceMetering().processingTime("customer_id", "service_call", processingTimeMillis, LocalDateTime.now()); // The meter name will be 'Call.processingTime'

   // Customer related meters
   customerMetering().signUp("customer_id", LocalDateTime.now());
   customerMetering().login("customer_id", LocalDateTime.now());
```

(see **ThreadContextExample** example for more info about how you can set up the customer id as a context,
so you won't have to provide it each time)

### Step 4 - flush and close
Unless you use the metering from a 'try with resources'
```
     try (final MeteringContext context = MeteringContext.getContext()) {
         customerMetering().signUp(CUSTOMER_ID, METER_TIME); // templates are just and abstraction layer on the top of the metering.
     }
```

you will need to call 'flushAndClose' explicitly before exiting your app.
```
   MeteringContext.flushAndClose();
```

### Step 5 (optional) - Add slf4j binding
By default the metering-client will log all of the warnings and errors to `std-err`. If you want it to log these messages elsewhere, all you need to do is to add the a slf4j-binding JAR to your runtime dependencies (unless you allready have it there and then you are all set).

For example, if you use:
Logging System | Slf4j Binder  | maven
-------------- | ------------- | -------------
Log4j-2 | log4j-slf4j-impl | https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
log4j version 1.2 | slf4j-log4j12 | https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
java.util.logging (JUL) | slf4j-jdk14 | https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14

More info about setting up slf4j at: http://www.slf4j.org/manual.html


## Usage - Basic guidelines
Usage api supports 3 types of requests:
1. **All** - Simple statistics for all the official meters in your account use the 'all' api.
2. **Get** - Custom-query for a specific meter.
3. **Batch** - A batch of custom-queries.

Below are the basic steps for creating a usage request for the different apis, and a short description of the response.
For more details regarding how to query the usage api, please refer to the actual examples.

### Step 1 - create the usage client
Have code similar to:
```
        final String appKey = System.getProperty("AMBERFLO_APP_KEY");
        final UsageClient usageClient = new UsageClient(appKey);
```

### Step 2 - create a request
For the 'All' api, create a **AllMetersAggregationsRequest** as follows:

```
        final TimeRange timeRange = TimeRangeFactory.truncatedLastDays(4);
        final AllMetersAggregationsRequest allRequest =
                AllMetersAggregationsRequestBuilder.instance(timeRange)
                        .setTimeGroupingInterval(AggregationInterval.DAY).build();
```

For the 'Get' api create a **MeterAggregationMetadata** request:
```
         final TimeRange timeRange = TimeRangeFactory.truncatedLastDays(4);
         final MeterAggregationMetadata getRequest =
                 MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange)
                         .setTimeGroupingInterval(AggregationInterval.DAY)
                         .setGroupBy(List.of(CUSTOMER_ID_FIELD))
                         .setFilter(Map.of("customerTier", List.of("Premium", "Gold"), "country", List.of("US")))
                         .setTake(new Take(5, !ASCENDING))
                         .build();
```


And for the 'Batch' api create a list of **MeterAggregationMetadata** requests.

### Step 2 - Send the request to Amberflo
After creating the request input just send it to amberflo:

```
       final List<DetailedMeterAggregation> allResults = usageClient.getAll(allRequest);
       final DetailedMeterAggregation getResult = usageClient.get(getRequest);
       final List<DetailedMeterAggregation> batchResults = usageClient.getBatch(List.of(getRequest));
```

As you can see in all cases we get an object or a list of **DetailedMeterAggregation**. Each 'DetailedMeterAggregation'
contains statistics for a single meter (per the specification of the request), and it contains the following fields:

```
   public class DetailedMeterAggregation {
       private final MeterAggregationMetadata metadata;
       private final List<Long> secondsSinceEpochIntervals;
       private final List<DetailedMeterAggregationGroup> clientMeters;
   
       public static class DetailedMeterAggregationGroup implements Comparable<DetailedMeterAggregationGroup> {
           private GroupInfo group;
           private Double groupValue;
           private List<DetailedAggregationValue> values;
       }
   
       public static class GroupInfo {
           private Map<String, String> groupInfo;
       }
   
       public static class DetailedAggregationValue {
           private Double value;
           private Long secondsSinceEpochUtc;
           private final Double percentageFromPrevious;
       }
   }
```

Where:
1. **metadata** - represents the aggregation id which is exactly the spec you mentioned as a query in the 'Get' api 
   (for the 'All' and 'Batch' apis each metadata will contain a query id for a single meter-api-name).
2. **secondsSinceEpochIntervals** - is an array of time-slots. Each number there represents a time
   range which starts with the time the number represents (inclusive) and ends at the next timestamp
   in the array (or infinity in case the timestamp is the last timestamp). We will see next how this
   array should be used.
3. **clientMeters** - this represents the aggregation groups of each meter. It's important to notice there
   are two type of groups:
   * **Normal-group** - Groups defined in MeterAggregationMetadata#GroupBy.
   * **Time-Buckets** - Groups defined by the MeterAggregationMetadata#timeGroupingInterval mentioned above.

   The **clientMeters** list contains a list of the **Normal-group**s where each "Normal-group" contains
   a list of values, one per **secondsSinceEpochIntervals** time-slot.

In more details **DetailedMeterAggregationGroup** contains:
1. **group** - which is essentially just a map of string to string which defines the groupBy values.
2. **groupValue** - the aggregated meter value for the entire time range.
3. **values** - a list of meter values, 1 per "time-slot" as defined in **secondsSinceEpochIntervals**.




## Detailed Examples
### Ingest - Detailed Examples
See the following apps for more detailed examples and additional features:
1. **MeteringExamples** - Describes the basic steps of using the meter client and sending meters, and the different
   ways you can call the metering-service (not including thread-context).
2. **MeteringInDevExample** - Describes the different meters-clients (direct and stdout). More specifically it shows
   that you can send the meters to std-out while you are in 'dev' env.
3. **MultiThreadExample** - In this example we simulated a multi-threads service. This example demonstrates that its
   easy and safe to call the meter service from many threads.
4. **ThreadContextExample** - This example shows how to define common attributes to be shared by many related meters
   (user id, session id, etc).

### Usage - Detailed Examples
1. **UsageExample** - Describes the main use-cases and rules of the 'Usage-Api'.

## Customer - Detailed Examples
1. **CustomerDetailsExample** - An example app which shows how to interact with the customer-details api.
2. **CustomerProductInvoiceExample** - An example app which shows how to interact with the customer-product-invoice api.


