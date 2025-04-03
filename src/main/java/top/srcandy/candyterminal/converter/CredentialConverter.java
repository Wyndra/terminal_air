package top.srcandy.candyterminal.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.srcandy.candyterminal.bean.vo.CredentialVO;
import top.srcandy.candyterminal.model.Credential;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CredentialConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "fingerprint", source = "fingerprint")
    @Mapping(target = "publicKey", source = "publicKey")
    @Mapping(target = "connectId", source = "connectId")
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "updateTime", source = "updateTime")
    CredentialVO credential2VO(Credential credential);

    List<CredentialVO> credentialList2VOList(List<Credential> credentials);
}
