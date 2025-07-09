package pitowka.amqp;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

@Testcontainers
@SpringBootTest
class SimpleAmqpTests {

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:4.1.2-management-alpine")
			.withExposedPorts(5672, 15672);

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		Startables.deepStart(rabbitMQContainer).join();

		registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
		registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getMappedPort(5672));
		registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
	}

	@TestConfiguration
	@RabbitListenerTest(spy = false, capture = true)
	static class SimpleAmqpTestsConfiguration {
		@Bean
		public TestRabbitTemplate testRabbitTemplate(ConnectionFactory connectionFactory) {
			return new TestRabbitTemplate(connectionFactory);
		}
	}

	@MockitoSpyBean
	private SimpleAmqpConfiguration.MessageHandler messageHandler;

	@Test
	void handleMessage(@Autowired TestRabbitTemplate testRabbitTemplate) {
		testRabbitTemplate.convertAndSend("hello.world.queue", "Hello World");

		Mockito.verify(messageHandler).handleMessage("Hello World");
	}
}