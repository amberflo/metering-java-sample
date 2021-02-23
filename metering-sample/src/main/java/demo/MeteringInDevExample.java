package demo;

import java.time.LocalDateTime;
import java.util.Map;

import static com.amberflo.metering.core.MeteringContext.metering;

import com.amberflo.metering.core.Metering;
import com.amberflo.metering.core.MeteringContext;
import com.amberflo.metering.core.meter_message.Domain;
import com.amberflo.metering.core.clients.StandardOutputMeteringClient;
import com.amberflo.metering.core.clients.DirectMeteringClient;

/**
 * This example came to describe how to set up the metering client config for different domains. As part of that we
 * will describe the different types of metering-client we currently have.
 *
 * The metering client has two domains: dev and prod. Each of these domains requires it own RESOURCE config file (place
 * them under the resource folder of your project):
 * 1. Dev - dev-metering.json
 * 2. Prod - prod-metering.json
 *
 * You can tell the {@link MeteringContext} which domain to use by setting the {@link MeteringContext#METERING_DOMAIN}
 * system env variable to 'Dev' or 'Prod' respectively.
 *
 * In this demo, the dev config includes two files
 * 1. dev-metering.json - which contains a {@link StandardOutputMeteringClient} configuration.
 * 2. prod-metering.json - which contains a {@link DirectMeteringClient} configuration.
 *
 * {@link StandardOutputMeteringClient} is kind of a stub-client. All it does is to log the meters to your
 * {@link System#out}. Use it in dev if you don't want to send the meters to a real end-point.
 *
 * {@link DirectMeteringClient} is the real amberflo client. Use it in prod (you can also use it in dev, just
 * make sure to have a different account than the one you have in prod).
 */
public class MeteringInDevExample {
    private final static String METER_NAME = "TrancsactionCount";
    private final static String CUSTOMER_ID = "YWJjNDU2";
    private final static String CUSTOMER_NAME = "Professor Albus Dumbledore";
    private final static LocalDateTime START_TIME = LocalDateTime.now();
    private final static Map<String, String> EXTRA_DIMENSIONS_MAP = null;

    public static void main(String[] args) throws Exception {
        // To log in dev you don't really need to set anything. Yet if you want you can call:
        System.setProperty(MeteringContext.METERING_DOMAIN, Domain.Dev.toString());
        // To set 'dev' as the current domain.

        // There are all kind of ways to send meters to amberflo.
        // Here we use the metering factory.
        try (final Metering metering = metering()) {
            for(int i = 1; i < 30; i++) {
                metering.meter(CUSTOMER_NAME, CUSTOMER_ID, METER_NAME, i, START_TIME, EXTRA_DIMENSIONS_MAP);
            }
        }
    }
}
