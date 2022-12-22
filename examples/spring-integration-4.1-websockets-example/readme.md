Spring Integration Tech Tip
===========================

Spring Integration 4.1 was released and it includes a *lot* of great new features! One of my favorites? Smart integration with the Spring 4 WebSocket support. Now you can compose a integration flow whose final destination is a WebSocket client. There is also support for acting as the client to a WebSocket service.

All clients listening on `/messages` will receive whatever message is sent into the `requestChannel` channel. A Spring 4 `MessageChannel` is a named conduit - more or less analogous to a `java.util.Queue<T>`. This example uses [the Spring Integration Java configuration DSL](https://spring.io/blog/2014/10/31/spring-integration-java-dsl-1-0-rc1-released) on top of the new [Spring Integration 4.1 web socket support](https://spring.io/blog/2014/11/11/spring-integration-and-amqp-releases-available). 

The `IntegrationFlow` is simple. For each message that comes in, copy it and address it to each listening `WebSocket` session by adding a header having the `SimpMessageHeaderAccessor.SESSION_ID_HEADER`, then send it the outbound `webSocketOutboundAdapter` which will deliver it to each listening client. To see it work, open http://localhost:8081/ in one browser window, and then http://localhost:8081/hi/Spring in another. 

There is great documentation on how to use the web socket [components in Spring Integration 4.1 documentation](http://docs.spring.io/spring-integration/docs/latest-ga/reference/html/web-sockets.html). There's a more inspiring example in [the Spring Integration samples directory](https://github.com/spring-projects/spring-integration-samples/tree/master/basic/web-sockets), too.
