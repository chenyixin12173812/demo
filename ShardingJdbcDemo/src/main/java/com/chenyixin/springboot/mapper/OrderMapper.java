package com.chenyixin.springboot.mapper;

import com.chenyixin.springboot.entity.OrderInfo;
import com.chenyixin.springboot.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    /**
     * 保存
     */
    void save(Order order);

    /**
     * 查询
     * @param id
     * @return
     */
    Order getById(Long id);

    OrderInfo getOrderInfoByUserId(Long userId);

}

