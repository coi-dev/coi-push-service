
package com.openexchange.coi.services.push.rest;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.exception.ResponseCodeAwareCoiServiceException;
import com.openexchange.coi.services.push.PushService;
import com.openexchange.coi.services.push.rest.VAPIDValidator.PublicKeySource;
import com.openexchange.coi.services.push.rest.util.SizeLimitInputStream;
import com.openexchange.coi.services.push.rest.util.Utils;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.validator.IsUUID;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 
 * {@link PushEndpointController} defines the rest endpoint used by the COI server
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@RestController
@RequestMapping("/push")
@Validated
@Profile(Profiles.PUSH)
public class PushEndpointController {

    private static final String TYPE = "type";
    private static final String METRIC_TYPE = "mtype";
    private static final String METRIC_TIMER = "com.openexchange.coi.services.push.rest.push";
    private static final Logger LOG = LoggerFactory.getLogger(PushEndpointController.class);

    /**
     * The payload overhead in bytes, which effectively reduces the maximum possible size of a payload a coi server is allowed to sent.
     */
    private static final int PAYLOAD_OVERHEAD = 7;

    /**
     * The max payload size in bytes of the push service. Usually 4KB (firebase)
     */
    private static final int MAX_PAYLOAD_SIZE = 4000;

    @Autowired
    private PushService pushService;

    @Autowired
    private VAPIDValidator vapidValidator;

    @Autowired
    private Utils utils;

    private Timer timer;
    private Counter successfullCounter;
    private Counter errorCounter;

    /**
     * Initializes a new {@link PushEndpointController}.
     */
    public PushEndpointController(MeterRegistry registry) {
        super();
        timer = Timer.builder(METRIC_TIMER).tag(METRIC_TYPE, "timer").register(registry);
        successfullCounter = Counter.builder(METRIC_TIMER).tag(METRIC_TYPE, "count").tag(TYPE, "success").register(registry);
        errorCounter = Counter.builder(METRIC_TIMER).tag(METRIC_TYPE, "count").tag(TYPE, "error").register(registry);
    }

    /**
     * 
     * {@link PushResourceHolder}
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v1.0.0
     */
    private class PushResourceHolder {

        private PushResource pushResource;
        private String id;

        private PushResourceHolder(String id) {
            this.id = id;
        }

        public PushResource getPushResource() throws CoiServiceException {
            if (pushResource == null) {
                pushResource = pushService.findById(id);
            }
            return pushResource;
        }
    }

    /**
     * The push endpoint used by the COI server to deliver push messages
     *
     * @param id The id of the push resource
     * @param requestEntity The request containing the data
     * @throws CoiServiceException
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, path = "/send/{id}")
    public void push(@PathVariable("id") @IsUUID String id, HttpServletRequest requestEntity) throws CoiServiceException {
        long start = System.currentTimeMillis();
        final PushResourceHolder res = new PushResourceHolder(id);

        // Validate vapid header
        Date t0 = new Date();
        vapidValidator.validateRequest(requestEntity, new PublicKeySource() {

            @Override
            public byte[] getPublicKey() throws CoiServiceException {
                // We defer loading the push-resource up until the point where we need to lookup the public key for VAPID validation
                return res.getPushResource().getPublicKey();
            }

        });
        Date t1 = new Date();
        LOG.debug("Took {} ms to validate VAPID token.", t1.getTime() - t0.getTime());

        try {
            byte[] data = IOUtils.toByteArray(new SizeLimitInputStream(requestEntity.getInputStream(), MAX_PAYLOAD_SIZE - PAYLOAD_OVERHEAD));
            if (data.length == 0) {
                throw CoiServiceExceptionCodes.INVALID_REQUEST_BODY.create();
            }

            PushResource resource = res.getPushResource();
            pushService.sendPushToDevice(resource, data);
            if (resource.isValid() == false) {
                utils.setExpiryDate(resource, true);
                resource.setValid(true);
                pushService.save(resource);
                LOG.debug("Properly saved push resource");
            }
            timer.record(Duration.ofMillis(System.currentTimeMillis() - start));
            successfullCounter.increment();
        } catch (IOException e) {
            if (e.getCause() instanceof CoiServiceException) {
                throw (CoiServiceException) e.getCause();
            }
            throw CoiServiceExceptionCodes.INVALID_REQUEST_BODY.create();
        }
    }

    /**
     * An {@link ExceptionHandler} for {@link PushException}
     *
     * @param ex The {@link CoiServiceException}
     * @return A map containing informations about the error
     */
    @ExceptionHandler(CoiServiceException.class)
    public ResponseEntity<Object> handlePushExceptions(CoiServiceException e) {
        errorCounter.increment();
        e.log(LOG);
        if (CoiServiceExceptionCodes.INVALID_TRANSPORT.equals(e)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The transport of this subscription is not supported anymore");
        }
        if (CoiServiceExceptionCodes.QUOTA_EXCEEDED.equals(e)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", e.getArgs().get(0).toString()).build();
        }
        if (e instanceof ResponseCodeAwareCoiServiceException) {
            return ResponseEntity.status(((ResponseCodeAwareCoiServiceException) e).getStatus()).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to send push notification");// Fallback error
    }

    /**
     * An {@link ExceptionHandler} for {@link ConstraintViolationException}s
     *
     * @param ex The {@link ConstraintViolationException}
     * @return A map containing informations about the error
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleValidationExceptions(ConstraintViolationException ex) {
        errorCounter.increment();
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach((error) -> {
            final String fieldName = error.getPropertyPath().toString();
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
