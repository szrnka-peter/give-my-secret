package io.github.gms.functions.system;

import io.github.gms.common.dto.SystemStatusDto;
import io.github.gms.common.enums.ContainerHostType;
import io.github.gms.common.util.Constants;
import io.github.gms.functions.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.github.gms.common.util.Constants.CONTAINER_HOST_TYPE;
import static io.github.gms.common.util.Constants.DOCKER_CONTAINER_ID;
import static io.github.gms.common.util.Constants.N_A;
import static io.github.gms.common.util.Constants.OK;
import static io.github.gms.common.util.Constants.POD_ID;
import static io.github.gms.common.util.Constants.SELECTED_AUTH_DB;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

	private final Environment environment;
	private final UserRepository userRepository;
	private final Clock clock;
	@Setter
    @Value("${config.auth.type}")
	private String authType;
	// It will be set with setter injection
	private BuildProperties buildProperties;

	public SystemStatusDto getSystemStatus() {
		SystemStatusDto.SystemStatusDtoBuilder builder = SystemStatusDto.builder();
		builder.withAuthMode(authType);
		builder.withVersion(getVersion());
		builder.withBuilt(getBuildTime().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)));
		builder.withContainerHostType(getContainerHostType());
		builder.withContainerId(getContainerId());

		if (!SELECTED_AUTH_DB.equals(authType)) {
			return builder.withStatus(OK).build();
		}

		long result = userRepository.countExistingAdmins();
		return builder.withStatus(result > 0 ? OK : "NEED_SETUP").build();
	}

	@Autowired(required = false)
	public void setBuildProperties(BuildProperties buildProperties) {
		this.buildProperties = buildProperties;
	}

	public String getContainerId() {
        ContainerHostType containerHostType = getContainerHostType();

		if (ContainerHostType.DOCKER == containerHostType || ContainerHostType.SWARM == containerHostType) {
			return environment.getProperty(DOCKER_CONTAINER_ID, N_A);
		} else if (ContainerHostType.KUBERNETES == containerHostType || ContainerHostType.OPENSHIFT == containerHostType) {
			return environment.getProperty(POD_ID, N_A);
		}

		return N_A;
	}

	private String getVersion() {
		return buildProperties != null ? buildProperties.getVersion() : "DevRuntime";
	}

	private ContainerHostType getContainerHostType() {
		return ContainerHostType.getContainerHostType(environment.getProperty(CONTAINER_HOST_TYPE));
	}

	private ZonedDateTime getBuildTime() {
		return buildProperties != null ? buildProperties.getTime().atZone(clock.getZone()) : ZonedDateTime.now(clock);
	}
}
