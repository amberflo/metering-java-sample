package demo.customer;

import com.amberflo.metering.common.JsonSerializerFactory;
import com.amberflo.metering.customer.model.invoice.CustomerProductInvoiceKey;
import com.google.gson.Gson;
import com.amberflo.metering.customer.clients.CustomerProductInvoiceClient;
import com.amberflo.metering.customer.model.invoice.DetailedCustomerProductInvoice;

import java.util.List;

/**
 * An example app which shows how to interact with the customer-product-invoice api.
 *
 * NOTICE - you can also implement "pay-as-you-go" policy with your customers using this client (see example 3 for
 * more details).
 */
public class CustomerProductInvoiceExample {
    private static final String CUSTOMER_ID = "5555";
    private static final String PRODUCT_ID = "1";
    private static final boolean FROM_CACHE = true;
    private static final boolean WITH_PAYMENT_STATUS = false;

    public static void main(final String[] args) {
        final Gson gson = JsonSerializerFactory.getGson();

        // create the client
        final String appKey = System.getProperty("AMBERFLO_APP_KEY");
        final CustomerProductInvoiceClient client = new CustomerProductInvoiceClient(appKey);

        // Example 1 - Get all invoices will retrieve you by default all invoices of the default product ("1"),
        // based on fresh usage data (for open invoices) and with the payment status of each invoice.
        final List<DetailedCustomerProductInvoice> allCustomerInvoices1 = client.getAll(CUSTOMER_ID);
        System.out.println("Example 1 - All invoices of product 1, fresh, with payment status");
        System.out.println(gson.toJson(allCustomerInvoices1));

        // Example 2 - You can also specify a different product id, tell the service to use cached data from the
        // last couple of hours (affect only open invoices), or instruct the service to not retrieve fresh invoices.
        // Asking for cached data or not retrieving the payment status results in better performance which might
        // also lower the costs of using amberflo.

        final List<DetailedCustomerProductInvoice> allCustomerInvoices2 =
                client.getAll(CUSTOMER_ID, PRODUCT_ID, FROM_CACHE, WITH_PAYMENT_STATUS);
        System.out.println("Example 2 - All invoices of product 1, from cache, and without payment status");
        System.out.println(gson.toJson(allCustomerInvoices2));

        // Example 3 - you can also ask for the latest invoice of the default product. This time it retrieve you
        // only the latest invoice, based on fresh usage data, but without a payment status.
        // Not returning the payment status, should make sense in most cases as the current latest invoice is most
        // likely an open invoice.
        final DetailedCustomerProductInvoice latestInvoice1 = client.getLatest(CUSTOMER_ID);
        System.out.println("Example 3 - latest invoice of product 1, fresh, and without payment status");
        System.out.println(gson.toJson(latestInvoice1));

        // Notice that the latest OPEN invoice gives you a view for the available amount of pay-as-you-go money for the
        // current latest invoice (how much more can be spent for the current invoice using prepaid or the plan-free
        // tier money).
        System.out.println("Available pay-as-you-go money (real currency):");
        System.out.println(latestInvoice1.getAvailablePayAsYouGoMoney());
        System.out.println("Available pay-as-you-go money (credit units):");
        System.out.println(latestInvoice1.getAvailablePayAsYouGoMoneyInCredits());

        // Example 4 - you can also ask for the latest invoice of the default product. This time it retrieve you
        // only the latest invoice, based on fresh usage data, but without a payment status.
        // Not returning the payment status, should make sense in most cases as the current latest invoice is most
        // likely an open invoice.
        final DetailedCustomerProductInvoice latestInvoice2 =
                client.getLatest(CUSTOMER_ID, PRODUCT_ID, FROM_CACHE, WITH_PAYMENT_STATUS);
        System.out.println("Example 4 - latest invoice of product 1, from cache, and without payment status");
        System.out.println(gson.toJson(latestInvoice2));

        // Example 5 - You can also get a specific invoice by it key.
        // You can create the key manually.
        final CustomerProductInvoiceKey invoiceKey =
                CustomerProductInvoiceKey.builder().customerId(CUSTOMER_ID).productId(PRODUCT_ID)
                        .productPlanId("PLAN_ID").month(1).day(1).year(2022).build();
        final DetailedCustomerProductInvoice invoice1 = client.get(invoiceKey);
        System.out.println("Example 5 - specific invoice - key created manually, fresh, and with payment status");
        System.out.println(gson.toJson(invoice1));

        // Example 6 - But you can also grab the key from the
        if (allCustomerInvoices1 != null && allCustomerInvoices1.size() > 0) {
            final CustomerProductInvoiceKey invoiceKey2 = allCustomerInvoices1.get(0).getInvoiceKey();
            final DetailedCustomerProductInvoice invoice2 = client.get(invoiceKey2);
            System.out.println(
                    "Example 6 - specific invoice - key from the all invoices-list, fresh, and with payment status");
            System.out.println(gson.toJson(invoice2));
        }
    }
}
