package com.amberflo.metering.cli;

import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Converts a string which represents the time since epoch in millis to a {@link LocalDateTime}
 */
class LocalDateTimeConverter implements CommandLine.ITypeConverter<LocalDateTime> {
    private static final LocalDateTime EARLIEST_ALLOWED_TIME = LocalDateTime.now().minusYears(1);
    private static final LocalDateTime LATEST_ALLOWED_TIME = LocalDateTime.now().plusYears(1);

    public LocalDateTime convert(final String millisSinceEpochString) {
        try {
            final long millisSinceEpoch = Long.valueOf(millisSinceEpochString);
            final LocalDateTime time =
                    Instant.ofEpochMilli(millisSinceEpoch).atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Below is a simple check to make sure there are no huge mistakes with the provided time.
            if (time.isBefore(EARLIEST_ALLOWED_TIME) || time.isAfter(LATEST_ALLOWED_TIME)) {
                throw new RuntimeException();
            }

            return time;
        } catch (RuntimeException e) {
            throw new CommandLine.TypeConversionException("'" + millisSinceEpochString + "' has an invalid value");
        }
    }
}
