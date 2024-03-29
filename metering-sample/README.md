# Metering Sample
This repo describes in details how to setup the metering-java client, and how interact with it.

## Ingest - Detailed Examples
See the following apps for more detailed examples and additional features:
1. **MeteringExamples** - Describes the basic steps of using the meter client and sending meters, and the different 
   ways you can call the metering-service (not including thread-context).
2. **MeteringInDevExample** - Describes the different meters-clients (direct and stdout). More specifically it shows 
   that you can send the meters to std-out while you are in 'dev' env.
3. **MultiThreadExample** - In this example we simulated a multi-threads service. This example demonstrates that its 
   easy and safe to call the meter service from many threads.
4. **ThreadContextExample** - This example shows how to define common attributes to be shared by many related meters 
   (user id, session id, etc).

## Usage - Detailed Examples
1. **UsageExample** - Describes the main use-cases and rules of the 'Usage-Api'.

## Customer - Detailed Examples
1. **CustomerDetailsExample** - An example app which shows how to interact with the customer-details api.
2. **CustomerProductInvoiceExample** - An example app which shows how to interact with the customer-product-invoice api.