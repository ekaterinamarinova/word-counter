package com.personaltask.wordcounter.property.yml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds the properties specific for the Google Cloud Storage configuration.
 *
 * @author EMarinova
 */
@Configuration
@ConfigurationProperties(prefix = "google-storage")
@Getter
@Setter
public class GoogleCloudProperties {

    private String fileNamePrefix;

    private String bucket;

    private String inbound;

    private String done;

    private String outbound;

    private String error;

    private String ext;
}
