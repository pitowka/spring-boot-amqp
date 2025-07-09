package pitowka.amqp;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.amqp.rabbit.test.context.SpringRabbitTest;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

@SpringRabbitTest
@SpringBootTest(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class AnnotationPojoAmqpTests {
	@RabbitListenerTest(capture = true)
	@TestConfiguration
	static class AnnotationPojoAmqpTestsConfiguration {
		@Bean
		public TestRabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
			TestRabbitTemplate retVal = new TestRabbitTemplate(connectionFactory);
			retVal.setMessageConverter(jsonMessageConverter);
			return retVal;
		}

		@Bean
		public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
			return new RabbitAdmin(connectionFactory);
		}

		@Bean
		public ConnectionFactory connectionFactory() {
			ConnectionFactory factory = mock(ConnectionFactory.class);
			Connection connection = mock(Connection.class);
			Channel channel = mock(Channel.class);
			willReturn(connection).given(factory).createConnection();
			willReturn(channel).given(connection).createChannel(anyBoolean());
			given(channel.isOpen()).willReturn(true);

			return factory;
		}

		@Bean
		public SimpleRabbitListenerContainerFactory testRabbitListenerContainerFactory(ConnectionFactory connectionFactory){
			SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
			factory.setConnectionFactory(connectionFactory);
			factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

			return factory;
		}
	}

//	@Autowired
//	private TestRabbitTemplate template;
//
//	@Autowired
//	private RabbitAdmin admin;
//
//	@Autowired
//	private RabbitListenerEndpointRegistry registry;

	@Test
	void handleMessage(
		@Autowired TestRabbitTemplate testRabbitTemplate,
		@Autowired RabbitListenerTestHarness harness) {
		testRabbitTemplate.convertAndSend("annotation.queue", new AnnotationAmqpConfiguration.AnnotationRequest("text", 20));

		AnnotationAmqpConfiguration.AnnotationPojoMessageHandler handler = harness.getSpy("foobar.annotation");

		Mockito.verify(handler).handleMessage(new AnnotationAmqpConfiguration.AnnotationRequest("text", 20));
	}
}