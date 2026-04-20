package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {
    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();


    // 初始化路由配置监听
    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        // 1. 项目启动时, 先拉取一次配置, 并且添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    /**
                     * 获取线程池，用于执行receiveConfigInfo方法
                     * @return
                     */
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 2. 监听到配置变更, 需要更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        // 3. 第一次读取到配置, 也需要更新到路由表
        updateConfigInfo(configInfo);
    }

    /**
     * 更新路由表
     * @param configInfo
     */
    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息: {}", configInfo);
        // 1. 解析配置信息, 转为 RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        // 2. 删除旧的路由表
        for(String routeId : routeIds){
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        // 3. 更新路由表
        for(RouteDefinition routeDefinition : routeDefinitions){
            // 3.1 更新路由表
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            // 3.2 记录路由 id, 便于下一次更新时删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
