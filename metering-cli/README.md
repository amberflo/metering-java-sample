# metering-java
A very simple cli tool for calling the Amberflo.io java client. The tool includes the following params:
1. meter_name - required.
2. app_key - required.
3. customer_id - optional.
4. customer_name - optional.
5. meter_value - optional (default to 1).
6. as_error - optional.

In order to use this tool just run the following command (after running `mvn clean package`):
1. With maven:

```mvn exec:java -Dexec.mainClass=com.amberflo.metering.cli.MeterCommand -m [meter_name] -k [app_key] -v [meter_value] -c [customer_id] -n [customer_name]```

2. With Java Jar command:

```java -jar metering-java-client-cli-[version]-jar-with-dependencies.jar -m [meter_name] -k [app_key] -v [meter_value] -c [customer-id] -n [customer_name]```


To mark the meter as an error related meter simply add `-e` to the exec call.