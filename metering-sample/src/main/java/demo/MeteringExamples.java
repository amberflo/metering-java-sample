package demo;

import com.amberflo.metering.core.MeteringContext;
import com.amberflo.metering.core.extensions.ServiceMetering;
import com.amberflo.metering.core.meter_message.Domain;
import com.amberflo.metering.core.meter_message.MeterMessage;
import com.amberflo.metering.core.meter_message.MeterMessageBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import com.amberflo.metering.core.meter_message.Region;

import static com.amberflo.metering.core.MeteringContext.metering;
import static com.amberflo.metering.core.extensions.ServiceMetering.serviceMetering;
import static com.amberflo.metering.core.extensions.UserMetering.userMetering;

/**
 * In this app we describe different ways to call the metering services:
 * 1. Builder.
 * 2. Factory.
 * 3. Templates.
 *
 * The example itself used the {@link Domain#Prod} configuration to setup the metering client.
 *
 * For more info about each of our api please see the java doc.
 */
public class MeteringExamples {
    private final static String METER_NAME = "TrancsactionCount";
    private final static int METER_VALUE = 3;
    private final static String USER_ID = "YWJjNDU2";
    private final static String USER_NAME = "Professor Albus Dumbledore";
    private final static String SERVICE_CALL = "process-request";
    private final static String SERVICE_NAME = "magic-service";
    private final static LocalDateTime START_TIME = LocalDateTime.now();
    private final static  Double MB_USED = 15.5;
    private final static double PROCESSING_TIME_MILLIS = 100;

    private final static boolean ERROR_FLAG = true;

    private final static Map<String, String> EXTRA_DIMENSIONS_MAP = null;


    public static void main(String[] args) throws Exception {
        // Step 1 - setting up the domain (prod dev)
        // You need to set the "metering domain" system env var to prod.
        System.setProperty(MeteringContext.METERING_DOMAIN, Domain.Prod.name());

        // Step 2 - send some meters to amberflo.
        sendSomeMeters();

        // Step 3 - flush and close the metering client.
        // For efficiency, we send meters to amberflo in batches. You can configure the max batch size in
        // the prod/def metering.json file. Having said that, even if you use a max batch size of 1, you still want
        // to call this method, especially if use configured 'isAsync' to true.
        MeteringContext.flushAndClose();

        // Comment: 'Metering' is autoClosable - so instead of step 2 and 3, you can also send meters this way:
        try (final MeteringContext context = MeteringContext.getContext()) {
            userMetering().signUp(USER_ID); // templates are just and abstraction layer on the top of the metering.
        }
    }

    /**
     * Here we describe the 3 methods to directly send meters:
     * 1. Builder.
     * 2. Factory.
     * 3. Templates.
     */
    private static void sendSomeMeters() {
        // Option 1: Create a custom meter with a builder.
        // This option gives you more flexibility regarding the way you construct your meter event, but it
        // requires more effort on your side, and might be more errors prone than other options.
        // Use it only if you need a completely custom meter.
        final MeterMessage meter = MeterMessageBuilder
                .createInstance(METER_NAME)
                .setMeterValue(METER_VALUE)
                .build();
        metering().meter(meter);

        // Look at this method to see a more advanced example for how to set a meter this way.
        customBuilderAdvanced();


        // Option 2: Create a custom message with a factory
        // A factory is a bit more structured way of creating a meter but less flexible. It lets you send
        // a meter which contains the:
        // a. The user name and id - Optional. These are the details of your user who made a call to your service
        //    let's say.
        // b. The meter name and value - Required.
        // c. The start_time - Optional. this can be a start time of a call, or the event time which is relevant
        //    for the meter. If the start time is null then the event time will by the current time when sending
        //    the meter.
        // d. The extra dimensions map - Optional. this is just a map of string keys to string values containing
        //    important dimensions.
        metering().meter(USER_NAME, USER_ID, METER_NAME, METER_VALUE, START_TIME, EXTRA_DIMENSIONS_MAP);
        metering().meter(null, null, METER_NAME, METER_VALUE, null, null);


        // Option 3: Templates
        // Templates are the most convenient but least flexible option of creating meters explicitly. Basically
        // templates are predefined meter types, with a predefined meter name, and predefined meter structure.
        //
        // Why do we need templates ?
        // Let's assume we want to create meters for a service. What would we try to measure ? ... probably we would
        // want to record commons things such as 'service calls', 'data usage' or 'processing time'. Now, we don't
        // want you to reinvent the will for such common use cases, so we created a few domain-specific factories that
        // allows you to create meters more easily.

        // Option 3.1 - Service-Metering:
        // use the serviceMetering to create
        final LocalDateTime startTime = LocalDateTime.now().minusMinutes(10);
        serviceMetering().call(USER_ID, SERVICE_CALL, startTime);
        serviceMetering().processingTime(USER_ID, SERVICE_CALL, PROCESSING_TIME_MILLIS);
        serviceMetering().dataUsage(USER_ID, SERVICE_CALL, MB_USED);
        // See the method below for more advanced\detailed examples of using the meters.
        serviceMeteringAdvanced();

        // Option 3.2 - User-Metering.
        // This templates allow you to create user (client) related meters.
        userMetering().signUp(USER_ID);
        userMetering().onboarded(USER_ID);
        userMetering().offboarded(USER_ID);
        userMetering().login(USER_ID);
        // This meter described a case a user registered to your service, but you rejected the registration
        // (false-data or a malicious user for example).
        userMetering().onboardingRejected(USER_ID);
    }

    private static void customBuilderAdvanced() {
        final Map<String, String> sessionInfo = new HashMap<>();
        sessionInfo.put("session", "789");

        final Map<String, String> countryAndStateInfo = new HashMap<>();
        sessionInfo.put("country", "US");
        sessionInfo.put("state", "WA");

        final MeterMessage meter = MeterMessageBuilder
                // You can provide the start/event time as part of the builder constructor.
                .createInstance(METER_NAME, START_TIME.minusMinutes(3))
                // meter value is actually optional param. If not set the the meter value will be 1.
                .setMeterValue(METER_VALUE)
                // if you want to capture the end time of the meter event (now), and measure the duration of
                // it, you can use the this method which will set up the duration as the time diff in millis between
                // now and the START_TIME. Calling this method will set the meter-type to "Millis".
                .captureEndTimeAndDuration() // in our case the duration should be ~3 minutes (in millis).
                // meter type isn't currently in use by amberflo. Yet if you feel the need to have it
                // use it ... we will notice that and adapt our system accordingly.
                .setMeterType("millis")
                // Set up user related data.
                .setUserId(USER_ID)
                .setUserName(USER_NAME)
                // Set up service related data.
                // Service name is a param to the 'DirectMeteringClient', nevertheless, you can override it here.
                .setServiceName(SERVICE_NAME)
                .setServiceCall(SERVICE_CALL)
                // If you want to mark your meter as error related you can call.
                .asError() /* or */ .asError(IllegalArgumentException.class)
                // you can set up a region.
                .setRegion(Region.US_West)
                // The dimensions Map gives you the option to add more properties to your meter, which don't
                // exist as part of the predefined methods of the builder.
                // For example, let's assume you want to add session info to your meter to track a process
                // or user request e2e, you can add this dimensions this way:
                .setDimensionsMap(sessionInfo)
                // Another example for custom dimensions:
                // Let's assume you want to measure user related events. As part of that you want to partition
                // your users by country and state. So you can add these two as custom attributes:
                .setDimensionsMap(countryAndStateInfo)
                // ^^ it's ok call setDimensionsMap multiple times as long as there is no intersection between
                // the keys of the maps.
                .build();
        metering().meter(meter);
    }

    /**
     * The service-metering template contains the following:
     *
     * {@link ServiceMetering#CALL}:
     * 1. CallCompleted (successfully).
     * 2. CallError.
     * 3. Call - this one is to try and measure a call regardless if it completed successfully or with an error.
     * All of these types of calls will produce a {@link ServiceMetering#CALL} meter as they indicates the event of finish
     * handling a call (with or without an error).
     *
     * Other types of calls:
     * There are 3 other more specific type of call which will produce different meter
     * 1. CallStarted - Will produce a {@link ServiceMetering#CALL_STARTED} meter. User this event if you want to
     * have different meters for the start and the end of a call.
     * 2. DataUsage - measure data used by the client (in Mb). Will produce a {@link ServiceMetering#CALL_DATA_USAGE} meter.
     * 3. ProcessingTime - Time it took to process a service call request (in millis). Will produce a
     * {@link ServiceMetering#CALL_PROCESSING_TIME} meter.
     */
    private static void serviceMeteringAdvanced() {
        // 'call' will produce a meter called "Call" and it can be used in many ways:
        serviceMetering().call(USER_ID, SERVICE_CALL);
        serviceMetering().call(USER_ID, SERVICE_CALL, START_TIME.minusMinutes(3)); // if you want an exact time
        serviceMetering().call(USER_ID, SERVICE_CALL, ERROR_FLAG); // if you want to mark it as an error
        serviceMetering().call(USER_ID, SERVICE_CALL, ERROR_FLAG, IllegalAccessError.class); // error + error type
        serviceMetering().call(USER_ID, SERVICE_CALL, START_TIME, ERROR_FLAG, IllegalAccessError.class);
        // The SERVICE_CALL in the example above isn't the meter name but a dimension.

        // If you find it more convenient you can also record a service call using
        serviceMetering().callCompleted(USER_ID, SERVICE_CALL); // To mark the end of a call that completed successfully.
        serviceMetering().callError(USER_ID, SERVICE_CALL); // To mark the end of a call that completed with an error.
        // We will see soon how these can be used.


        // 'callStarted' as the name suggests, measure an event of start handling a service call regardless of the
        // results (the call completed successfully or not).
        serviceMetering().callStarted(USER_ID, SERVICE_CALL, START_TIME);


        // 'processingTime' - measures the time it took to process the call in millis.
        serviceMetering().processingTime(USER_ID, SERVICE_CALL, PROCESSING_TIME_MILLIS);


        // 'dataUsage' - as the name suggests, measures the data returned to the client or used by the call in Mb.
        serviceMetering().dataUsage(USER_ID, SERVICE_CALL, MB_USED);


        // Examples of using multiple of the calls above:
        final Runnable methodToRun = () -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        serviceMeteringMultiCalls(methodToRun);
    }

    /**
     * You can have a similar decorator/interceptor method to this one in your code.
     */
    private static void serviceMeteringMultiCalls(final Runnable runnable) {
        final LocalDateTime startTime = LocalDateTime.now();
        try {
            serviceMetering().callStarted(USER_ID, SERVICE_CALL, startTime);

            runnable.run();

            serviceMetering().callCompleted(USER_ID, SERVICE_CALL);
        } catch (final Exception e) {
            serviceMetering().callError(USER_ID, SERVICE_CALL, e.getClass());
        } finally {
            final long durationInMillis  =
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                        startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            serviceMetering().processingTime(USER_ID, SERVICE_CALL, durationInMillis);
        }
    }
}
