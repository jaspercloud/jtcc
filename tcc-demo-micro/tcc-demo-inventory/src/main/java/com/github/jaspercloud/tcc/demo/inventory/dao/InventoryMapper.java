package com.github.jaspercloud.tcc.demo.inventory.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper {

    @Select("select num from inventory where id=#{id}")
    Integer selectInventoryById(@Param("id") Long id);

    @Update("update inventory set num=#{setNum} where id=#{id} and num=#{queryNum}")
    int updateInventory(@Param("id") long id, @Param("setNum") int setNum, @Param("queryNum") int queryNum);
}
