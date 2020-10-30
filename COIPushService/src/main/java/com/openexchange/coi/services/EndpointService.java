
package com.openexchange.coi.services;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@link EndpointService} builds end points for accessing COI Services.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
@Service
public class EndpointService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointService.class);

    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static final String PORT_PROP = "server.port";
    private static final String SCHEME_PROP = "server.ssl.key-store";

    @Autowired
    private Environment environment;

    @Autowired
    private GeneralConfiguration configuration;

    /**
     * Creates an end point.
     * 
     * @return The URL to an end point
     */
    public URL getEndpoint() {
        return getEndpoint(null);
    }

    /**
     * Creates an end point.
     * 
     * @param path The path to the end point
     * @return The full URL to an end point with the given path
     */
    public URL getEndpoint(String path) {
        try {
            // @formatter:off
			return new URIBuilder()
					.setScheme(StringUtils.isEmpty(environment.getProperty(SCHEME_PROP)) ? HTTP : HTTPS)
					.setHost(configuration.getHostName())
					.setPort(Integer.parseInt(environment.getRequiredProperty(PORT_PROP)))
					.setPath(path)
					.build()
					.toURL();
			// @formatter:on
        } catch (NumberFormatException | URISyntaxException | MalformedURLException e) {
            LOG.error("Unable to create endpoint url: " + e.getMessage(), e);
            throw new IllegalArgumentException("Unabled to create endpoint URI.", e);
        }
    }
}
