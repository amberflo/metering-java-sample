package demo;

import com.amberflo.metering.core.MeteringContext;
import com.amberflo.metering.core.meter_message.MeterMessage;
import com.amberflo.metering.core.meter_message.MeterMessageBuilder;

import java.util.Random;

/**
 * A simple worker thread the records a predefined amount of meters over {@link ProducerThread#TEN_SECONDS}.
 */
class ProducerThread extends Thread {
    private final static String METER_NAME = "TrancsactionCount";
    private final static String USER_NAME = "harry potter";
    private final static String SERVICE_CALL = "process-request";

    final static int TEN_SECONDS = 10 * 1000;
    final int id;

    final Random random = new Random();

    /**
     * When started this thread will send "id" number of meters over ~10 seconds.
     * For example, if 'id' = 4, then this thread will send 4 meters to amberflo every ~2.5 seconds.
     * If 'id' = 10, then this thread will send 10 meters to amberflo every ~1 second.
     */
    ProducerThread(int id) {
        this.id = id;
    }

    private MeterMessage createMeterMessage(final int round) {
        // This is how creating a CUSTOM meter looks like.
        return MeterMessageBuilder
                .createInstance(METER_NAME)
                .setMeterValue(round*5.5)
                .setUserId(String.valueOf(id))
                .setUserName(USER_NAME)
                .setServiceCall(SERVICE_CALL + " " + id)
                .build();
    }

    public void run() {
        for (int round = 0; round < id; round++) {
            // send meter
            MeteringContext.metering().meter(createMeterMessage(round));

            // go to sleep
            sleep();
        }
    }

    private void sleep() {
        final int sleepTime = random.nextInt(TEN_SECONDS) / id;
        System.out.println("Thread " + id + " produced a message and it about to go to sleep for: " + sleepTime + " millis");

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}