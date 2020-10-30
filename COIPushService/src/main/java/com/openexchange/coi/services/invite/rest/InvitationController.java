
package com.openexchange.coi.services.invite.rest;

import static java.lang.System.currentTimeMillis;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.ResponseCodeAwareCoiServiceException;
import com.openexchange.coi.services.invite.InviteService;
import com.openexchange.coi.services.invite.rest.body.Invitation;
import com.openexchange.coi.services.invite.storage.mysql.entities.InvitationEntityImpl;
import com.openexchange.coi.services.invite.templating.TemplateService;
import com.openexchange.coi.services.push.rest.PushEndpointController;
import freemarker.template.TemplateException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.glxn.qrgen.javase.QRCode;

/**
 * 
 * {@link InvitationController} defines the rest endpoints used by COI client to manage invitations
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@RestController
@RequestMapping("/invite")
@Profile(Profiles.INVITE)
public class InvitationController {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationController.class);

	private static final String COUNTER = "counter";
    private static final String REQUEST = "request";
    private static final String TIMER = "timer";
    private static final String MTYPE = "mtype";

    private static final String REQ_GET = "get";
    private static final String REQ_GET_WEB = "getWebsite";
    private static final String REQ_REMOVE = "remove";
    private static final String REQ_REGISTER = "register";
    private static final String METRIC_NAME = "com.openexchange.coi.services.invite.rest";

    @Autowired
    private InviteService inviteService;

    @Autowired
    private TemplateService tmplService;

    private Map<String, Timer> timers = new HashMap<>();
    private Map<String, Counter> successCounter = new HashMap<>();
    private Counter errorCounter;

    /**
     * Initializes a new {@link PushEndpointController}.
     */
    public InvitationController(MeterRegistry registry) {
        super();
        timers.put(REQ_REGISTER, Timer.builder(METRIC_NAME).tag(MTYPE, TIMER).tag(REQUEST, REQ_REGISTER).register(registry));
        timers.put(REQ_REMOVE, Timer.builder(METRIC_NAME).tag(MTYPE, TIMER).tag(REQUEST, REQ_REMOVE).register(registry));
        timers.put(REQ_GET, Timer.builder(METRIC_NAME).tag(MTYPE, TIMER).tag(REQUEST, REQ_GET).register(registry));
        timers.put(REQ_GET_WEB, Timer.builder(METRIC_NAME).tag(MTYPE, TIMER).tag(REQUEST, REQ_GET_WEB).register(registry));

        successCounter.put(REQ_REGISTER, Counter.builder(METRIC_NAME).tag(MTYPE, COUNTER).tag(REQUEST, REQ_REGISTER).register(registry));
        successCounter.put(REQ_REMOVE, Counter.builder(METRIC_NAME).tag(MTYPE, COUNTER).tag(REQUEST, REQ_REMOVE).register(registry));
        successCounter.put(REQ_GET, Counter.builder(METRIC_NAME).tag(MTYPE, COUNTER).tag(REQUEST, REQ_GET).register(registry));
        successCounter.put(REQ_GET_WEB, Counter.builder(METRIC_NAME).tag(MTYPE, COUNTER).tag(REQUEST, REQ_GET_WEB).register(registry));

        errorCounter = Counter.builder(METRIC_NAME).tag(MTYPE, COUNTER).tag("type", "error").register(registry);
    }

    /**
     * Creates a new invitation
     *
     * @param body The invitation
     * @return The {@link InvitationEntityImpl}
     * @throws CoiServiceException
     */
    @PutMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Invitation createInvitation(@Valid @RequestBody Invitation body) throws CoiServiceException {
        long start = currentTimeMillis();
        Invitation result = inviteService.storeInvite(body);
        timers.get(REQ_REGISTER).record(Duration.ofMillis(currentTimeMillis() - start));
        successCounter.get(REQ_REGISTER).increment();
        return result;
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
     * Removes the invitation
     *
     * @param id The id of the invitation
     * @throws CoiServiceException
     */
    @DeleteMapping("/{id}")
    public void removeInvitation(@PathVariable("id") String id) throws CoiServiceException {
        long start = currentTimeMillis();
        inviteService.removeInvitation(id);
        timers.get(REQ_REMOVE).record(Duration.ofMillis(currentTimeMillis() - start));
        successCounter.get(REQ_REMOVE).increment();
    }
    
    /**
     * Gets the invitation with the given id
     *
     * @param id The id of the invitation
     * @throws CoiServiceException
     */
    @GetMapping("/{id}")
    public Invitation getInvitation(@PathVariable("id") String id) throws CoiServiceException {
        long start = currentTimeMillis();
        try {
            return inviteService.getInvite(id);
        } finally {
            timers.get(REQ_GET).record(Duration.ofMillis(currentTimeMillis() - start));
            successCounter.get(REQ_GET).increment();
        }
    }

    /**
     * Gets the invitation with the given id as a web-page
     *
     * @param id The id of the invitation
     * @throws CoiServiceException
     * @throws IOException
     * @throws TemplateException
     */
    @GetMapping("/website/{id}")
    public String getInvitationWebsite(@PathVariable("id") String id) throws CoiServiceException {
        long start = currentTimeMillis();
        Invitation invitation = inviteService.getInvite(id);
        try {
            return tmplService.applyData(invitation.getId(), invitation.getSender().getName(), invitation.getSender().getEmail(), invitation.getMessage(), invitation.getSender().getImage() != null ? new String(invitation.getSender().getImage()) : null);
        } finally {
            timers.get(REQ_GET_WEB).record(Duration.ofMillis(currentTimeMillis() - start));
            successCounter.get(REQ_GET_WEB).increment();
        }
    }

    /**
     * Gets a list of {@link Invitation}s. For testing purposes only.
     * 
     * @param from The optional lower limit. Defaults to 0
     * @param to The optional upper limit. Defaults to 50
     * @return A list of {@link Invitation}s
     * @throws CoiServiceException
     */
    @GetMapping("/list")
    public List<Invitation> list(@RequestParam(required = false) Long from, @RequestParam(required = false) Long to) throws CoiServiceException {
        if (from == null) {
            from = 0l;
        }
        if (to == null) {
            to = 50l;
        }
        return inviteService.list(from, to);
    }
    
    /**
     * Creates a qr code
     *
     * @param content The content of the qr code
     * @return The qr code as a base64 string
     */
    @SuppressWarnings("unused")
    private String createQRCode(String content) {
        return QRCode.from(content).stream().toString();
    }

    /**
     * An {@link ExceptionHandler} for {@link PushException}
     *
     * @param ex The {@link PushException}
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
