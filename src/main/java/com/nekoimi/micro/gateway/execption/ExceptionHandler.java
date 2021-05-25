package com.nekoimi.micro.gateway.execption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;

/**
 * nekoimi  2021/5/25 下午4:05
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class ExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setCode(99500);
        jsonResponse.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant(ZoneOffset.of("+8")).toEpochMilli());
        if (ex instanceof ResponseStatusException) {
            HttpStatus status = ((ResponseStatusException) ex).getStatus();
            String code = String.format("10%d", status.value());
            jsonResponse.setCode(Integer.parseInt(code));
            jsonResponse.setMessage(status.getReasonPhrase());
        }

        else if (ex instanceof ConnectTimeoutException ||
        ex instanceof SslHandshakeTimeoutException) {
            log.error("[ConnectTimeoutException] " + ex.getMessage());
            jsonResponse.setCode(99504);
            jsonResponse.setMessage("Gateway timed out");
        }

        else {
            log.error("clazz: " + ex.getClass());
            jsonResponse.setMessage(ex.getMessage());
        }
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            return bufferFactory.wrap(jsonResponse.toJson().getBytes());
        }));
    }

    @Getter
    @Setter
    private static final class JsonResponse {
        private int code;
        private String message = "ok";
        private Object data = new HashMap<>();
        private long timestamp;

        public String toJson() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                return e.getMessage();
            }
        }
    }
}
