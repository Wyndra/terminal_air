package top.srcandy.candyterminal.bean.vo;

import com.lzhpo.sensitive.SensitiveStrategy;
import com.lzhpo.sensitive.annocation.Sensitive;
import com.lzhpo.sensitive.annocation.SensitiveFilterWords;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class UserProfileVO {
    private Long uid;
    private String username;
    @Sensitive(strategy = SensitiveStrategy.MOBILE_PHONE)
    private String phone;
    private String email;
    private String avatar;
    @Sensitive(strategy = SensitiveStrategy.CUSTOMIZE_FILTER_WORDS)
    @SensitiveFilterWords({"他妈的", "去你大爷", "卧槽", "草泥马", "废物", "垃圾", "操你妈", "操你", "操你大爷", "操你奶奶", "操你爷爷", "操你祖宗", "操你全家", "你妈逼", "你妈的",})
    private String nickname;
    @Sensitive(strategy = SensitiveStrategy.PASSWORD)
    private String salt;
    private String isTwoFactorAuth;
    private Timestamp createTime;
}
