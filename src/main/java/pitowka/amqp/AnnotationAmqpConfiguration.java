package pitowka.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Objects;

@Configuration
public class AnnotationAmqpConfiguration {

    @Bean
    Queue annotationPojoQueue() {
        return new Queue("annotation.queue");
    }

    @Bean
    AnnotationPojoMessageHandler annotationPojoMessageHandler(){
        return new AnnotationPojoMessageHandlerImpl();
    }

    @Bean
    Object queueHandler(AnnotationPojoMessageHandler annotationPojoMessageHandler){
        return new AnnotationPojoMessageHandler(){
            @Override
            @RabbitListener(
                id = "foobar.annotation",
                queues = "annotation.queue",
                messageConverter = "jsonMessageConverter"   // asi nie je nutne
            )
            public void handleMessage(AnnotationRequest request) {
                annotationPojoMessageHandler.handleMessage(request);
            }
        };
    }

    public interface AnnotationPojoMessageHandler {
        void handleMessage(AnnotationRequest request);
    }

    public static class AnnotationRequest implements Serializable {
        private String text;
        private Integer count;

        public AnnotationRequest(String text, Integer count) {
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
            AnnotationRequest that = (AnnotationRequest) o;
            return Objects.equals(text, that.text) && Objects.equals(count, that.count);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, count);
        }

        @Override
        public String toString() {
            return "AnnotationRequest{" +
                "text='" + text + '\'' +
                ", count=" + count +
                '}';
        }
    }

    public class AnnotationPojoMessageHandlerImpl implements AnnotationPojoMessageHandler {

        @Override
        public void handleMessage(AnnotationRequest request) {
            System.err.println("Received: " + request);
        }
    }
}
