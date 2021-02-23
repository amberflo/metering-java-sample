package com.amberflo.metering.cli;

import com.amberflo.metering.core.MeteringContext;
import com.amberflo.metering.core.clients.AsyncMeteringClient;
import com.amberflo.metering.core.meter_message.Domain;
import com.amberflo.metering.core.meter_message.MeterMessage;
import com.amberflo.metering.core.meter_message.MeterMessageBuilder;
import com.amberflo.metering.core.meter_message.Region;
import com.google.gson.Gson;
import picocli.CommandLine;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * This is a very basic command line tool to help send basic meters which includes the following params:
 * 1. meter_name - required.
 * 2. customer_id - optional.
 * 3. meter_value - optional (default to 1).
 * 4. as_error - optional.
 *
 * You also need to provide your 'app_key' in order to identify yourself with Amberflo.
 *
 * We use picocli in this simple tool. For more info about picocli refer to:
 * 1. https://picocli.info/#_overview
 * 2. https://www.baeldung.com/java-picocli-create-command-line-program
 */
@Command(name = "meter", mixinStandardHelpOptions = true, version = "meter 1.0.0",
        description = "Send a simple meter to Amberflo")
public class MeterCommand implements Callable<Integer> {
    public static final String CLI = "CLI";
    public static final Domain DOMAIN = Domain.Prod;
    public static final Region REGION = Region.US_West;
    public static final int MAX_SECOND_BETWEEN_WRITES = 0;
    public static final int MAX_BATCH_SIZE = 1;

    @Option(names = {"-m", "--meter_name"}, description = "The meter name identifies the meter you want to ingest.")
    private String meterName = null;

    @Option(names = {"-k", "--app_key"}, description = "appKey identifies your account with Amberflo")
    String appKey;

    @Option(names = {"-c", "--customer_id"}, description = "The customer id (the client who made the call to " +
            "your service)")
    private String customerId = null;

    @Option(names = {"-e", "--as_error"}, description = "If provided and true, then this meter will be marked as an" +
            "error/failure related meter")
    private Boolean asError = false;

    @Option(names = {"-v", "--meter_value"}, description = "The meter value")
    private Double value = null;

    @Override
    public Integer call() {
        try (final MeteringContext context = MeteringContext.createOrReplaceContext(appKey, CLI, DOMAIN,
                REGION, MAX_SECOND_BETWEEN_WRITES, MAX_BATCH_SIZE)) {
            final MeterMessageBuilder builder = MeterMessageBuilder.createInstance(meterName)
                    .setCustomerId(customerId);

            if (value != null) {
                builder.setMeterValue(value);
            }

            if (asError) {
                builder.asError();
            }

            final MeterMessage meter = builder.build();

            context.meteringInstance().meter(meter);

            final Gson gson = new Gson();
            System.out.println("meter: " + gson.toJson(meter));
            System.out.println("sent the meter to Amberflo.");
        };

        return 0;
    }

    public static void main(final String... args) {
        int exitCode = new CommandLine(new MeterCommand()).execute(args);
        System.exit(exitCode);
    }
}