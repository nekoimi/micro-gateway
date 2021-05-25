package com.nekoimi.micro.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekoimi.micro.gateway.event.NacosDynamicRouteEvent;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * nekoimi  2021/5/25 下午3:13
 */
public class YamlTests {

    static final String yamlString = "      routes:\n" +
            "        - id: a\n" +
            "          uri: https://www.baidu.com/aaa\n" +
            "          predicates:\n" +
            "            - Method=GET";

    @Test
    public void testReadYamlToJson() {
        Yaml yamlc = new Yaml();
        Object load = yamlc.load(yamlString);
        System.out.println(load);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String string = mapper.writeValueAsString(load);
            System.out.println(string);
            NacosDynamicRouteEvent.RouteProperties properties = mapper.readValue(string, NacosDynamicRouteEvent.RouteProperties.class);
            System.out.println(properties);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Yaml yaml = new Yaml(new Constructor(NacosDynamicRouteEvent.RouteProperties.class));
        NacosDynamicRouteEvent.RouteProperties load1 = yaml.load(yamlString);
        System.out.println(load1);
        System.out.println(load1.getRoutes());
    }

}
