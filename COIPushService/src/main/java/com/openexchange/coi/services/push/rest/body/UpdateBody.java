
package com.openexchange.coi.services.push.rest.body;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;

/**
 * 
 * {@link UpdateBody} - the data needed to update an existing push resource with a new push token
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class UpdateBody {

    @NotNull(message = "pushToken must not be null")
    @Size(max = 256, message = "The push token must not be longer than 256 characters.")
    @Getter
    String pushToken;

    /**
     * Initializes a new {@link UpdateBody}.
     */
    public UpdateBody() {
        super();
    }

    /**
     * 
     * Initializes a new {@link UpdateBody}.
     * 
     * @param pushToken The new push token
     */
    public UpdateBody(String pushToken) {
        super();
        this.pushToken = pushToken;
    }

}
