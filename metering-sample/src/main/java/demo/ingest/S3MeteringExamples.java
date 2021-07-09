package demo.ingest;

import com.amberflo.metering.ingest.MeteringContext;
import com.amberflo.metering.ingest.meter_message.MeterMessage;
import com.amberflo.metering.ingest.meter_message.MeterMessageBuilder;
import java.time.LocalDateTime;
import static com.amberflo.metering.ingest.MeteringContext.metering;

/**
 * In this app we describe different ways to call the S3metering services:
 * 1. Builder.
 * 2. Factory.
 **
 * For more info about each of our api please see the java doc.
 */
public class S3MeteringExamples {
    private final static String METER_NAME = "TrancsactionCount";
    private final static int METER_VALUE = 2210;
    private final static String CUSTOMER_ID = "Batman";
    private final static LocalDateTime EVENT_TIME = LocalDateTime.now();
    private final static String S3_METERING_CONFIG_FILE = "s3-metering.json";
    private final static String ACCESS_KEY = "AKIAZCXKKO52IT5MMZG7";
    private final static String SECRET_KEY = "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";
    private final static String BUCKET_NAME = "2210-amberflo";

    public static void main(String[] args) {


        createS3MeteringClientWithFile();


//        createS3MeteringClientWithCredentials();

    }

    /**
     * A way to to create S3Metering client using the config file by specifying the clientType, bucketName,
     * httpRetriesCount, httpTimeOutSeconds, isAsync parameters in the json file"
     */
    private static void createS3MeteringClientWithFile() {
        try {
            MeteringContext.contextWithS3ClientFromFile(S3_METERING_CONFIG_FILE);
            final MeterMessage meter = MeterMessageBuilder
                    .createInstance(METER_NAME, EVENT_TIME, CUSTOMER_ID)
                    .setMeterValue(METER_VALUE)
                    .build();

            metering().meter(meter);
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        MeteringContext.flushAndClose();
    }

    /**
     * A way to to create S3Metering client using the bucket name, access key, secret key, retries, timeout
     * to the context.
     */
    private static void createS3MeteringClientWithCredentials() {
        MeteringContext.contextWithS3Client(BUCKET_NAME, ACCESS_KEY, SECRET_KEY, 5, 30);

        final MeterMessage meter = MeterMessageBuilder
                .createInstance(METER_NAME, EVENT_TIME, CUSTOMER_ID)
                .setMeterValue(METER_VALUE)
                .build();

        metering().meter(meter);
        MeteringContext.flushAndClose();
    }
}
