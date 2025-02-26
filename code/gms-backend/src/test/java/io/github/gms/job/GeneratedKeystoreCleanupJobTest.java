package io.github.gms.job;

import io.github.gms.abstraction.AbstractLoggingUnitTest;
import io.github.gms.common.enums.SystemProperty;
import io.github.gms.common.enums.SystemStatus;
import io.github.gms.functions.keystore.KeystoreFileService;
import io.github.gms.functions.maintenance.job.JobEntity;
import io.github.gms.functions.maintenance.job.JobRepository;
import io.github.gms.functions.setup.SystemAttributeRepository;
import io.github.gms.functions.system.SystemService;
import io.github.gms.functions.systemproperty.SystemPropertyService;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static io.github.gms.util.LogAssertionUtils.assertLogContains;
import static io.github.gms.util.TestUtils.createJobEntity;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
class GeneratedKeystoreCleanupJobTest extends AbstractLoggingUnitTest {

    private SystemService systemService;
    private Clock clock;
    private SystemPropertyService systemPropertyService;
    private JobRepository jobRepository;
    private KeystoreFileService service;
    private GeneratedKeystoreCleanupJob job;
    private SystemAttributeRepository systemAttributeRepository;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();

        // init
        systemService = mock(SystemService.class);
        systemPropertyService = mock(SystemPropertyService.class);
        service = mock(KeystoreFileService.class);
        clock = mock(Clock.class);
        jobRepository = mock(JobRepository.class);
        systemAttributeRepository = mock(SystemAttributeRepository.class);
        job = new GeneratedKeystoreCleanupJob(service);

        ReflectionTestUtils.setField(job, "systemService", systemService);
        ReflectionTestUtils.setField(job, "systemPropertyService", systemPropertyService);
        ReflectionTestUtils.setField(job, "clock", clock);
        ReflectionTestUtils.setField(job, "jobRepository", jobRepository);
        ReflectionTestUtils.setField(job, "systemAttributeRepository", systemAttributeRepository);

        addAppender(GeneratedKeystoreCleanupJob.class);

        MDC.clear();
    }

    @Test
    void run_whenSystemIsNotReady_thenSkipExecution() {
        // arrange
        when(systemAttributeRepository.getSystemStatus()).thenReturn(Optional.of(TestUtils.createSystemAttributeEntity(SystemStatus.NEED_SETUP)));

        // act
        job.run();

        // assert
        assertTrue(logAppender.list.isEmpty());
        verify(systemAttributeRepository).getSystemStatus();
    }

    @Test
    void run_whenJobIsDisabled_thenSkipExecution() {
        // arrange
        when(systemPropertyService.getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED)).thenReturn(false);
        when(systemAttributeRepository.getSystemStatus()).thenReturn(Optional.of(TestUtils.createSystemAttributeEntity(SystemStatus.OK)));

        // act
        job.run();

        // assert
        assertTrue(logAppender.list.isEmpty());
        verify(systemPropertyService).getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED);
    }

    @Test
    void run_whenSkipJobExecutionReturnsTrue_thenSkipExecution() {
        // arrange
        when(systemService.getContainerId()).thenReturn("ab123457");
        when(systemPropertyService.getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED)).thenReturn(true);
        when(systemPropertyService.getBoolean(SystemProperty.ENABLE_MULTI_NODE)).thenReturn(true);
        when(systemPropertyService.get(SystemProperty.KEYSTORE_CLEANUP_RUNNER_CONTAINER_ID)).thenReturn("ab123456");
        when(systemAttributeRepository.getSystemStatus()).thenReturn(Optional.of(TestUtils.createSystemAttributeEntity(SystemStatus.OK)));

        // act
        job.run();

        // assert
        assertTrue(logAppender.list.isEmpty());
        verify(systemService).getContainerId();
        verify(systemPropertyService).getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED);
        verify(systemPropertyService).get(SystemProperty.KEYSTORE_CLEANUP_RUNNER_CONTAINER_ID);
        verify(service, never()).deleteTempKeystoreFiles();
    }

    @Test
    void run_whenNoKeystoreFileDeleted_thenSKipLogging() {
        // arrange
        when(systemPropertyService.getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED)).thenReturn(true);
        when(service.deleteTempKeystoreFiles()).thenReturn(0L);
        when(jobRepository.save(any(JobEntity.class))).thenReturn(createJobEntity());
        when(jobRepository.findById(anyLong())).thenReturn(java.util.Optional.of(createJobEntity()));
        when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(systemAttributeRepository.getSystemStatus()).thenReturn(Optional.of(TestUtils.createSystemAttributeEntity(SystemStatus.OK)));

        // act
        job.run();

        // assert
        assertTrue(logAppender.list.isEmpty());
        verify(service).deleteTempKeystoreFiles();
        verify(jobRepository, times(2)).save(any(JobEntity.class));
        verify(jobRepository).findById(anyLong());
    }

    @Test
    void run_whenAllConditionsMet_thenProcess() {
        // arrange
        when(systemPropertyService.getBoolean(SystemProperty.KEYSTORE_CLEANUP_JOB_ENABLED)).thenReturn(true);
        when(service.deleteTempKeystoreFiles()).thenReturn(1L);
        when(jobRepository.save(any(JobEntity.class))).thenReturn(createJobEntity());
        when(jobRepository.findById(anyLong())).thenReturn(java.util.Optional.of(createJobEntity()));
        when(clock.instant()).thenReturn(Instant.parse("2023-06-29T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(systemAttributeRepository.getSystemStatus()).thenReturn(Optional.of(TestUtils.createSystemAttributeEntity(SystemStatus.OK)));

        // act
        job.run();

        // assert
        assertFalse(logAppender.list.isEmpty());
        assertLogContains(logAppender, "1 temporary keystore(s) deleted");
        verify(service).deleteTempKeystoreFiles();
        verify(jobRepository, times(2)).save(any(JobEntity.class));
        verify(jobRepository).findById(anyLong());
    }
}
