package com.github.jaspercloud.tcc.dubbo.demo.dao.account;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

    @Select("select coin from account where id=#{id}")
    Integer selectCoinById(@Param("id") Long id);

    @Update("update account set coin=#{setCoin} where id=#{id} and coin=#{queryCoin}")
    int updateCoin(@Param("id") long id, @Param("setCoin") int setCoin, @Param("queryCoin") int queryCoin);
}
