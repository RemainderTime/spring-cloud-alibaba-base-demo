package com.xf.demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * packageName com.xf.demo.mapper
 * @author remaindertime
 * @className SeataMapper
 * @date 2024/12/3
 * @description
 */
@Mapper
public interface SeataOrderMapper {

    @Insert("INSERT INTO xf_order (user_id, product_id) values (#{userId}, #{productId})")
    void createOrder(@Param("userId") Integer userId, @Param("productId") Integer productId);

}
