package com.openexchange.coi.services;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 
 * {@link CoiServiceApplication}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
@EnableScheduling
@EnableJpaRepositories(basePackages = { "com.openexchange.coi.services.push.storage.mysql", "com.openexchange.coi.services.invite.storage.mysql" })
public class CoiServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CoiServiceApplication.class);
    
    @Autowired
    GeneralConfiguration config;

	public static void main(String[] args) {
		LOG.info("Starting coi services....");
        SpringApplication springApplication = new SpringApplication(CoiServiceApplication.class);
        // Add initializer for the default profiles
        springApplication.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {

            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                LOG.info("Settings default profiles to \"" + String.join(",", Profiles.getDefaultProfiles()) + "\"");
                applicationContext.getEnvironment().setDefaultProfiles(Profiles.getDefaultProfiles());

                String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
                if (profiles.length == 1 && profiles[0].equals("kubernetes")) {
                    LOG.info("Identified kubernetes environment with the default profile. Adding default profiles to the active profile.");
                    for (String profile : Profiles.getDefaultProfiles()) {
                        applicationContext.getEnvironment().addActiveProfile(profile);
                    }
                }
            }
        });
        springApplication.run(args);
        LOG.info("Successfully started COI Services");
	}


    /**
     * This factory is used to delegate http request on port 80 to https
     *
     * @return A ConfigurableServletWebServerFactory which delegates requests from http(80) to https
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        if (config.isForceHttps()) {
            LOG.info("Adding SecuredRedirectHandler to redirect http to https");
            factory.addServerCustomizers(new JettyServerCustomizer() {

                @Override
                public void customize(Server server) {
                    final HttpConnectionFactory httpConnectionFactory = server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);
                    final ServerConnector httpConnector = new ServerConnector(server, httpConnectionFactory);
                    int port = 80;
                    httpConnector.setPort(port);
                    server.addConnector(httpConnector);

                    final HandlerList handlerList = new HandlerList();
                    handlerList.addHandler(new SecuredRedirectHandler());
                    for (Handler handler : server.getHandlers()) {
                        handlerList.addHandler(handler);
                    }
                    server.setHandler(handlerList);
                }
            });
        }
        return factory;
    }

}
