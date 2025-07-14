package pitowka.amqp;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Configuration
public class PojoClientConfiguration {
    @Bean
    Queue pojoClientQueue() {
        return new Queue("pojo.client.queue");
    }

    @Bean
    public DirectExchange clientExchange() {
        return new DirectExchange("pojo.client");
    }

    @Bean
    RabbitTemplate pojoClientRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //template.setRoutingKey("pojo.queue");
        //template.setDefaultReceiveQueue("pojo.queue");
        template.setMessageConverter(jsonMessageConverter);

        return template;
    }

    public static class ClientRequest implements Serializable {
        private String text;

        public ClientRequest(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ClientRequest that = (ClientRequest) o;
            return text.equals(that.text);
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public String toString() {
            return "ClientRequest{" +
                "text='" + text + '\'' +
                '}';
        }
    }

    public static class ClientResp implements Serializable {
        private String text;

        public ClientResp(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ClientResp that = (ClientResp) o;
            return text.equals(that.text);
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public String toString() {
            return "ClientResp{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}
