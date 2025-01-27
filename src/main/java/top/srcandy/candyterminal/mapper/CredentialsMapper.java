package top.srcandy.candyterminal.mapper;

import com.lzhpo.sensitive.annocation.SensitiveKeepLength;
import org.apache.ibatis.annotations.*;
import top.srcandy.candyterminal.model.Credential;

import java.util.List;

@Mapper
public interface CredentialsMapper {
    @Select("select id,name,public_key,cid,create_time,update_time from Credentials where uid = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "connectId", column = "cid"),
            @Result(property = "createTime", column = "create_at"),
            @Result(property = "updateTime", column = "update_at")
    })
    List<Credential> selectCredentialsByUserId(Long userId);

    @Select("select id,name,public_key,uid,create_time,update_time from Credentials where cid = #{connectId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "userId", column = "uid"),
            @Result(property = "createTime", column = "created_at"),
            @Result(property = "updateTime", column = "updated_at")
    })
    List<Credential> selectCredentialsByConnectId(Long connectId);


    @Insert("insert into Credentials(name,uid,public_key,private_key,passphrase,cid,created_at,updated_at) values(#{name}, #{userId}, #{publicKey}, #{privateKey}, #{passphrase}, #{connectId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCredential(Credential credential);

    @Delete("delete from Credentials where id = #{id}")
    void deleteCredential(Long id);

    @Update("update Credentials set name = #{name}, public_key = #{publicKey}, private_key = #{privateKey}, passphrase = #{passphrase}, cid = #{connectId}, updated_at = #{updateTime} where id = #{id}")
    void updateCredential(Credential credential);
}
