package io.github.gms.secure.job;

import io.github.gms.common.enums.RotationPeriod;
import io.github.gms.secure.entity.SecretEntity;
import io.github.gms.secure.repository.SecretRepository;
import io.github.gms.secure.service.SecretRotationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "config.job.secretRotation.enabled", havingValue = "true", matchIfMissing = true)
public class SecretRotationJob {
	
	private static final long DELAY_SECONDS = 55L;

	private final Clock clock;
	private final SecretRepository secretRepository;
	private final SecretRotationService service;

	public SecretRotationJob(Clock clock, SecretRepository secretRepository, SecretRotationService service) {
		this.clock = clock;
		this.secretRepository = secretRepository;
		this.service = service;
	}

	@Scheduled(cron = "0 * * * * ?")
	public void execute() {
		List<SecretEntity> resultList = secretRepository.findAllOldRotated(ZonedDateTime.now(clock).minusSeconds(DELAY_SECONDS));
		
		AtomicLong counter = new AtomicLong(0L);
		resultList.forEach(secretEntity -> {
			if (shouldNotRotate(secretEntity)) {
				return;
			}

			service.rotateSecret(secretEntity);
			counter.incrementAndGet();
		});
		
		if (counter.get() > 0) {
			log.info("{} entities updated", counter.get());
		}
	}

	private boolean shouldNotRotate(SecretEntity secretEntity) {
		RotationPeriod rotationPeriod = secretEntity.getRotationPeriod();
		ZonedDateTime comparisonDate = ZonedDateTime.now(clock).minus(
				rotationPeriod.getUnitValue(), 
				rotationPeriod.getUnit());

		return secretEntity.getLastRotated().isAfter(comparisonDate);
	}
}
