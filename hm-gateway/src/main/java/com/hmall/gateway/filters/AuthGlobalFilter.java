package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 登录拦截
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取request
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否需要做登录拦截
        if (isExclude(request.getPath().toString())) {
            // 放行
            return chain.filter(exchange);
        }
        // 3. 获取token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        // 4. 校验并解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 拦截, 设置响应状态码为 401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5. 传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange
                .mutate()
                .request(
                        // 在实际开发中, "user-info"建议定义为常量, 避免错误
                        // "user-info"实际可以为任意值, 由各开发人员约定好一个值
                        builder -> builder.header("user-info",
                                userInfo))
                .build();
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 判断是否需要做登录拦截
     * @param path
     * @return
     */
    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPattern, path)) { return true; }
        }
        return false;
    }

    /**
     * 设置优先级，数字越小优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
