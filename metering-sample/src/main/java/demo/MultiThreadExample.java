package demo;

import com.amberflo.metering.core.MeteringContext;
import com.amberflo.metering.core.meter_message.Domain;

import java.util.LinkedList;
import java.util.List;

/**
 * The example simulates a sending meters from many different threads.
 *
 * In order to do that, you need to set
 *     "isAsync": true
 * As part of your metering config (see {@link MeteringInDevExample} for more details about the configuration). If you
 * set 'isAsync' to 'false', you will most likely face concurrency issues when trying to send meters.
 *
 * In this simulation we use a {@link ProducerThread}. A ProducerThread is just a thread that logs X amount of meters
 * in {@link ProducerThread#TEN_SECONDS} seconds. In this we will span 15 such producers with X from 1 to 15 and
 * let them run.
 *
 * The result will be 120 recorded meters (1 meter for producer one, 2 meter for producer two, ..., 15 meters for
 * producer fifteen).
 */
public class MultiThreadExample {
    public static void main(String[] args) {
        System.setProperty(MeteringContext.METERING_DOMAIN, Domain.Prod.toString());

        // Create 15 ProducerThread with ids from 1 to 15.
        final List<ProducerThread> threadPool = new LinkedList<>();
        int totalMetersToSend = 0;
        for(int i = 1; i <= 15; i++) {
            final ProducerThread thread = new ProducerThread(i);
            threadPool.add(thread);
            thread.start();

            totalMetersToSend+=i;
        }

        // let the threads send the messages.
        sleep();

        // This is basically an arithmetic progression with a sum of n(A1 + An) / 2
        // 15(1 + 15) / 2 = 15 * 16 / 2 = 120
        System.out.println("Total sum: " + totalMetersToSend);

        MeteringContext.flushAndClose();
    }

    private static void sleep() {
        try {
            Thread.sleep(15 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
