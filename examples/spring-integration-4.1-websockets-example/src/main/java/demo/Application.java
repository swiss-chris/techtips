package demo ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.websocket.ServerWebSocketContainer;
import org.springframework.integration.websocket.outbound.WebSocketOutboundMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.SESSION_ID_HEADER;

/**
 * @author Artem Bilan
 * @author Josh Long
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@RestController
public class Application {

    private static final GenericMessage<Object> EMPTY_MESSAGE = new GenericMessage<>("");
    private static final AtomicInteger counter = new AtomicInteger();
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServerWebSocketContainer serverWebSocketContainer() {
        return new ServerWebSocketContainer("/messages").withSockJs();
    }

    @Bean
    MessageHandler webSocketOutboundAdapter() {
        return new WebSocketOutboundMessageHandler(serverWebSocketContainer());
    }

    @Bean
    IntegrationFlow webSocketFlow() {
        return flow -> flow
                .log(Message::getPayload)
                .split(Message.class, m -> serverWebSocketContainer()
                        .getSessions()
                        .keySet()
                        .stream()
                        .map(s -> MessageBuilder.fromMessage(m)
                                .setHeader(SESSION_ID_HEADER, s)
                                .build())
                        .collect(Collectors.toList()))
                .channel(c -> c.executor(Executors.newCachedThreadPool()))
                .handle(webSocketOutboundAdapter());
    }

    @Bean
    IntegrationFlow isochroneSchedulerFlow() {
        var nbIncrementsPerSecond = 20;
        return IntegrationFlows
                .from(() -> EMPTY_MESSAGE, e -> e.poller(Pollers.fixedRate(1000 / nbIncrementsPerSecond, MILLISECONDS)))
                .transform(m -> counter.incrementAndGet() % 100)
                .channel(webSocketFlow().getInputChannel())
                .get();
    }
}
