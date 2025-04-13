package top.srcandy.terminal_air.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.srcandy.terminal_air.pojo.vo.CredentialVO;
import top.srcandy.terminal_air.pojo.model.Credential;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CredentialConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "fingerprint", source = "fingerprint")
    @Mapping(target = "publicKey", source = "publicKey")
    @Mapping(target = "connectId", source = "connectId")
    @Mapping(target = "createTime", source = "createTime")
    CredentialVO credential2VO(Credential credential);

    List<CredentialVO> credentialList2VOList(List<Credential> credentials);
}
