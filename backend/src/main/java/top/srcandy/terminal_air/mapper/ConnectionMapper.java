package top.srcandy.terminal_air.mapper;

import org.apache.ibatis.annotations.*;
import org.checkerframework.checker.guieffect.qual.UIPackage;
import top.srcandy.terminal_air.model.Connection;

import java.util.List;

@Mapper
public interface ConnectionMapper {

    @Select("select cid,connect_uuid,connect_host, connect_port, connect_username, connect_pwd,connect_name, connect_creater_uid, connect_credential,connect_method from Connection where connect_creater_uid = #{connectCreaterUid}")
    @Results({
            @Result(property = "cid", column = "cid"),
            @Result(property = "connectionUuid", column = "connect_uuid"),
            @Result(property = "connectHost", column = "connect_host"),
            @Result(property = "connectPort", column = "connect_port"),
            @Result(property = "connectUsername", column = "connect_username"),
            @Result(property = "connectPwd", column = "connect_pwd", javaType = String.class),
            @Result(property = "connectName", column = "connect_name"),
            @Result(property = "connectMethod", column = "connect_method"),
            @Result(property = "credentialId", column = "connect_credential"),
            @Result(property = "connect_creater_uid", column = "connect_creater_uid")
    })
    List<Connection> selectByConnectCreaterUid(Long connectCreaterUid);

    @Insert("insert into Connection(connect_creater_uid, connect_uuid, connect_name, connect_host, connect_port, connect_username, connect_pwd, connect_method) values(#{connect_creater_uid},#{connectionUuid}, #{connectName}, #{connectHost}, #{connectPort}, #{connectUsername}, #{connectPwd}, #{connectMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "cid", keyColumn = "cid")
    void insertConnect(Connection connect);

    @Delete("delete from Connection where cid = #{cid}")
    void deleteConnect(Long cid);

    @Delete("delete from Connection where connect_uuid = #{connectionUuid}")
    void deleteConnectByUuid(String connectionUuid);

    @Update("update Connection set connect_name = #{connectName}, connect_host = #{connectHost}, connect_port = #{connectPort}, connect_username = #{connectUsername}, connect_pwd = #{connectPwd}, connect_method = #{connectMethod}, connect_credential = #{credentialId} where cid = #{cid}")
    void updateConnect(Connection connect);

    /* 当连接查询的时候，检查connect_uuid是否存在
    * 如果为NULL，则update一个新的UUID上去
    * 如果不为NULL，则不做任何操作
     */
    @Update("UPDATE Connection SET connect_uuid = #{connectionUuid} WHERE cid = #{cid} AND connect_uuid IS NULL")
    int updateConnectUuid(Connection connect);


}
