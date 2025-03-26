package top.srcandy.candyterminal.mapper;

import org.apache.ibatis.annotations.*;
import top.srcandy.candyterminal.model.Connection;

import java.util.List;

@Mapper
public interface ConnectionMapper {


    @Select("select cid, connect_host, connect_port, connect_username, connect_pwd,connect_name, connect_creater_uid, connect_method from Connection where connect_creater_uid = #{connectCreaterUid}")
    @Results({
            @Result(property = "cid", column = "cid"),
            @Result(property = "connectHost", column = "connect_host"),
            @Result(property = "connectPort", column = "connect_port"),
            @Result(property = "connectUsername", column = "connect_username"),
            @Result(property = "connectPwd", column = "connect_pwd", javaType = String.class),
            @Result(property = "connectName", column = "connect_name"),
            @Result(property = "connectMethod", column = "connect_method"),
            @Result(property = "connect_creater_uid", column = "connect_creater_uid")
    })
    List<Connection> selectByConnectCreaterUid(Long connectCreaterUid);

    @Insert("insert into Connection(connect_creater_uid, connect_name, connect_host, connect_port, connect_username, connect_pwd, connect_method) values(#{connect_creater_uid}, #{connectName}, #{connectHost}, #{connectPort}, #{connectUsername}, #{connectPwd}, #{connectMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "cid", keyColumn = "cid")
    void insertConnect(Connection connect);

    @Delete("delete from Connection where cid = #{cid}")
    void deleteConnect(Long cid);

    @Update("update Connection set connect_name = #{connectName}, connect_host = #{connectHost}, connect_port = #{connectPort}, connect_username = #{connectUsername}, connect_pwd = #{connectPwd}, connect_method = #{connectMethod} where cid = #{cid}")
    void updateConnect(Connection connect);
}
