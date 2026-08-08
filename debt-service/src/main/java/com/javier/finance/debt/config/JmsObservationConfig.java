
package com.javier.finance.debt.config;

import io.micrometer.observation.ObservationRegistry;
import jakarta.jms.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class JmsObservationConfig {

    @Bean
    JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, ObservationRegistry observationRegistry) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setObservationRegistry(observationRegistry);
        template.setDeliveryPersistent(true);
        return template;
    }

    @Bean(name = "jmsListenerContainerFactory")
    DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            ObservationRegistry observationRegistry) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setObservationRegistry(observationRegistry);
        factory.setConcurrency("1-5");
        return factory;
    }
}
