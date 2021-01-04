package com.personaltask.wordcounter.configuration;

import org.apache.camel.CamelContext;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfiguration {
    @Bean
    public QuartzComponent quartzComponent(CamelContext camelContext) {
        QuartzComponent quartz2 = camelContext.getComponent("quartz2", QuartzComponent.class);
        quartz2.setInterruptJobsOnShutdown(true);
        return quartz2;
    }
}
