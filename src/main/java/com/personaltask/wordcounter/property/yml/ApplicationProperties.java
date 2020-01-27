package com.personaltask.wordcounter.property.yml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationProperties {

    private String cronExpressionWorkdaysEachMinute;

    private String fileDestination;

    private String downloaded;

    private String counted;

    private String generatedFileNamePrefix;
}
