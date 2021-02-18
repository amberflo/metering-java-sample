package com.amberflo.metering.autoconfigure;

import com.amberflo.metering.Metering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot autoconfiguration class for amberflo metering.
 *
 * @author Christopher Smith
 */
@Configuration
@EnableConfigurationProperties(AmberfloProperties.class)
@ConditionalOnProperty("amberflo.metering.writeKey")
public class AmberfloMeteringAutoConfiguration {

  @Autowired private AmberfloProperties properties;

  @Bean
  public Metering amberflometering() {
    return Metering.builder(properties.getWriteKey()).build();
  }
}
