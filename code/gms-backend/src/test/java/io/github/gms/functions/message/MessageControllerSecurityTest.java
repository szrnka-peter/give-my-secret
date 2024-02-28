package io.github.gms.functions.message;

import io.github.gms.abstraction.AbstractSecurityTest;
import io.github.gms.common.dto.LongValueDto;
import io.github.gms.functions.message.MarkAsReadRequestDto;
import io.github.gms.functions.message.MessageListDto;
import io.github.gms.common.dto.PagingDto;
import io.github.gms.util.TestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;

import static io.github.gms.util.TestConstants.TAG_SECURITY_TEST;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@Tag(TAG_SECURITY_TEST)
class MessageControllerSecurityTest extends AbstractSecurityTest {

    @Test
    void testListFailWithHttp403() {
        PagingDto request = PagingDto.builder().page(0).size(50).direction("ASC").property("id").build();
        HttpEntity<PagingDto> requestEntity = new HttpEntity<>(request, TestUtils.getHttpHeaders(jwt));

        // assert
        HttpClientErrorException.Forbidden exception = assertThrows(HttpClientErrorException.Forbidden.class, () ->
                executeHttpPost("/secure/message/list", requestEntity, MessageListDto.class));

        assertTrue(exception.getMessage().startsWith("403"));
    }

    @Test
    void testUnreadMessagesCountFailWithHttp403() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(TestUtils.getHttpHeaders(jwt));

        // assert
        HttpClientErrorException.Forbidden exception = assertThrows(HttpClientErrorException.Forbidden.class, () ->
                executeHttpGet("/secure/message/unread", requestEntity, LongValueDto.class));

        assertTrue(exception.getMessage().startsWith("403"));
    }

    @Test
    void testMarkAsReadFailWithHttp403() {
        MarkAsReadRequestDto request =  MarkAsReadRequestDto.builder().ids(Set.of(1L, 2L, 3L)).build();
        HttpEntity<MarkAsReadRequestDto> requestEntity = new HttpEntity<>(request, TestUtils.getHttpHeaders(jwt));

        // assert
        HttpClientErrorException.Forbidden exception = assertThrows(HttpClientErrorException.Forbidden.class, () ->
                executeHttpPost("/secure/message/mark_as_read", requestEntity, String.class));

        assertTrue(exception.getMessage().startsWith("403"));
    }
}