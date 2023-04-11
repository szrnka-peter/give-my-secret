package io.github.gms.secure.entity;

import java.time.ZonedDateTime;

import io.github.gms.common.abstraction.AbstractGmsEntity;
import io.github.gms.common.enums.EventOperation;
import io.github.gms.common.enums.EventTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Data
@Entity
@Table(name = "gms_event")
@EqualsAndHashCode(callSuper = false)
public class EventEntity extends AbstractGmsEntity {

	private static final long serialVersionUID = 4146919964684367885L;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "event_date")
	private ZonedDateTime eventDate;
	
	@Column(name = "operation")
	@Enumerated(EnumType.STRING)
	private EventOperation operation;
	
	@Column(name = "target")
	@Enumerated(EnumType.STRING)
	private EventTarget target;
}
