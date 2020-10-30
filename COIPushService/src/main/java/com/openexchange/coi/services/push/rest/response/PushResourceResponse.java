package com.openexchange.coi.services.push.rest.response;

import java.util.Base64;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.push.storage.mysql.entities.PushResourceImpl;
import lombok.Getter;

/**
 * 
 * {@link PushResourceResponse} is like a {@link PushResourceImpl} extended by an endpoint field
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class PushResourceResponse {

    @Getter
    private final String endpoint;
    @Getter
    private final String id;
    @Getter
    private final String appId;
    @Getter
    private final String pushToken;
    @Getter
    private final Long lastModified;
    @Getter
    private final Long expireDate;
    @Getter
    private final String transport;
    @Getter
    private final String publicKey;

    /**
     * Initializes a new {@link PushResourceResponse}.
     * 
     * @param res The {@link PushResource}
     * @param endpoint The endpoint url
     */
    public PushResourceResponse(PushResource res, String endpoint) {
        super();
        id = res.getId();
        appId = res.getAppId();
        expireDate = res.getExpireDate().getTime();
        lastModified = res.getLastModified().getTime();
        pushToken = res.getPushToken();
        transport = res.getTransport();
        publicKey = new String(Base64.getEncoder().encodeToString(res.getPublicKey()));
        this.endpoint = endpoint;
    }
    
}
