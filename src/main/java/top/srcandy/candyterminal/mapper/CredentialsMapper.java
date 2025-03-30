package top.srcandy.candyterminal.mapper;

import com.lzhpo.sensitive.annocation.SensitiveKeepLength;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.model.Credential;

import java.util.List;

@Mapper
public interface CredentialsMapper {
    @Select("select id,uuid,name,uid,tags,fingerprint,public_key,private_key,cid,created_at,updated_at from Credentials where uid = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "name", column = "name"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "userId", column = "uid"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "privateKey", column = "private_key"),
            @Result(property = "connectId", column = "cid"),
            @Result(property = "createTime", column = "created_at"),
            @Result(property = "updateTime", column = "updated_at")

    })
    List<Credential> selectCredentialsByUserId(Long userId);

    @Select("select id,uuid,name,uid,tags,fingerprint,public_key,private_key,cid,created_at,updated_at from Credentials where id = #{id} and uid = #{uid}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "name", column = "name"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "userId", column = "uid"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "privateKey", column = "private_key"),
            @Result(property = "connectId", column = "cid"),
            @Result(property = "createTime", column = "created_at"),
            @Result(property = "updateTime", column = "updated_at")
    })
    Credential selectCredentialByUidAndId(Long uid,Long id);

    @Select("SELECT COUNT(*) FROM Credentials WHERE uid = #{userId} AND name = #{name}")
    int countCredentialsByUserIdAndName(Long userId, String name);

    @Select("SELECT COUNT(*) FROM Credentials WHERE uid = #{userId}")
    int countCredentialsByUserId(@Param("userId") Long userId);

    @Select("select id,uuid,name,uid,tags,fingerprint,public_key,private_key,cid,created_at,updated_at from Credentials where cid = #{connectId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "name", column = "name"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "userId", column = "uid"),
            @Result(property = "createTime", column = "created_at"),
            @Result(property = "updateTime", column = "updated_at")
    })
    List<Credential> selectCredentialsByConnectId(Long connectId);


    @Insert("insert into Credentials(" +
            "uuid,name,uid,tags,fingerprint,public_key,private_key,created_at,updated_at) " +
            "values(#{uuid}, #{name}, #{userId}, #{tags}, #{fingerprint}, #{publicKey}, #{privateKey}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCredential(Credential credential);

    @Delete("delete from Credentials where id = #{id}")
    void deleteCredential(Long id);

    @Update("update Credentials set name = #{name}, public_key = #{publicKey}, private_key = #{privateKey}, cid = #{connectId}, updated_at = #{updateTime} where id = #{id}")
    void updateCredential(Credential credential);
}
