package pitowka.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleAmqpConfiguration {
    @Bean
    Queue helloWorldQueue() {
        return new Queue("hello.world.queue");
    }

    @Bean
    RabbitTemplate helloWorldTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //The routing key is set to the name of the queue by the broker for the default exchange.
        template.setRoutingKey("hello.world.queue");
        //Where we will synchronously receive messages from
        template.setDefaultReceiveQueue("hello.world.queue");
        return template;
    }

    @Bean
    SimpleMessageListenerContainer helloWorldListenerContainer(ConnectionFactory connectionFactory, MessageHandler messageHandler) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("hello.world.queue");
        container.setMessageListener(new MessageListenerAdapter(messageHandler, "handleMessage"));

        return container;
    }

    @Bean
    MessageHandler messageHandler(){
        return new HelloWorldHandler();
    }

    public interface MessageHandler{
        void handleMessage(String text);
    }

    class HelloWorldHandler implements MessageHandler{

        @Override
        public void handleMessage(String text) {
            System.err.println("Received: " + text);
        }
    }
}
