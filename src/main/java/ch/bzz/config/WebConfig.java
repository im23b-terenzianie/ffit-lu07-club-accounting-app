package ch.bzz.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web Configuration for HTTP to HTTPS Redirect
 * 
 * This configuration enables the application to:
 * 1. Run HTTPS on port 8443 (primary)
 * 2. Accept HTTP requests on port 8080
 * 3. Automatically redirect HTTP (8080) to HTTPS (8443)
 * 
 * After configuration:
 * - http://localhost:8080/api/hello  -> redirects to https://localhost:8443/api/hello
 * - https://localhost:8443/api/hello -> works directly
 * 
 * This allows GitHub Pages (HTTPS) to call your API via HTTPS
 * while still supporting HTTP for backward compatibility.
 */
@Slf4j
@Configuration
public class WebConfig {

    /**
     * Configure Tomcat with HTTP to HTTPS redirect
     * - Primary connector: HTTPS on port 8443 (configured in application.properties)
     * - Additional connector: HTTP on port 8080 (redirects to HTTPS)
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // Configure security constraint to redirect HTTP to HTTPS
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
                
                log.info("HTTP to HTTPS redirect configured");
            }
        };

        // Add HTTP connector on port 8080 that redirects to HTTPS port 8443
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        
        return tomcat;
    }

    /**
     * Create HTTP connector on port 8080
     * This connector accepts HTTP requests and redirects them to HTTPS
     */
    private Connector redirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443); // Redirect to HTTPS port
        
        log.info("HTTP connector configured on port 8080 (redirects to 8443)");
        
        return connector;
    }
}
