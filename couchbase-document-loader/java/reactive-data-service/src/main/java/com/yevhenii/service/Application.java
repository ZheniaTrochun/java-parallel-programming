package com.yevhenii.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

//@EnableWebFlux
//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
@SpringBootApplication
public class Application {

    private final static String SERVER_HOST = "localhost";
    private final static int SERVER_PORT = 8080;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
//    public static void main(String[] args) throws Exception {
//        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class)) {
//            context.getBean(NettyContext.class).onClose().block();
//        }
//    }
//
//    @Profile("default")
//    @Bean
//    public NettyContext nettyContext(ApplicationContext context) {
//        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
//        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
//        HttpServer httpServer = HttpServer.create(SERVER_HOST, SERVER_PORT);
//
//        return httpServer.newHandler(adapter).block();
//    }
}
