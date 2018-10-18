package com.github.jaspercloud.tcc.dubbo.demo.dao.order;

import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {

    @Select("select nextval('order_id_seq')")
    Long selectId();

    @Insert("insert into tcc_order (id,price,num,coin,order_status) values (#{id},#{price},#{num},#{coin},#{status})")
    void insertOrder(@Param("id") long id, @Param("price") int price, @Param("num") int num, @Param("coin") int coin, @Param("status") String status);

    @Update("update tcc_order set order_status=#{status} where id=#{id}")
    void updateOrderStatus(@Param("id") long id, @Param("status") String status);
}
