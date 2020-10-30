
package com.openexchange.coi.services.push.rest;

import static java.lang.System.currentTimeMillis;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.openexchange.coi.services.EndpointService;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.exception.ResponseCodeAwareCoiServiceException;
import com.openexchange.coi.services.push.PushService;
import com.openexchange.coi.services.push.crypto.keys.parsing.PublicKeyParser;
import com.openexchange.coi.services.push.rest.body.PushResourceBody;
import com.openexchange.coi.services.push.rest.body.UpdateBody;
import com.openexchange.coi.services.push.rest.response.PushResourceResponse;
import com.openexchange.coi.services.push.rest.util.Utils;
import com.openexchange.coi.services.push.storage.DefaultPushResource;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.push.storage.mysql.entities.PushResourceImpl;
import com.openexchange.coi.services.validator.IsUUID;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 
 * {@link PushResourceController} defines the rest endpoints used by COI client to manage push resources
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@RestController
@RequestMapping("/push/resource")
@Validated
@Profile(Profiles.PUSH)
public class PushResourceController {

    // Some metric constants
    private static final String TAG_REQUEST = "request";
    private static final String METRIC_COUNTER = "counter";
    private static final String METRIC_TIMER = "timer";
    private static final String TAG_TYPE = "mtype";
    private static final String REQ_GET = "get";
    private static final String REQ_REMOVE = "remove";
    private static final String REQ_UPDATE = "update";
    private static final String REQ_REGISTER = "register";
    private static final String METRIC_NAME = "com.openexchange.coi.services.push.rest";
    private static final Logger LOG = LoggerFactory.getLogger(PushResourceController.class);

    private static final String PUSH_URI_PATH = "/push/send/";

    @Autowired
    Environment environment;
    @Autowired
    private PushService pushSevice;
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private Utils utils;

    private Map<String, Timer> timers = new HashMap<>();
    private Map<String, Counter> successCounter = new HashMap<>();
    private Counter errorCounter;

    /**
     * Initializes a new {@link PushEndpointController}.
     */
    public PushResourceController(MeterRegistry registry) {
        super();
        timers.put(REQ_REGISTER, Timer.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_TIMER).tag(TAG_REQUEST, REQ_REGISTER).register(registry));
        timers.put(REQ_UPDATE, Timer.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_TIMER).tag(TAG_REQUEST, REQ_UPDATE).register(registry));
        timers.put(REQ_REMOVE, Timer.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_TIMER).tag(TAG_REQUEST, REQ_REMOVE).register(registry));
        timers.put(REQ_GET, Timer.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_TIMER).tag(TAG_REQUEST, REQ_GET).register(registry));

        successCounter.put(REQ_REGISTER, Counter.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_COUNTER).tag(TAG_REQUEST, REQ_REGISTER).register(registry));
        successCounter.put(REQ_UPDATE, Counter.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_COUNTER).tag(TAG_REQUEST, REQ_UPDATE).register(registry));
        successCounter.put(REQ_REMOVE, Counter.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_COUNTER).tag(TAG_REQUEST, REQ_REMOVE).register(registry));
        successCounter.put(REQ_GET, Counter.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_COUNTER).tag(TAG_REQUEST, REQ_GET).register(registry));

        errorCounter = Counter.builder(METRIC_NAME).tag(TAG_TYPE, METRIC_COUNTER).tag("type", "error").register(registry);
    }

    /**
     * Extracts and validates the public
     *
     * @param body The {@link PushResourceBody} which contains the public key
     * @return The public key
     * @throws CoiServiceException In case the public key is missing or invalid
     */
    private byte[] getPublicKey(PushResourceBody body) throws CoiServiceException {
        PublicKey key = new PublicKeyParser().parse(body.getPublicKey());
        return key.getEncoded();
    }

    /**
     * 
     * Creates a new push resource
     *
     * @param body The push resource to register
     * @return The {@link PushResourceResponse}
     * @throws CoiServiceException
     */
    @PutMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public PushResourceResponse registerPushResource(@Valid @RequestBody PushResourceBody body) throws CoiServiceException {
        long start = currentTimeMillis();
        PushResource reg = new DefaultPushResource();
        reg.setAppId(body.getAppId());
        reg.setPushToken(body.getPushToken());
        reg.setLastModified(new Date());
        reg.setPublicKey(getPublicKey(body));
        reg.setTransport(body.getTransport());
        utils.setExpiryDate(reg, false);
        checkPushRegistration(reg);
        PushResource save = pushSevice.save(reg);
        try {
            return new PushResourceResponse(save, createEntpoint(save.getId()));
        } finally {
            timers.get(REQ_REGISTER).record(Duration.ofMillis(currentTimeMillis() - start));
            successCounter.get(REQ_REGISTER).increment();
        }
    }

    /**
     * Checks for required fields of a {@link PushResourceImpl}
     *
     * @param reg The {@link PushResourceImpl} to check
     * @throws CoiServiceException
     * @throws ResponseStatusException In case the resource is missing a required field.
     */
    private void checkPushRegistration(PushResource reg) throws CoiServiceException {
        if (reg.getAppId() == null) {
            throw CoiServiceExceptionCodes.MISSING_FIELD.create("appId");
        }
        if (reg.getPushToken() == null) {
            throw CoiServiceExceptionCodes.MISSING_FIELD.create("pushToken");
        }
        if (reg.getTransport() == null) {
            throw CoiServiceExceptionCodes.MISSING_FIELD.create("transport");
        }

        if (false == pushSevice.isAvailable(reg.getTransport())) {
            throw CoiServiceExceptionCodes.INVALID_TRANSPORT.create(reg.getTransport());
        }
    }

    /**
     * Creates an endpoint field from the host and the push resource id
     *
     * @param uid The push resource id
     * @return The endpoint
     */
    private String createEntpoint(String uid) {
        return endpointService.getEndpoint(PUSH_URI_PATH + uid).toString();
    }

    /**
     * Updates the push token and/or extends the expiry date
     *
     * @param id The id of the push resource
     * @param update The optional {@link UpdateBody} containing the push token
     * @throws CoiServiceException
     */
    @PatchMapping("/{id}")
    public PushResourceResponse updatePushToken(@PathVariable("id") @IsUUID String id, @Valid @RequestBody(required = false) UpdateBody update) throws CoiServiceException {
        long start = currentTimeMillis();
        PushResource old = pushSevice.findById(id);
        PushResource res = old.clone();
        if (update != null) {
            res.setPushToken(update.getPushToken());
        }
        res.setLastModified(new Date());
        boolean needsUpdate = false;
        if (old.isValid()) {
            needsUpdate = utils.setExpiryDate(res, true);
        }
        needsUpdate |= update != null;
        if (needsUpdate == false) {
            // Nothing to do
            successCounter.get(REQ_UPDATE).increment();
            timers.get(REQ_UPDATE).record(Duration.ofMillis(currentTimeMillis() - start));
            return new PushResourceResponse(res, createEntpoint(id));
        }
        PushResource result = pushSevice.update(old, res);
        successCounter.get(REQ_UPDATE).increment();
        timers.get(REQ_UPDATE).record(Duration.ofMillis(currentTimeMillis() - start));
        return new PushResourceResponse(result, createEntpoint(id));
    }

    /**
     * An {@link ExceptionHandler} for {@link MethodArgumentNotValidException}
     *
     * @param ex The {@link MethodArgumentNotValidException}
     * @return A map containing informations about the error
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        errorCounter.increment();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
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

    /**
     * Removes the push resource
     *
     * @param id The id of the push resource
     * @throws CoiServiceException
     */
    @DeleteMapping("/{id}")
    public void removePushResource(@PathVariable("id") @IsUUID String id) throws CoiServiceException {
        long start = currentTimeMillis();
        PushResource res = pushSevice.findById(id);
        pushSevice.delete(res);
        timers.get(REQ_REMOVE).record(Duration.ofMillis(currentTimeMillis() - start));
        successCounter.get(REQ_REMOVE).increment();
    }

    /**
     * Gets the push resource with the given id
     *
     * @param id The id of the push resource
     * @throws CoiServiceException
     */
    @GetMapping("/{id}")
    public PushResourceResponse getPushResource(@PathVariable("id") @IsUUID String id) throws CoiServiceException {
        long start = currentTimeMillis();
        PushResource res = pushSevice.findById(id);
        try {
            return new PushResourceResponse(res, createEntpoint(res.getId()));
        } finally {
            timers.get(REQ_GET).record(Duration.ofMillis(currentTimeMillis() - start));
            successCounter.get(REQ_GET).increment();
        }
    }

    /**
     * Gets a list of push resources. For testing purposes only.
     * 
     * @param from The optional lower limit. Defaults to 0
     * @param to The optional upper limit. Default to 50
     * @return A list of push resources
     * @throws CoiServiceException
     */
    @GetMapping("/list")
    public List<PushResource> list(@RequestParam(required = false) Long from, @RequestParam(required = false) Long to) throws CoiServiceException {
        if (from == null) {
            from = 0l;
        }
        if (to == null) {
            to = 50l;
        }
        return pushSevice.list(from, to);
    }

    /**
     * An {@link ExceptionHandler} for {@link CoiServiceException}
     *
     * @param ex The {@link CoiServiceException}
     * @return A map containing informations about the error
     */
    @ExceptionHandler(CoiServiceException.class)
    public ResponseEntity<Object> handlePushException(CoiServiceException e) {
        errorCounter.increment();
        e.log(LOG);
        if (e instanceof ResponseCodeAwareCoiServiceException) {
            return ResponseEntity.status(((ResponseCodeAwareCoiServiceException) e).getStatus()).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown error");// Fallback error
    }

}
