package com.nekoimi.micro.gateway.event;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * nekoimi  2021/5/25 上午10:16
 */
@Slf4j
@Component
public class NacosDynamicRouteEvent implements ApplicationEventPublisherAware {
    @Autowired
    private NacosConfigProperties configProperties;
    @Autowired
    private RouteDefinitionWriter routeWriter;
    private ApplicationEventPublisher applicationEventPublisher;
    private static final String dataId = "com.nekoimi.micro.gateway";
    private static final List<String> GLOBAL_ROUTE = new ArrayList<>();

    @PostConstruct
    public void nacosDynamicRouteListener() {
        log.debug("Nacos dynamic route listener......");
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, configProperties.getServerAddr());
            properties.put(PropertyKeyConst.NAMESPACE, configProperties.getNamespace());
            properties.put(PropertyKeyConst.ENCODE, configProperties.getEncode());
            ConfigService configService = NacosFactory.createConfigService(properties);
            String defaultConfigInfo = configService.getConfig(dataId, configProperties.getGroup(), configProperties.getTimeout());
            addBatchRoute(defaultConfigInfo);
            configService.addListener(dataId, configProperties.getGroup(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }
                @Override
                public void receiveConfigInfo(String configInfo) {
                    addBatchRoute(configInfo);
                }
            });
        } catch (NacosException nex) {
            log.error("[Nacos dynamic route] " + nex.getErrMsg());
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void addRoute(RouteDefinition routeDefinition) {
        routeWriter.save(Mono.just(routeDefinition)).subscribe();
        GLOBAL_ROUTE.add(routeDefinition.getId());
    }

    private void clearRoute() {
        for (String routeId : GLOBAL_ROUTE) {
            routeWriter.delete(Mono.just(routeId)).subscribe();
        }
        GLOBAL_ROUTE.clear();
    }

    private void addBatchRoute(String configInfo) {
        if (configInfo == null) {
            log.warn("route configInfo is null! ignore");
            return; // Ignore
        }
        RouteProperties routeProperties = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            routeProperties = mapper.readValue(configInfo, RouteProperties.class);
        } catch (JsonProcessingException jex) {
            Yaml yaml = new Yaml(new Constructor(RouteProperties.class));
            routeProperties = yaml.load(configInfo);
        }
        if (routeProperties == null) {
            log.error("parse configInfo err, routeProperties is null, ignore.....");
            return;
        }
        clearRoute();
        List<RouteDefinition> routeDefinitions = routeProperties.getRoutes();
        for (RouteDefinition routeDefinition : routeDefinitions) {
            addRoute(routeDefinition);
        }
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(routeWriter));
    }

    @Getter
    @Setter
    public static final class RouteProperties {
        private List<RouteDefinition> routes = new ArrayList<>();
    }
}
