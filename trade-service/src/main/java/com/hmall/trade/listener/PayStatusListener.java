package com.hmall.trade.listener;

import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    /**
     * 监听支付成功消息
     * @param orderId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = "trade.pay.success.queue", // 队列名称
                    durable = "true" // 持久化
            ),
            exchange = @Exchange(name = "pay.direct"), // 交换机名称
            key = "pay.success" // 路由键
    ))
    public void listenPaySuccess(Long orderId){
        orderService.markOrderPaySuccess(orderId);
    }
}
