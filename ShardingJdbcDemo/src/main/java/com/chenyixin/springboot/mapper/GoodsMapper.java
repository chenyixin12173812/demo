package com.chenyixin.springboot.mapper;

import com.chenyixin.springboot.entity.Goods;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsMapper {

    /**
     * 查询
     * @param id
     * @return
     */
    List<Goods> getById(@Param("id") Long id);
}

