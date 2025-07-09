package top.srcandy.terminal_air.mapper;

import org.apache.ibatis.annotations.*;
import top.srcandy.terminal_air.pojo.model.Connection;

import java.util.List;

@Mapper
public interface ConnectionMapper {

    @Select("select id,uuid,host, port, username, password,name, user_id, credential,method from Connection where user_id = #{userId}")
    @Results({@Result(property = "id", column = "id"),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "host", column = "host"),
            @Result(property = "port", column = "port"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password", javaType = String.class),
            @Result(property = "name", column = "name"),
            @Result(property = "method", column = "method"),
            @Result(property = "credentialId", column = "credential"),
            @Result(property = "user_id", column = "user_id")})
    List<Connection> selectByConnectCreaterUid(Long userId);

    @Insert("insert into Connection(user_id, uuid, name, host, port, username,password, method) values(#{user_id},#{uuid}, #{name}, #{host}, #{port}, #{username}, #{password}, #{method})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertConnect(Connection connect);

    @Delete("delete from Connection where id = #{cid}")
    void deleteConnect(Long cid);

    @Delete("delete from Connection where uuid = #{connectionUuid}")
    void deleteConnectByUuid(String connectionUuid);

    @Update("update Connection set name = #{name}, host = #{host}, port = #{port}, username = #{username}, password = #{password}, method = #{method}, credential = #{credentialId} where id = #{id}")
    void updateConnect(Connection connect);

    /* 当连接查询的时候，检查uuid是否存在
     * 如果为NULL，则update一个新的UUID上去
     * 如果不为NULL，则不做任何操作
     */
    @Update("UPDATE Connection SET uuid = #{uuid} WHERE id = #{id} AND uuid IS NULL")
    int updateConnectUuid(Connection connect);


}
