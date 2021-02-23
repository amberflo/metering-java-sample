package demo;

import com.amberflo.metering.core.Metering;
import com.amberflo.metering.core.meter_message.MeterMessage;
import com.amberflo.metering.core.meter_message.MeterMessageBuilder;
import com.amberflo.metering.core.meter_message.ThreadContext;

import java.util.HashMap;
import java.util.Map;

import static com.amberflo.metering.core.MeteringContext.metering;
import static com.amberflo.metering.core.extensions.ServiceMetering.serviceMetering;
import static com.amberflo.metering.core.extensions.CustomerMetering.customerMetering;

/**
 * This example shows how to use the {@link ThreadContext}.
 *
 * {@link ThreadContext} is great for setting-up common attributes (dimensions) that describe multiple related meters.
 *
 * For example, let's assume your service received a request from multiple identified customers. The service runs every
 * request in it own thread. While processing each request you want all of the meters to record the customer-id who
 * sent the request to the server. One option is to inject the customer-id to all of the methods in your code that
 * contains metering logic. But that will be a lot of work and not a very nice code. Another option is to use
 * {@link ThreadContext} which lets you set up some common attributes such us customer, session, or the service-call.
 * These attributes, are thread specific, and will be added to all of the meters in the context of usage.
 */
public class ThreadContextExample {
    private final static String METER_NAME = "TrancsactionCount";
    private final static String CUSTOMER_ID = "YWJjNDU2";
    private final static String SERVICE_CALL = "processRequest";
    private final static String SERVICE_NAME = "myLambda";

    private final static Map<String, String> SESSION_INFO = new HashMap<>() {{
        put("session", "789");
    }};

    public static void main(String[] args) throws Exception {
        int meterNum = 0;

        try (final Metering metering = metering()) {
            // Example 1 - set context for the customer-id, service-call, and the session.
            try(final ThreadContext context = new ThreadContext()) {
                context.properties().setCustomerId(CUSTOMER_ID).setServiceCall(SERVICE_CALL)
                        .setDimensionsMap(SESSION_INFO);

                recordMeter(meterNum++);
            }
            // Notice, that the thread context is auto-closable so you can use it in a context of a 'try with resource'.


            // Example 2 - no context.
            // Once you are out of the context, the new meters won't have any of the previous context attributes.
            recordMeter(meterNum++);

            // Example 3 - set up a session info and record 2 meters.
            try(final ThreadContext context = new ThreadContext()) {
                context.properties().setCustomerId(CUSTOMER_ID);

                recordMeter(meterNum++);
                // Once you set up the customer-id, you don't need to provide it to the customerMetering or
                // serviceMetering templates.
                customerMetering().signUp();
                serviceMetering().call(SERVICE_CALL);
            }


            // Example 4 - run from a different thread, and from the current thread.
            final var worker = new ProducerThreadWithContext();
            worker.run();
            recordMeter(meterNum++);
            worker.join();
            recordMeter(meterNum++);
        }
    }

    private static void recordMeter(final int num) {
        // There are all kind of ways to send meters to amberflo.
        // Here we chose to use the builder.
        final MeterMessage meter = MeterMessageBuilder
                .createInstance(METER_NAME + " " + num)
                .build();
        metering().meter(meter);
    }

    private static class ProducerThreadWithContext extends Thread {
        public void run() {
            ThreadContext.localThreadProperties()
                    .setServiceCall(SERVICE_CALL)
                    .setServiceName(SERVICE_NAME);
            recordMeter(-10);
        }
    }
}
