package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            /**
             * 查询商品列表降级处理方案
             * @param ids 商品ID列表
             * @return
             */
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败!", cause);
                return CollUtils.emptyList();
            }

            /**
             * 扣减库存降级处理方案
             * @param items 订单明细列表
             */
            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败!", cause);
                throw new RuntimeException(cause);
            }
        };
    }
}
