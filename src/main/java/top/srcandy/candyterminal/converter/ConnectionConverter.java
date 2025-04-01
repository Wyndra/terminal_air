package top.srcandy.candyterminal.converter;

import org.mapstruct.*;
import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.request.UpdateConnectionRequest;

@Mapper(componentModel = "spring")
public interface ConnectionConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cid", source = "cid")
    @Mapping(target = "connectHost", source = "host")
    @Mapping(target = "connectPort", source = "port")
    @Mapping(target = "connectUsername", source = "username")
    @Mapping(target = "connectPwd", source = "password")
    @Mapping(target = "connectName", source = "name")
    @Mapping(target = "connectMethod", source = "method")
    Connection request2connectInfo(UpdateConnectionRequest request);

}
