# metering-java-sample
This repo describes in details how to setup the metering-java client, and how interact with it.

## Basic guidelines
### Step 1: Setup Dev/Prod client configs
Add two files to the 'Resources' folder of your project:
1. dev-metering.json
2. prod-metering.json

Recommended - Setup dev to use 'StandardOutputClient' for Dev, and 'DirectClient' for prod. This way when you are in 
dev you don't have to send real meters.

**For Dev**
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

**For Prod**
```
{
  "clientType": "DirectClient",
  "isAsync": true,
  "params": {
    "maxDelayInSec": 0.5,
    "apiKey": "Y2hhbmdlbWU=",
    "accountName": "demo",
    "maxBatchSize": 10,
    "serviceName": "name"
  }
}
```

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
   final Double meterValue = 3.55;
   final LocalDateTime time = LocalDateTime.now();
   final Map<String, String> extraDimensionsMap = null;
   
   metering().meter("user_name", "user_id", "meter_name", meterValue, time, extraDimensionsMap);
```

**Templates**
```
   // Service call related meters.
   serviceMetering().call("user_id", "service_call"); // The meter name will be 'Call' not "service_call"
   serviceMetering().processingTime("user_id", "service_call", processingTimeMillis); // The meter name will be 'Call.processingTime'

   // User related meters
   userMetering().signUp("user_id");
   userMetering().login("user_id");
```

### Step 4 - flush and close
Unless you use the metering for a 'try with resources'
```
     try (final MeteringContext context = MeteringContext.getContext()) {
         userMetering().signUp(USER_ID); // templates are just and abstraction layer on the top of the metering.
     }
```

you will need to call 'flushAndClose' explicitly before exiting your app.
```
   MeteringContext.flushAndClose();
```



## Detailed Examples
See the following apps for more detailed examples and additional features:
1. **MeteringExamples** - Describes the basic steps of using the meter client and sending meters, and the different 
   ways you can call the metering-service (not including thread-context).
2. **MeteringInDevExample** - Describes the different meters-clients (direct and stdout). More specifically it shows 
   that you can send the meters to std-out while you are in 'dev' env.
3. **MultiThreadExample** - In this example we simulated a multi-threads service. This example demonstrates that its 
   easy and safe to call the meter service from many threads.
4. **ThreadContextExample** - This example shows how to define common attributes to be shared by many related meters 
   (user id, session id, etc).



