package top.srcandy.candyterminal.converter;

import org.mapstruct.*;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.request.UpdateConnectRequest;

@Mapper(componentModel = "spring")
public interface ConnectConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cid", source = "cid")
    @Mapping(target = "connectHost", source = "host")
    @Mapping(target = "connectPort", source = "port")
    @Mapping(target = "connectUsername", source = "username")
    @Mapping(target = "connectPwd", source = "password")
    @Mapping(target = "connectName", source = "name")
    @Mapping(target = "connectMethod", source = "method")
    void updateConnectRequestToConnectInfo(UpdateConnectRequest request, @MappingTarget ConnectInfo connectInfo);
}
