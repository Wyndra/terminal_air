package top.srcandy.terminal_air.converter;

import org.mapstruct.*;
import top.srcandy.terminal_air.pojo.vo.UserProfileVO;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.UpdateProfileRequest;

@Mapper(componentModel = "spring")
public interface UserProfileConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "salt", source = "salt")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "avatar", source = "avatar")
    @Mapping(target = "isTwoFactorAuth", source = "isTwoFactorAuth")
    @Mapping(target = "twoFactorAuthSecret", source = "twoFactorAuthSecret")
    void updateUserProfileRequestToUser(UpdateProfileRequest request, @MappingTarget User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserProfileVO user2UserProfileVO(User user);
}
