package vn.dlvn.underwriting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // Allow all origins for external forms (in production, specify exact origins)
        corsConfiguration.addAllowedOriginPattern("*");
        
        // Allow common HTTP methods
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("DELETE");
        corsConfiguration.addAllowedMethod("OPTIONS");
        
        // Allow common headers
        corsConfiguration.addAllowedHeader("*");
        
        // Disable credentials to allow wildcard origins
        corsConfiguration.setAllowCredentials(false);
        
        // How long the browser should cache preflight request results
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        source.registerCorsConfiguration("/v2/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}