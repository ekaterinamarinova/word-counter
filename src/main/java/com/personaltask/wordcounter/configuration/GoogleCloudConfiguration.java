package com.personaltask.wordcounter.configuration;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCloudConfiguration {
    @Bean
    public Storage getStorageInstance() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
