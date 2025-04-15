package top.srcandy.terminal_air.mapper;

import org.apache.ibatis.annotations.*;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;

import java.util.List;

@Mapper
public interface CredentialsMapper {

    /**
     * 统一的结果映射
     */
    @Results(id = "credentialResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "name", column = "name"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "userId", column = "uid"),
            @Result(property = "status", column = "status"),
            @Result(property = "fingerprint", column = "fingerprint"),
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "privateKey", column = "private_key"),
            @Result(property = "connectId", column = "cid"),
            @Result(property = "createTime", column = "created_at"),
            @Result(property = "updateTime", column = "updated_at")
    })
    @Select("SELECT * FROM Credentials WHERE uid = #{userId}")
    List<Credential> selectCredentialsByUserId(Long userId);

    @Select("SELECT * FROM Credentials WHERE id = #{id}")
    @ResultMap("credentialResultMap")
    Credential selectCredentialById(Long id);

    /**
     * 根据用户ID和凭据ID查询凭据
     */
    @Select("SELECT * FROM Credentials WHERE id = #{id} AND uid = #{uid}")
    @ResultMap("credentialResultMap")
    Credential selectCredentialByUidAndId(Long uid, Long id);

    /**
     * 根据用户ID和凭据UUID查询凭据
     */
    @Select("SELECT * FROM Credentials WHERE uuid = #{uuid} AND uid = #{uid}")
    @ResultMap("credentialResultMap")
    Credential selectCredentialByUidAndUuid(Long uid, String uuid);

    @Select("SELECT * FROM Credentials WHERE uuid = #{uuid}")
    @ResultMap("credentialResultMap")
    Credential selectCredentialByUuid(String uuid);

    /**
     * 统计用户同名凭据数量
     */
    @Select("SELECT COUNT(*) FROM Credentials WHERE uid = #{userId} AND name = #{name}")
    int countCredentialsByUserIdAndName(Long userId, String name);

    /**
     * 统计用户凭据总数
     */
    @Select("SELECT COUNT(*) FROM Credentials WHERE uid = #{userId}")
    int countCredentialsByUserId(@Param("userId") Long userId);

    /**
     * 根据连接ID查询凭据
     */
    @Select("SELECT * FROM Credentials WHERE cid = #{connectId}")
    @ResultMap("credentialResultMap")
    List<Credential> selectCredentialsByConnectId(Long connectId);


    @Select("SELECT * FROM Credentials WHERE cid = #{connectId} AND status = 2 AND uid = #{userId}")
    @ResultMap("credentialResultMap")
    List<Credential> selectBoundCredentialsByConnectionId(Long userId, Long connectId);

    /**
     * 根据Connection ID查询已绑定的凭据
     */
    @Select("SELECT * FROM Credentials WHERE cid = (SELECT cid from Connection where connect_uuid = #{connectionUuid}) AND status = 2 AND uid = #{userId}")
    @ResultMap("credentialResultMap")
    List<Credential> selectBoundCredentialsByConnectionUuid(Long userId,String connectionUuid);

    /**
     * 插入凭据
     */
    @Insert("INSERT INTO Credentials (uuid, name, uid, status, tags, fingerprint, public_key, private_key, created_at, updated_at) " +
            "VALUES (#{uuid}, #{name}, #{userId}, #{status}, #{tags}, #{fingerprint}, #{publicKey}, #{privateKey}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCredential(Credential credential);

    /**
     * 删除凭据
     */
    @Delete("DELETE FROM Credentials WHERE id = #{id}")
    void deleteCredential(Long id);

    @Delete("DELETE FROM Credentials WHERE uuid = #{uuid}")
    void deleteCredentialByUuid(String uuid);

    @Update("UPDATE Credentials SET status = #{status} WHERE uuid = #{uuid}")
    void updateCredentialStatus(CredentialStatusRequest request);

    /**
     * 更新凭据连接ID
     */
    @Update("UPDATE Credentials SET cid = #{connectId},status = 2 WHERE uuid = #{uuid}")
    void updateCredentialConnectId(CredentialConnectionRequest request);

    /**
     * 更新凭据
     */
    @Update("UPDATE Credentials SET name = #{name}, public_key = #{publicKey}, private_key = #{privateKey}, " +
            "cid = #{connectId}, updated_at = #{updateTime} WHERE id = #{id}")
    void updateCredential(Credential credential);
}
