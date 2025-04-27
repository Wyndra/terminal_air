package top.srcandy.terminal_air.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

@Mapper
public interface TwoFactorAuthMapper {
    @Insert("INSERT IGNORE INTO TwoFactorAuth(uid, twoFactorAuthSecret) VALUES (#{uid}, #{twoFactorAuthSecret});")
    void initTwoFactorAuthSecret(Long uid, String twoFactorAuthSecret);

    @Select("select count(tfid) from TwoFactorAuth where uid = #{uid}")
    int countTwoFactorAuthSecret(Long uid);

    @Update("update TwoFactorAuth set twoFactorTitle = #{twoFactorTitle},oneTimeCodeBackup = #{oneTimeCodeBackup} where uid = #{uid}")
    void updateTwoFactorAuthSecret(Long uid, String twoFactorTitle, String oneTimeCodeBackup);

    @Update("update TwoFactorAuth set isTwoFactorAuth = #{status} where uid = #{uid}")
    void updateTwoFactorAuthStatus(Long uid, String status);

    @Update("update TwoFactorAuth set twoFactorTitle = #{title},oneTimeCodeBackup = #{oneTimeCodeBackup} where uid = #{uid}")
    void updateTwoFactorAuthTitleAndBackup(Long uid, String title, String oneTimeCodeBackup);

    @Update("update TwoFactorAuth set oneTimeCodeBackup = #{oneTimeCodeBackup} where uid = #{uid}")
    void updateTwoFactorAuthOneTimeCodeBackup(Long uid, String oneTimeCodeBackup);

    @Select("select twoFactorAuthSecret from TwoFactorAuth where uid = #{uid}")
    String getUserTwoFactorAuthSecret(Long uid);

    @Select("select twoFactorTitle from TwoFactorAuth where uid = #{uid}")
    String getUserTwoFactorAuthTitle(Long uid);

    @Select("select oneTimeCodeBackup from TwoFactorAuth where uid = #{uid}")
    String getUserOneTimeCodeBackup(Long uid);

    @Select("select isTwoFactorAuth from TwoFactorAuth where uid = #{uid}")
    String getUserTwoFactorAuthStatus(Long uid);

    @Delete("delete from TwoFactorAuth where uid = #{uid}")
    void deleteTwoFactorAuth(Long uid);

}
