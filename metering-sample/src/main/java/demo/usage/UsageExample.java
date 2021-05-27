package demo.usage;

import com.amberflo.metering.usage.clients.UsageClient;
import com.amberflo.metering.usage.model.request.*;
import com.amberflo.metering.usage.model.response.DetailedMeterAggregation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * An example app to show the main use-cases and rules of the 'Usage-Api'.
 */
public class UsageExample {
    private static final boolean ASCENDING = true;
    public static final String CUSTOMER_ID_FIELD = "customerId";

    public static void main(final String[] args) {
        // Step 1 - create the client
        final String appKey = System.getProperty("AMBERFLO_APP_KEY");
        final UsageClient usageClient = new UsageClient(appKey);

        // Step 2 - query the system

        // If you want simple statistics for all of the official meters in your account use the 'all' api.
        queryTheAllApi(usageClient);

        // If you want to create a custom-query for any specific meter use the "get" api.
        queryTheGetApi(usageClient);

        // For a batch of custom-queries use the "batch" api.
        queryTheGetBatchApi(usageClient);
    }

    private static void queryTheAllApi(final UsageClient usageClient) {
        // Creating a simple 'ALL' api request can be pretty simple.
        // 1. Use one of the TimeRangeFactory methods to easily create a time-range for your query.
        // 2. Use the AllMetersAggregationsRequestBuilder to create a request.
        // 3. Provide the all-request to the usage-client and get the results.
        final TimeRange timeRange1 = TimeRangeFactory.yearToDate();
        final AllMetersAggregationsRequest request1 = AllMetersAggregationsRequestBuilder.instance(timeRange1).build();
        // Each item in the list corresponds to an official 'meter' defined by you in advance.
        final List<DetailedMeterAggregation> meterAggregations1 = usageClient.getAll(request1);

        System.out.println(meterAggregations1);

        // You can also look at X days backwards (for example).
        // The start time of the result will be (now - X days) truncated to the beginning of the day.
        // For example if now is 3/27/2021-02:53 and X = 4 then the result of calling this method is 3/23/2021-00:00.
        final TimeRange timeRange2 = TimeRangeFactory.truncatedLastDays(4);
        final AllMetersAggregationsRequest request2 = AllMetersAggregationsRequestBuilder.instance(timeRange2).build();
        final List<DetailedMeterAggregation> meterAggregations2 = usageClient.getAll(request2);

        // You can also look at X month backwards (for example).
        // The start time of the result will be (now - X days) truncated to the beginning of the MONTH.
        // For example if now is 3/27/2021-02:53 and X = 2 then the result of calling this method is 1/1/2021-00:00.
        final TimeRange timeRange3 = TimeRangeFactory.truncatedLastMonths(2);
        final AllMetersAggregationsRequest request3 = AllMetersAggregationsRequestBuilder.instance(timeRange3).build();
        final List<DetailedMeterAggregation> meterAggregations3 = usageClient.getAll(request3);

        // truncatedLastWeeks and truncatedLastHours have similar truncation logic.
        // The reason we truncate is related to the way the usage-api works. Let's look at the example below and then
        // explain the reason.

        final TimeRange timeRange4 = TimeRangeFactory.truncatedLastDays(4);
        final AllMetersAggregationsRequest request4 =
                AllMetersAggregationsRequestBuilder.instance(timeRange4)
                        .setTimeGroupingInterval(AggregationInterval.DAY).build();
        final List<DetailedMeterAggregation> meterAggregations4 = usageClient.getAll(request4);

        // As you can see in the example above we asked for the results with a TimeGroupingInterval of a day.
        // Asking this will cause the usage api to provide you with statistics for each day of the time range you
        // asked for. For example, if you asked for the time range {3/23/2021-05:00 - today}, then for each meter you
        // will get a statistic for the entire time range as well as a statistic for each day starting from hour 00:00.
        // So basically the usage-api will provide you with statistics for the time-range of {3/23/2021-05:00 - today}.
        // To make it less confusing, we provide you with "truncated" methods which already produce the truncated
        // hour/day/week/month on the client side.



        // The truncated methods can also help you getting the beginning of the week (let's say).
        final TimeRange timeRange5 = TimeRangeFactory.truncatedLastWeeks(0);
        final AllMetersAggregationsRequest request5 =
                AllMetersAggregationsRequestBuilder.instance(timeRange5)
                        .setTimeGroupingInterval(AggregationInterval.DAY).build();
        final List<DetailedMeterAggregation> meterAggregations5 = usageClient.getAll(request5);

        // Using the all api you can ask to filter the meters for a certain customer id.
        final TimeRange timeRange6 = TimeRangeFactory.truncatedLastWeeks(0);
        final AllMetersAggregationsRequest request6 =
                AllMetersAggregationsRequestBuilder.instance(timeRange6)
                        .setFilterByCustomerId("customer-id-123")
                        .setTimeGroupingInterval(AggregationInterval.DAY).build();
        final List<DetailedMeterAggregation> meterAggregations6 = usageClient.getAll(request6);

        // Last, regarding the time-range we will mention that you can define a custom time range for your query.
        // Just notice that the usage-api extends the time range you provide according to the provided
        // AggregationInterval (or to AggregationInterval.Hour if no AggregationInterval was provided).
        // So for example a query with a time of {3/23/2021-05:21 - 3/29/2021-08:21} and a AggregationInterval.DAY
        // will be treated as {3/23/2021-00:00 - 3/30/2021-00:00} and a AggregationInterval.DAY.
        final TimeRange timeRange = TimeRange.builder()
                .startTimeInSeconds(OffsetDateTime.now().minusMonths(2).toEpochSecond())
                .endTimeInSeconds(OffsetDateTime.now().minusMonths(1).toEpochSecond()).build();
        final AllMetersAggregationsRequest request =
                AllMetersAggregationsRequestBuilder.instance(timeRange)
                        .setTimeGroupingInterval(AggregationInterval.DAY).build();
        final List<DetailedMeterAggregation> meterAggregations = usageClient.getAll(request);
    }


    private static void queryTheGetApi(final UsageClient usageClient) {
        // Querying the get api is the custom and more advanced way of querying the usage-api.
        // At the very least you need to provide the: meter-name, aggregation type, and time range.
        final String meterApiName = "myMeter";
        final TimeRange timeRange = TimeRangeFactory.yearToDate();

        final MeterAggregationMetadata request1 =
                MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange).build();
        final DetailedMeterAggregation result1 = usageClient.get(request1);

        System.out.println(result1);

        // You can use all available meter types, just be aware that (currently) Amberflo has indexes only
        // for the official 'AggregationType' of your meter. So querying for a different type might
        // cause the query to be slower.
        final MeterAggregationMetadata request2 =
                MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.MIN, timeRange).build();
        final DetailedMeterAggregation result2 = usageClient.get(request2);

        // You can ask to partition the results by a certain field(s). When doing this Amberflo will provide
        // you with statistics per group.
        final MeterAggregationMetadata request3 =
                MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange)
                        .setGroupBy(List.of(CUSTOMER_ID_FIELD)).build();
        final DetailedMeterAggregation result3 = usageClient.get(request3);


        // Partitioning can yield a lot of results, and there is a size limit of 6Mb for the entire serialized
        // DetailedMeterAggregation response. So you better add a 'take' clause when partitioning.
        // The query below will provide you with statistics for the top 5 customers with the highest meter sum for the
        // time range you provided.
        final MeterAggregationMetadata request4 =
                MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange)
                        .setGroupBy(List.of(CUSTOMER_ID_FIELD))
                        .setTake(new Take(5, !ASCENDING))
                        .build();
        final DetailedMeterAggregation result4 = usageClient.get(request4);


        // As with the 'all' api you can also ask to partition each group of statistics by units of time.
        // In the example below we the usage-api will return you statistics for the top 5 customer, where for each
        // customer you will get statistics for the entire time range, as well as for each day.
        // Just notice that the usage-api extends the time range you provide according to the provided
        // AggregationInterval (or to AggregationInterval.Hour if no AggregationInterval was provided).
        // So for example a query with a time of {3/23/2021-05:21 - 3/29/2021-08:21} and a AggregationInterval.DAY
        // will be treated as {3/23/2021-00:00 - 3/30/2021-00:00} and a AggregationInterval.DAY.
        final MeterAggregationMetadata request5 =
                MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange)
                        .setTimeGroupingInterval(AggregationInterval.DAY)
                        .setGroupBy(List.of(CUSTOMER_ID_FIELD))
                        .setTake(new Take(5, !ASCENDING))
                        .build();
        final DetailedMeterAggregation result5 = usageClient.get(request5);


        // Last we will mention that you can define filters for your query.
        // The example below yields statistics for the top 5 "premium" or "gold" customers in the US.
        // Assuming you have this fields - if you don't the usage api will throw an exception.
        try {
            final MeterAggregationMetadata request6 =
                    MeterAggregationMetadataBuilder.instance(meterApiName, AggregationType.SUM, timeRange)
                            .setTimeGroupingInterval(AggregationInterval.DAY)
                            .setGroupBy(List.of(CUSTOMER_ID_FIELD))
                            .setFilter(Map.of("customerTier", List.of("Premium", "Gold"), "country", List.of("US")))
                            .setTake(new Take(5, !ASCENDING))
                            .build();
            final DetailedMeterAggregation result6 = usageClient.get(request6);
        } catch (final RuntimeException exception) {
            System.out.println(exception);
        }
    }


    private static void queryTheGetBatchApi(final UsageClient usageClient) {
        // The "batch" api is exactly the same as the "get" api only the you provide it with a batch
        // of request and it return you a batch of results.
        final String meterApiName1 = "myMeter1";
        final String meterApiName2= "myMeter2";
        final TimeRange timeRange = TimeRangeFactory.yearToDate();

        final MeterAggregationMetadata request1 =
                MeterAggregationMetadataBuilder.instance(meterApiName1, AggregationType.SUM, timeRange).build();
        final MeterAggregationMetadata request2 =
                MeterAggregationMetadataBuilder.instance(meterApiName2, AggregationType.SUM, timeRange).build();
        final List<DetailedMeterAggregation> result1 = usageClient.getBatch(List.of(request1, request2));

        System.out.println(result1);
    }
}
