package demo.customer_details;

import com.amberflo.metering.customer_details.clients.CustomerDetailsClient;
import com.amberflo.metering.customer_details.model.CustomerDetails;

import java.util.Map;
import java.util.UUID;

/**
 * An example app which shows how to interact with the customer-details api.
 */
public class CustomerDetailsExample {
    public static void main(final String[] args) {
        // create the client
        final String appKey = System.getProperty("AMBERFLO_APP_KEY");
        final CustomerDetailsClient client = new CustomerDetailsClient(appKey);

        // Create a customer is simple - just define a customer:
        // Id must be unique - and this will be the id that you use when ingesting a meter for the customer or
        // querying the usage api.
        final String customerId = UUID.randomUUID().toString();
        // Name is mandatory but not necessarily unique.
        final String customerName = "moishe oofnik"; // https://www.google.com/search?q=moishe+oofnik
        // Traits are optional and suppose to capture additional data about the user.
        final Map<String, String> traits = Map.of("tv-show", "Rechov Sumsum"); // https://www.google.com/search?q=rechov+sumsum

        final CustomerDetails customerDetails = new CustomerDetails(customerId, customerName, traits);
        client.add(customerDetails);

        // You shouldn't be able to add the same client again.
        try {
            client.add(customerDetails);
        } catch (final RuntimeException exception) {
            System.out.println(exception);
        }


        // But you can update it.
        final String customerName2 = "Kippi Ben Kippod"; // https://www.google.com/search?q=kippi+ben+kippod
        final CustomerDetails updatedCustomer = new CustomerDetails(customerId, customerName2, traits);
        client.update(updatedCustomer);

        // If you aren't sure if the customer exists you can query the system or just call
        client.addOrUpdate(customerDetails);

        // As mentioned you can always query the system for a given customer id.
        final CustomerDetails customerDetailsResult = client.get(customerId);
        System.out.println("retrieved customer:");
        System.out.println(customerDetailsResult);
    }
}
