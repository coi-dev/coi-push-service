
package com.openexchange.coi.services.push.rest.body;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.openexchange.coi.services.push.rest.validator.EnumValidator;
import com.openexchange.coi.services.push.transport.Transport;
import lombok.Getter;

/**
 * 
 * {@link PushResourceBody} contains the data needed to register a push resource
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class PushResourceBody {

    @NotBlank(message = "appId must not be null or empty")
    @Size(max = 128, message = "The app id must not be longer than 128 characters.")
    @Getter
    String appId;
    @NotBlank(message = "pushToken must not be null or empty")
    @Size(max = 256, message = "The push token must not be longer than 256 characters.")
    @Getter
    String pushToken;
    @NotNull(message = "transport must not be null")
    @EnumValidator(enumClazz = Transport.class, message = "The transport is invalid")
    @Getter
    String transport = Transport.firebase.name();
    @NotNull(message = "publicKey must not be null")
    @Getter
    String publicKey;

    /**
     * Initializes a new {@link PushResourceBody}.
     */
    public PushResourceBody() {
        super();
    }

    /**
     * 
     * Initializes a new {@link PushResourceBody}.
     * 
     * @param id The id
     * @param appId The app identifier
     * @param pushToken The push token
     * @param transport The name of the transport to use
     * @param publicKey The public key of the coi server
     */
    public PushResourceBody(String id, String appId, String pushToken, String transport, String publicKey) {
        super();
        this.appId = appId;
        this.pushToken = pushToken;
        this.transport = transport;
        this.publicKey = publicKey;
    }

}
