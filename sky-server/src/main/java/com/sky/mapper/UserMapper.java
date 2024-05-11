package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/11 15:23
 *Description:
 */
@Mapper
public interface UserMapper {
    /**
     * 根据openid查询是否为新用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     * 如果用户不存在，自动创建
     */
    void insert(User user);

    /**
     * 查询当前登入用户
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);


    /**
     * 查询每天的总用户数量
     * @param map
     * @return
     */
    Integer sumUserByMap(Map map);


}
