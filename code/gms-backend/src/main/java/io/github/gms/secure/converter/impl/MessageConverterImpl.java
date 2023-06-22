package io.github.gms.secure.converter.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import io.github.gms.secure.converter.MessageConverter;
import io.github.gms.secure.dto.MessageDto;
import io.github.gms.secure.dto.MessageListDto;
import io.github.gms.secure.entity.MessageEntity;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Component
public class MessageConverterImpl implements MessageConverter {

	@Override
	public MessageDto toDto(MessageEntity entity) {
		return MessageDto.builder()
				.id(entity.getId())
				.userId(entity.getUserId())
				.message(entity.getMessage())
				.creationDate(entity.getCreationDate())
				.opened(entity.isOpened())
				.build();
	}

	@Override
	public MessageListDto toDtoList(Page<MessageEntity> resultList) {
		List<MessageDto> results = resultList.toList().stream().map(this::toDto).toList();
		return MessageListDto.builder().resultList(results).totalElements(resultList.getTotalElements()).build();
	}
}
