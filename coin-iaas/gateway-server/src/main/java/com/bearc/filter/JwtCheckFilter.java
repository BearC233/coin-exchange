package com.bearc.filter;

import com.alibaba.fastjson.JSONObject;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
@Component
public class JwtCheckFilter implements GlobalFilter, Ordered {
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${no.require.urls:/admin/login}")
    private Set<String> noRequireTokenUris;
    //拦截器行为
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1:检查接口是否需要token才能访问
        if(isRequireToken(exchange)){
            return chain.filter(exchange);//不需要直接放行
        }
        //2:取出用户token
        String token=getUserToken(exchange);
        //3:判断token是否有效
        if(StringUtils.isEmpty(token)){
            return buildNoAuthorationResult(exchange);
        }
        Boolean hasKey = redisTemplate.hasKey(token);
        if(hasKey!=null&&hasKey){
            return chain.filter(exchange);//有效,放行
        }
        return buildNoAuthorationResult(exchange);
    }
    //构建未授权结果
    private Mono<Void> buildNoAuthorationResult(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set("Content-Type", "application/json");
        JSONObject result = new JSONObject();
        result.put("error", "No Authorization");
        result.put("errMsg","Token not found");
        DataBuffer wrap = response.bufferFactory().wrap(result.toJSONString().getBytes());
        return response.writeWith(Flux.just(wrap));
    }
    //获取用户token
    private String getUserToken(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return token==null ? null : token.replace("bearer ", "");
    }
    //判断是否需要token
    private boolean isRequireToken(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return noRequireTokenUris.contains(path);
    }

    //拦截顺序
    @Override
    public int getOrder() {
        return 0;
    }
}
