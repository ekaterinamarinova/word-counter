package com.personaltask.wordcounter.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExceptionLoggingProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggingProcessor.class);

    public static final String NAME = "exceptionLoggingProcessor";

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        LOGGER.error("ERROR: " + e.getMessage(), e.getCause());
    }
}
