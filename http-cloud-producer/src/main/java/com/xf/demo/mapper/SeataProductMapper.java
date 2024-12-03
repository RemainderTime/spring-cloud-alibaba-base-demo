package com.xf.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * packageName com.xf.demo.mapper
 * @author remaindertime
 * @className SeataProductMapper
 * @date 2024/12/3
 * @description
 */
@Mapper
public interface SeataProductMapper {

    @Update("UPDATE xf_product SET num = num - #{num} WHERE id = #{productId}")
    void deInventory(@Param("num") Integer num, @Param("productId") Integer productId);
}
