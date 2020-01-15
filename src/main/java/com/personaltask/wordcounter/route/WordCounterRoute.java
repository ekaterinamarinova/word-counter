package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WordCounterRoute extends RouteBuilder {

    private ApplicationProperties properties;

    public WordCounterRoute(ApplicationProperties properties) {
        this.properties = properties;
    }

    public void configure() throws Exception {
        from("quartz2://simpleCron?cron=" + properties.getCron())
                .process("downloadProcessor")
                .split(body())
                .log("${body}");
    }
}
