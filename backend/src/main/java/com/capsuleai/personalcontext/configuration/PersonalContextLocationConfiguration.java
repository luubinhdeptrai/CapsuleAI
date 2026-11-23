package com.capsuleai.personalcontext.configuration;

import com.capsuleai.personalcontext.adapter.out.persistence.JdbcLocationRepository;
import com.capsuleai.personalcontext.adapter.out.weather.OpenMeteoWeatherProvider;
import com.capsuleai.personalcontext.application.EnvironmentalContextService;
import com.capsuleai.personalcontext.application.ReviseLocationService;
import com.capsuleai.personalcontext.application.ViewLocationContextService;
import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Wires US-CTX-001: location persistence, the bounded weather adapter (or a NOT_CONFIGURED provider when
 * disabled) and the location use cases. Reuses the application's single Clock and the Personal Context
 * transaction port. Creating the adapter opens no connection.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PersonalContextWeatherProperties.class)
public class PersonalContextLocationConfiguration {

    @Bean
    LocationRepository locationRepository(JdbcClient jdbcClient) {
        return new JdbcLocationRepository(jdbcClient);
    }

    /** The application context closes the adapter (AutoCloseable), which stops its acquisition threads. */
    @Bean
    WeatherProvider weatherProvider(PersonalContextWeatherProperties properties, Clock clock) {
        if (!properties.enabled()) {
            return query -> new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.NOT_CONFIGURED);
        }
        return new OpenMeteoWeatherProvider(properties.baseUrl(), properties.acquisitionDeadline(),
                properties.maximumConcurrentRequests(), properties.maximumResponseBytes(), clock);
    }

    @Bean
    EnvironmentalContextService environmentalContextService(LocationRepository locationRepository,
                                                            WeatherProvider weatherProvider,
                                                            PersonalContextTransactions personalContextTransactions,
                                                            Clock clock) {
        return new EnvironmentalContextService(locationRepository, weatherProvider, personalContextTransactions, clock);
    }

    @Bean
    ViewLocationContextService viewLocationContextService(LocationRepository locationRepository,
                                                          EnvironmentalContextService environmentalContextService) {
        return new ViewLocationContextService(locationRepository, environmentalContextService);
    }

    @Bean
    ReviseLocationService reviseLocationService(LocationRepository locationRepository,
                                                EnvironmentalContextService environmentalContextService,
                                                PersonalContextTransactions personalContextTransactions, Clock clock) {
        return new ReviseLocationService(locationRepository, environmentalContextService, personalContextTransactions,
                clock);
    }
}
