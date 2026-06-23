package de.eecc.dcp.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.eecc.dcp.api.DcpPresentation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DcpPresentation.class)
@EnableConfigurationProperties(DcpProperties.class)
public class DcpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DcpPresentation dcpPresentation(DcpProperties properties, ObjectProvider<ObjectMapper> objectMapper) {
        var builder = DcpPresentation.builder().options(properties.toOptions());
        objectMapper.ifAvailable(ignored -> {
            // ObjectMapper wiring will be added when message serialization is implemented.
        });
        return builder.build();
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
    @ConditionalOnMissingBean
    public DcpExceptionHandler dcpExceptionHandler() {
        return new DcpExceptionHandler();
    }
}
