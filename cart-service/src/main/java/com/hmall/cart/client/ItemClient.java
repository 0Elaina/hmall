package com.hmall.cart.client;

import com.hmall.cart.domain.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

// @FeignClient 注解声明这是一个 Feign 客户端
// "item-service" 指定了要调用的目标服务名称，OpenFeign 会从注册中心获取该服务的实例
@FeignClient("item-service")
public interface ItemClient {
    /**
     * 根据商品ID集合查询商品信息
     *
     * @param ids 商品ID列表
     * @return 包含对应商品信息的 ItemDTO 列表
     */
    // @GetMapping 注解声明这是一个 GET 请求，路径为 /items
    // OpenFeign 会自动拼接服务地址，最终请求类似 http://item-service/items
    @GetMapping("/items")
    // @RequestParam 注解将方法参数绑定到 HTTP 请求的查询参数上
    // 这里表示将 Java 中的 ids 参数序列化为 URL 中的 ?ids=1,2,3 格式
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);
}
