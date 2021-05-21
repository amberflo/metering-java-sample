package demo;

import com.amberflo.metering.ingest.Metering;
import com.amberflo.metering.ingest.meter_message.MeterMessage;
import com.amberflo.metering.ingest.meter_message.MeterMessageBuilder;
import com.amberflo.metering.ingest.meter_message.ThreadContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.amberflo.metering.ingest.MeteringContext.metering;
import static com.amberflo.metering.ingest.extensions.ServiceMetering.serviceMetering;
import static com.amberflo.metering.ingest.extensions.CustomerMetering.customerMetering;

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
            // Example 1 - using threadContext explicitly (not in the scope of try-with-resources)
            // The thread context is relevant from the point you defined it to the point you close it.
            // Also notice that in this example we set context for the customer-id, service-call, and the session.
            // The only way to set the customer info not as part of the context is by calling "setCustomerInfo".
            final ThreadContext threadContext = new ThreadContext();
            try {
                threadContext.setCustomerInfo(CUSTOMER_ID)
                        .properties().setServiceCall(SERVICE_CALL).setDimensionsMap(SESSION_INFO);

                recordMeterWhenCustomerContext(meterNum++);
            } finally {
                threadContext.close();
            }


            // Example 2 - try-with-resources
            // (https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
            // The thread context is auto-closable so you can use it in a context of a 'try with resource'.
            // With try-with-resources we can write the same logic as the logic about using a more precise syntax.
            try(final ThreadContext context = new ThreadContext()) {
                context.setCustomerInfo(CUSTOMER_ID)
                        .properties().setServiceCall(SERVICE_CALL).setDimensionsMap(SESSION_INFO);

                recordMeterWhenCustomerContext(meterNum++);
            }


            // Example 3 - no context.
            // Once you are out of the context, the new meters won't have any of the previous context attributes.
            recordNotInCusomerContext(meterNum++);

            // Example 4 - set up a session info and record 2 meters.
            try(final ThreadContext context = new ThreadContext()) {
                context.setCustomerInfo(CUSTOMER_ID).properties();

                recordMeterWhenCustomerContext(meterNum++);
                // Once you set up the customer-id, you don't need to provide it to the customerMetering or
                // serviceMetering templates.
                customerMetering().signUp(LocalDateTime.now());
                serviceMetering().call(SERVICE_CALL, LocalDateTime.now());
            }


            // Example 5 - run from a different thread, and from the current thread.
            final var worker = new ProducerThreadWithContext();
            worker.run();
            recordNotInCusomerContext(meterNum++);
            worker.join();
            recordNotInCusomerContext(meterNum++);
        }
    }

    private static void recordMeterWhenCustomerContext(final int num) {
        // There are all kind of ways to send meters to amberflo.
        // Here we chose to use the builder.
        final MeterMessage meter = MeterMessageBuilder
                // Notice that we call 'createWithinCustomerContext' and not 'createInstance'.
                // This way we can avoid providing the customer info, as we assume it's part of
                // the context.
                .createWithinCustomerContext(METER_NAME + " " + num, LocalDateTime.now())
                .build();
        metering().meter(meter);
    }

    private static void recordNotInCusomerContext(final int num) {
        final MeterMessage meter = MeterMessageBuilder
                // If we aren't in scope of a context with customer info, we must call 'createInstance' or
                // else we will get an exception.
                .createInstance(METER_NAME + " " + num, LocalDateTime.now(), CUSTOMER_ID)
                .build();
        metering().meter(meter);
    }

    private static class ProducerThreadWithContext extends Thread {
        public void run() {
            ThreadContext.localThreadProperties()
                    .setServiceCall(SERVICE_CALL)
                    .setServiceName(SERVICE_NAME);
            recordNotInCusomerContext(-10);
        }
    }
}
