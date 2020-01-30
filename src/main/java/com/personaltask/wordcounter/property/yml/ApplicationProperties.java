package com.personaltask.wordcounter.property.yml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds application specific properties.
 * For example name of directories for local file storage.
 *
 * @author EMarinova
 */
@Configuration
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationProperties {

    private String cronExpressionWorkdaysEachMinute;

    private String generatedFileNamePrefix;

    private String fileDestinationLocal;

    private String downloaded;

    private String counted;

}
