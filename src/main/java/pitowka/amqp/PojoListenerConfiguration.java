package pitowka.amqp;

import lombok.Builder;
import lombok.Data;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.Serializable;
import java.util.Objects;

@Configuration()
public class PojoListenerConfiguration {

    @Bean
    Queue pojoQueue() {
        return new Queue("pojo.queue");
    }

    @Bean
    RabbitTemplate pojoRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey("pojo.queue");
        template.setDefaultReceiveQueue("pojo.queue");
        template.setMessageConverter(jsonMessageConverter);

        return template;
    }

    @Bean
    SimpleMessageListenerContainer pojoListenerContainer(ConnectionFactory connectionFactory, MessageListenerAdapter pojoListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("pojo.queue");
        container.setMessageListener(pojoListenerAdapter);

        return container;
    }

    @Bean
    MessageListenerAdapter pojoListenerAdapter(PojoMessageHandler handler, MessageConverter jsonMessageConverter) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(handler, "handleMessage");
        adapter.setMessageConverter(jsonMessageConverter);

        return adapter;
    }

    @Bean
    PojoMessageHandler pojoMessageHandler(){
        return new PojoMessageHandlerImpl();
    }

    public interface PojoMessageHandler {
        void handleMessage(PojoRequest request);
    }

    public static class PojoRequest implements Serializable {
        private String text;
        private Integer count;

        public PojoRequest(String text, Integer count) {
            this.text = text;
            this.count = count;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PojoRequest that = (PojoRequest) o;
            return Objects.equals(text, that.text) && Objects.equals(count, that.count);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, count);
        }

        @Override
        public String toString() {
            return "PojoRequest{" +
                "text='" + text + '\'' +
                ", count=" + count +
                '}';
        }
    }

    @Data
    @Builder
    public static class Response{
        private final String message;
        private final ErrorSection error;

        @Data
        static class ErrorSection{
            private final String code;
            private final String message;
        }
    }

    public class PojoMessageHandlerImpl implements PojoMessageHandler {

        @Override
        public void handleMessage(PojoRequest request) {
            System.err.println("Received: " + request);
        }
    }
}
