package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据OpenId来查询用户
     * @param openid
     * @return
     */

    @Select("select * from user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     * 插入新用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据动态条件来统计用户数量
     * @param map
     * @return
     */
    Integer countBymap(Map map);
}
