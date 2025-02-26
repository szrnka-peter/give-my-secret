package io.github.gms.common.aspect;

import io.github.gms.abstraction.AbstractUnitTest;
import io.github.gms.common.enums.EventOperation;
import io.github.gms.common.enums.EventTarget;
import io.github.gms.common.model.UserEvent;
import io.github.gms.functions.event.EventService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Unit test of {@link EventPublisherAspect}
 */
class EventPublisherAspectTest extends AbstractUnitTest {

	private EventService service;
	private EventPublisherAspect aspect;

	@BeforeEach
	void beforeEach() {
		service = mock(EventService.class);
		aspect = new EventPublisherAspect(service);
	}

	@Test
	void test_whenAnnotationIsOnTargetClass_thenReturnOk() {
		// arrange
		ReflectionTestUtils.setField(aspect, "service", service);
		
		// act
		TestController target = new TestController();
		AspectJProxyFactory factory = new AspectJProxyFactory(target);
		factory.addAspect(aspect);
		TestController proxy = factory.getProxy();
		
		String response = proxy.test();
		
		// assert
		Assertions.assertThat(response).isEqualTo("OK");
		
		ArgumentCaptor<UserEvent> userEventCaptor = ArgumentCaptor.forClass(UserEvent.class);
		Mockito.verify(service).saveUserEvent(userEventCaptor.capture());
		
		UserEvent capturedUserEvent = userEventCaptor.getValue();
		Assertions.assertThat(capturedUserEvent.getOperation()).isEqualTo(EventOperation.GET_BY_ID);
		Assertions.assertThat(capturedUserEvent.getTarget()).isEqualTo(EventTarget.ADMIN_USER);
	}
	
	@Test
	void test_whenAnnotationIsOnTargetMethod_thenReturnOk() {
		// arrange
		ReflectionTestUtils.setField(aspect, "service", service);
		
		// act
		TestController target = new TestController();
		AspectJProxyFactory factory = new AspectJProxyFactory(target);
		factory.addAspect(aspect);
		TestController proxy = factory.getProxy();
		
		String response = proxy.test2();
		
		// assert
		Assertions.assertThat(response).isEqualTo("OK");
		ArgumentCaptor<UserEvent> userEventCaptor = ArgumentCaptor.forClass(UserEvent.class);
		Mockito.verify(service).saveUserEvent(userEventCaptor.capture());
		
		UserEvent capturedUserEvent = userEventCaptor.getValue();
		Assertions.assertThat(capturedUserEvent.getOperation()).isEqualTo(EventOperation.GET_BY_ID);
		Assertions.assertThat(capturedUserEvent.getTarget()).isEqualTo(EventTarget.API_KEY);
	}
	
	@Test
	void test_whenAnnotationIsMissing_thenReturnOk() {
		// arrange
		ReflectionTestUtils.setField(aspect, "service", service);
		
		// act
		Test2Controller target = new Test2Controller();
		AspectJProxyFactory factory = new AspectJProxyFactory(target);
		factory.addAspect(aspect);
		Test2Controller proxy = factory.getProxy();
		
		String response = proxy.test();
		
		// assert
		Assertions.assertThat(response).isEqualTo("OK");
		
		response = proxy.test2();
		
		// assert
		Assertions.assertThat(response).isEqualTo("OK");
		Mockito.verify(service, never()).saveUserEvent(any(UserEvent.class));
	}
	
	@Test
	void test_whenMethodsCalled_thenPointcutsDoNothing() {
		assertDoesNotThrow(() -> aspect.allMethod());
		assertDoesNotThrow(() -> aspect.audited());
		assertDoesNotThrow(() -> aspect.restController());
	}
}
