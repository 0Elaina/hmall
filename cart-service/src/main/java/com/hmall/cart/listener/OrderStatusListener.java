package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(value = MqConstants.TRADE_EXCHANGE_NAME, type = ExchangeTypes.TOPIC, durable = "true"),
            key = {MqConstants.ROUTING_KEY_ORDER_CREATE}
    ))
    // Header注解: 获取消息头中的数据
    public void listenOrderCreate(List<Long> itemIds, @Header("user-info")Long userId){
        // 1. 获取当前用户id
        UserContext.setUser(userId);
        cartService.removeByItemIds(itemIds);

        // 2. 清理用户id
        UserContext.removeUser();
    }
}
