package top.srcandy.candyterminal.converter;

import org.mapstruct.*;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.UpdateProfileRequest;

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
    void updateUserProfileRequestToUser(UpdateProfileRequest request, @MappingTarget User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "salt", source = "salt")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "avatar", source = "avatar")
    UserProfileVO userToUserProfileVO(User user);
}
