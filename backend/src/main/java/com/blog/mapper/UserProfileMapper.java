package com.blog.mapper;

import com.blog.entity.UserProfile;
import org.apache.ibatis.annotations.*;

/**
 * 用户资料Mapper
 */
@Mapper
public interface UserProfileMapper {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfile findByUserId(Long userId);

    @Insert("INSERT INTO user_profile (user_id, avatar, bio) VALUES (#{userId}, #{avatar}, #{bio})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProfile profile);

    @Update("UPDATE user_profile SET avatar = #{avatar}, bio = #{bio} WHERE user_id = #{userId}")
    int update(UserProfile profile);
}
