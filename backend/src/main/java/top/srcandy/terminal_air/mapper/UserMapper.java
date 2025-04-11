package top.srcandy.terminal_air.mapper;


import org.apache.ibatis.annotations.*;
import top.srcandy.terminal_air.pojo.dto.RegisterDTO;
import top.srcandy.terminal_air.pojo.model.User;

@Mapper
public interface UserMapper {

    @Results(id = "userResultMap", value = {
            @Result(property = "uid", column = "uid"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "password_hash", column = "password_hash"),
            @Result(property = "password", column = "password"),
            @Result(property = "salt", column = "salt"),
            @Result(property = "email", column = "email"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "isTwoFactorAuth", column = "isTwoFactorAuth"),
            @Result(property = "twoFactorAuthSecret", column = "twoFactorAuthSecret"),
            @Result(property = "createTime", column = "create_time"),
    })
    @Select("SELECT * FROM User WHERE username = #{username}")
    User selectByUserName(String username);

    @Select("SELECT * FROM User WHERE phone = #{phone}")
    @ResultMap("userResultMap")
    User selectByUserPhone(String phone);

    @Insert("insert into User (username,password,salt,phone,twoFactorAuthSecret) values (#{username},#{password},#{salt},#{phone},#{twoFactorSecret})")
    int insertSelective(RegisterDTO request);

    @Update("update User set username = #{username},password = #{password},password_hash = #{password_hash},phone = #{phone},email = #{email},nickname = #{nickname},avatar = #{avatar},isTwoFactorAuth = #{isTwoFactorAuth},twoFactorAuthSecret = #{twoFactorAuthSecret} where uid = #{uid}")
    void update(User updateProfileRequest);


}
