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

    @Autowired
    protected CredentialConverter credentialConverter;

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "cid")
    @Mapping(target = "host", source = "host")
    @Mapping(target = "port", source = "port")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "method", source = "method")
    public abstract Connection request2connection(UpdateConnectionRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "host", source = "host")
    @Mapping(target = "port", source = "port")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "method", source = "method")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "user_id", source = "user_id")
    public abstract ConnectionVo connection2ConnectionVO(Connection connection);

    public abstract List<ConnectionVo> connectionList2ConnectionVOList(List<Connection> connections);

    @AfterMapping
    protected void mapCredentialUUID(@MappingTarget ConnectionVo connectionVO, Connection connection) {
        if (connection.getCredentialId() != null) {
            try {
                connectionVO.setCredentialUUID(credentialsService.selectCredentialById(connection.getCredentialId()).getUuid());
                // 将凭证信息转换为VO对象
                connectionVO.setCredential(credentialConverter.credential2VO(credentialsService.selectCredentialById(connection.getCredentialId())));
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
