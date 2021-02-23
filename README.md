# metering-java-sample
This repo describes in details how to setup the metering-java client, and how interact with it.

## Basic guidelines
### Step 1: Setup Dev/Prod client configs
Add two files to the 'Resources' folder of your project:
1. dev-metering.json
2. prod-metering.json

Recommended - Setup dev to use 'StandardOutputClient' for Dev, and 'DirectClient' for prod. This way when you are in 
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
  "maxAsyncQueyeSize": 20000,
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
3. **maxAsyncQueyeSize** - Discard it (optional Parameter default to 100,000), or change it to a value bigger than 1000. 

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

### Step 3: Send meters
Use the metering builder, factory or templates to send meters.

**Builder**
```
   final MeterMessage meter = MeterMessageBuilder
       .createInstance(METER_NAME)
       .setMeterValue(METER_VALUE)
       .build();
    metering().meter(meter);
```

**factory**
```
   final Double meterValue = 3D;
   final LocalDateTime time = LocalDateTime.now();
   final Map<String, String> extraDimensionsMap = null;
   
   metering().meter("customer_name", "customer_id", "meter_name", meterValue, time, extraDimensionsMap);
```

**Templates**
```
   // Service call related meters.
   serviceMetering().call("customer_id", "service_call"); // The meter name will be 'Call' not "service_call"
   serviceMetering().processingTime("customer_id", "service_call", processingTimeMillis); // The meter name will be 'Call.processingTime'

   // Customer related meters
   customerMetering().signUp("customer_id");
   customerMetering().login("customer_id");
```

### Step 4 - flush and close
Unless you use the metering for a 'try with resources'
```
     try (final MeteringContext context = MeteringContext.getContext()) {
         customerMetering().signUp(CUSTOMER_ID); // templates are just and abstraction layer on the top of the metering.
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


## Detailed Examples
See the following apps for more detailed examples and additional features:
1. **MeteringExamples** - Describes the basic steps of using the meter client and sending meters, and the different 
   ways you can call the metering-service (not including thread-context).
2. **MeteringInDevExample** - Describes the different meters-clients (direct and stdout). More specifically it shows 
   that you can send the meters to std-out while you are in 'dev' env.
3. **MultiThreadExample** - In this example we simulated a multi-threads service. This example demonstrates that its 
   easy and safe to call the meter service from many threads.
4. **ThreadContextExample** - This example shows how to define common attributes to be shared by many related meters 
   (customer id, session id, etc).


