package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    @Select("select * from user where id = #{id}")
    User getById(Long id);

    void insert(User user);

    @Select("select count(0) from user where create_time < #{dateTime}")
    Integer getTotalUser(LocalDateTime dateTime);

    @Select("select count(0) from user where create_time < #{endTime} and create_time > #{beginTime}")
    Integer getNewUser(LocalDateTime beginTime, LocalDateTime endTime);
}
