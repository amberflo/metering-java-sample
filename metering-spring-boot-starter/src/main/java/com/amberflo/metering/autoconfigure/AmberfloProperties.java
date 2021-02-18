package com.amberflo.metering.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the amberflo metering client. Since the client only supports writes
 * at this point, the write key is the only (and required) property.
 *
 * @author Christopher Smith
 */
@ConfigurationProperties("amberflo.metering")
public class AmberfloProperties {

  private String writeKey;

  public String getWriteKey() {
    return writeKey;
  }

  public void setWriteKey(String writeKey) {
    this.writeKey = writeKey;
  }
}
