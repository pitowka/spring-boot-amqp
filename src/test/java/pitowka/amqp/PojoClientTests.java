package pitowka.amqp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import pitowka.amqp.PojoClientConfiguration.ClientRequest;
import pitowka.amqp.PojoClientConfiguration.ClientResp;

import java.util.function.Function;

@Testcontainers
@SpringBootTest
class PojoClientTests {
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
    static class PojoAmqpTestsConfiguration {
        @Bean
        Function<ClientRequest, ClientResp> server(){
            return new Function<ClientRequest, ClientResp>() {

                @Override
                @RabbitListener(
                        id = "pojo.client.listener",
                        queues = "pojo.client.queue"
                )
                public ClientResp apply(ClientRequest clientRequest) {
                    return new ClientResp("As response of: "+clientRequest.getText());
                }
            };
        }
    }

    @Autowired
    private DirectExchange clientExchange;

    @Test
    void handleResponse(@Autowired RabbitTemplate pojoClientRabbitTemplate){
        ClientResp retVal = (ClientResp) pojoClientRabbitTemplate.convertSendAndReceive("pojo.client.queue", new PojoClientConfiguration.ClientRequest("text"));
        System.err.println(retVal);
        Assertions.assertNotNull(retVal);
    }
}
