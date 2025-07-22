package pitowka.amqp;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

class PojoConverterTests {
    @Test
    void f(){
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(new ClassMapper() {
            @Override
            public void fromClass(Class<?> clazz, MessageProperties properties) {

            }

            @Override
            public Class<?> toClass(MessageProperties properties) {
                return null;
            }
        });

        Message msg = jackson2JsonMessageConverter.toMessage(
                PojoListenerConfiguration.Response.builder()
                        .message("Hello World!")
                        .error(new PojoListenerConfiguration.Response.ErrorSection("1", "Error message"))
                        .build(),
                null);
        System.err.println(new String(msg.getBody()));

    }
}
