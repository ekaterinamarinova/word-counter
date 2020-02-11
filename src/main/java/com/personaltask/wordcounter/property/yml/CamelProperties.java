package com.personaltask.wordcounter.property.yml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application-camel")
@Getter
@Setter
public class CamelProperties {

    private Integer maximumRedeliveries;

    private Integer delayInMilliseconds;

}
