package top.srcandy.terminal_air.converter;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import top.srcandy.terminal_air.pojo.vo.ConnectionVo;
import top.srcandy.terminal_air.pojo.model.Connection;
import top.srcandy.terminal_air.request.UpdateConnectionRequest;
import top.srcandy.terminal_air.service.CredentialsService;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ConnectionConverter {

    @Autowired
    protected CredentialsService credentialsService;

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cid", source = "cid")
    @Mapping(target = "connectHost", source = "host")
    @Mapping(target = "connectPort", source = "port")
    @Mapping(target = "connectUsername", source = "username")
    @Mapping(target = "connectPwd", source = "password")
    @Mapping(target = "connectName", source = "name")
    @Mapping(target = "connectMethod", source = "method")
    public abstract Connection request2connection(UpdateConnectionRequest request);

    @Mapping(target = "cid", source = "cid")
    @Mapping(target = "connectHost", source = "connectHost")
    @Mapping(target = "connectPort", source = "connectPort")
    @Mapping(target = "connectUsername", source = "connectUsername")
    @Mapping(target = "connectPwd", source = "connectPwd")
    @Mapping(target = "connectName", source = "connectName")
    @Mapping(target = "connectMethod", source = "connectMethod")
    @Mapping(target = "connectionUuid", source = "connectionUuid")
    @Mapping(target = "connect_creater_uid", source = "connect_creater_uid")
    public abstract ConnectionVo connection2ConnectionVO(Connection connection);

    public abstract List<ConnectionVo> connectionList2ConnectionVOList(List<Connection> connections);

    @AfterMapping
    protected void mapCredentialUUID(@MappingTarget ConnectionVo connectionVO, Connection connection) {
        if (connection.getCredentialId() != null) {
            try {
                connectionVO.setCredentialUUID(credentialsService.selectCredentialById(connection.getCredentialId()).getUuid());
            } catch (Exception e) {
                // 处理异常，例如记录日志或者设置默认值
                connectionVO.setCredentialUUID(null);
                log.error("Failed to fetch credential UUID: {}", e.getMessage());
            }
        }
    }

    @AfterMapping
    protected void mapCredentialUUID(@MappingTarget Connection connection, UpdateConnectionRequest request) {
        if (request.getCredentialUUID() != null) {
            try {
                connection.setCredentialId(credentialsService.selectCredentialByUuid(request.getCredentialUUID()).getId());
            } catch (Exception e) {
                // 处理异常，例如记录日志或者设置默认值
                connection.setCredentialId(null);
                log.error("Failed to fetch credential UUID: {}", e.getMessage());
            }
        }
    }

}
