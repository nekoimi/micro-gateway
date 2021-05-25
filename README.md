# 全局网关

- 动态路由，yaml or json 配置

##### Route 配置 (nacos)

- yaml 配置 (推荐)

```yaml
  routes:
    - id: a
      uri: https://www.v2ex.com/
      predicates:
        - Path=/t/**
    - id: b
      uri: https://www.hao123.com
      predicates:
        - Path=/hao123/**
      filters:
        - StripPrefix=1
    - id: c
      uri: https://www.baidu.com
      predicates:
        - Path=/baidu/**
      filters:
        - StripPrefix=1
```

- json 配置

```json
{
  "routes": [
    {
      "id": "a",
      "uri": "https://www.baidu.com/",
      "predicates": [
        {
          "name": "Path",
          "args": {
            "pattern": "/baidu/**"
          }
        }
      ]
    }
  ]
}
```

