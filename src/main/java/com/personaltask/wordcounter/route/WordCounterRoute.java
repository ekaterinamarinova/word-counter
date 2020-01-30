package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Component, holding the route to be executed
 *
 * @author EMarinova
 */
@Component
public class WordCounterRoute extends RouteBuilder {

    private ApplicationProperties properties;

    public WordCounterRoute(ApplicationProperties properties) {
        this.properties = properties;
    }

    public void configure() throws Exception {

        from("quartz2://simpleCron?cron=" + properties.getCronExpressionWorkdaysEachMinute())
                .process("downloadProcessor")
                .split(body())
                    .process("wordCountProcessor")
                    .process("uploadProcessor")
                    .process("moveProcessor")
                .end()
                .process("cleanLocalDirProcessor");

    }
}
