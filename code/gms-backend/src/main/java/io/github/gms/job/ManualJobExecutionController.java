package io.github.gms.job;

import io.github.gms.common.abstraction.GmsController;
import io.github.gms.common.enums.EventTarget;
import io.github.gms.common.enums.MdcParameter;
import io.github.gms.common.types.AuditTarget;
import io.github.gms.common.util.MdcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.gms.common.util.Constants.ROLE_ADMIN;
import static io.github.gms.common.util.Constants.TRUE;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@RestController
@AuditTarget(EventTarget.ANNOUNCEMENT)
@RequestMapping("/secure/job_execution")
@PreAuthorize(ROLE_ADMIN)
public class ManualJobExecutionController implements GmsController {

    static final String EVENT_MAINTENANCE = "/event_maintenance";
    static final String GENERATED_KEYSTORE_CLEANUP = "/generated_keystore_cleanup";
    static final String JOB_MAINTENANCE = "/job_maintenance";
    static final String MESSAGE_CLEANUP = "/message_cleanup";
    static final String SECRET_ROTATION = "/secret_rotation";
    static final String USER_ANONYMIZATION = "/user_anonymization";
    static final String USER_DELETION = "/user_deletion";
    static final String LDAP_USER_SYNC = "/ldap_user_sync";

    private final EventMaintenanceJob eventMaintenanceJob;
    private final GeneratedKeystoreCleanupJob generatedKeystoreCleanupJob;
    private final JobMaintenanceJob jobMaintenanceJob;
    private final MessageCleanupJob messageCleanupJob;
    private final SecretRotationJob secretRotationJob;
    private final UserAnonymizationJob userAnonymizationJob;
    private final UserDeletionJob userDeletionJob;
    private final LdapUserSyncJob ldapUserSyncJob;

    @Autowired
    public ManualJobExecutionController(EventMaintenanceJob eventMaintenanceJob,
                                        GeneratedKeystoreCleanupJob generatedKeystoreCleanupJob,
                                        JobMaintenanceJob jobMaintenanceJob,
                                        MessageCleanupJob messageCleanupJob,
                                        SecretRotationJob secretRotationJob,
                                        UserAnonymizationJob userAnonymizationJob,
                                        UserDeletionJob userDeletionJob,
                                        @Autowired(required = false) LdapUserSyncJob ldapUserSyncJob) {
        this.eventMaintenanceJob = eventMaintenanceJob;
        this.generatedKeystoreCleanupJob = generatedKeystoreCleanupJob;
        this.jobMaintenanceJob = jobMaintenanceJob;
        this.messageCleanupJob = messageCleanupJob;
        this.secretRotationJob = secretRotationJob;
        this.userAnonymizationJob = userAnonymizationJob;
        this.userDeletionJob = userDeletionJob;
        this.ldapUserSyncJob = ldapUserSyncJob;
    }

    @FunctionalInterface
    protected interface JobExecutor {
        void execute();
    }

    @GetMapping(EVENT_MAINTENANCE)
    public ResponseEntity<Void> eventMaintenance() {
        return runJob(eventMaintenanceJob::run);
    }

    @GetMapping(GENERATED_KEYSTORE_CLEANUP)
    public ResponseEntity<Void> generatedKeystoreCleanup() {
        return runJob(generatedKeystoreCleanupJob::run);
    }

    @GetMapping(JOB_MAINTENANCE)
    public ResponseEntity<Void> jobMaintenance() {
        return runJob(jobMaintenanceJob::run);
    }

    @GetMapping(MESSAGE_CLEANUP)
    public ResponseEntity<Void> messageCleanup() {
        return runJob(messageCleanupJob::run);
    }

    @GetMapping(SECRET_ROTATION)
    public ResponseEntity<Void> secretRotation() {
        return runJob(secretRotationJob::run);
    }

    @GetMapping(USER_ANONYMIZATION)
    public ResponseEntity<Void> userAnonymization() {
        return runJob(userAnonymizationJob::run);
    }

    @GetMapping(USER_DELETION)
    public ResponseEntity<Void> userDeletion() {
        return runJob(userDeletionJob::run);
    }

    @GetMapping(LDAP_USER_SYNC)
    public ResponseEntity<Void> ldapUserSync() {
        if (ldapUserSyncJob == null) {
            return ResponseEntity.notFound().build();
        }

        return runJob(ldapUserSyncJob::run);
    }

    private ResponseEntity<Void> runJob(JobExecutor jobExecutor) {
        MdcUtils.put(MdcParameter.MANUAL_JOB_EXECUTION, TRUE);

        jobExecutor.execute();

        MdcUtils.remove(MdcParameter.MANUAL_JOB_EXECUTION);

        return ResponseEntity.ok().build();
    }
}
