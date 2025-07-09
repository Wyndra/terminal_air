package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.mapper.IConnectionDao;
import top.srcandy.terminal_air.pojo.dto.ConnectionDto;
import top.srcandy.terminal_air.pojo.vo.ConnectionVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.converter.ConnectionConverter;
import top.srcandy.terminal_air.mapper.ConnectionMapper;
import top.srcandy.terminal_air.pojo.model.Connection;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.AddConnectionRequest;
import top.srcandy.terminal_air.request.UpdateConnectionRequest;
import top.srcandy.terminal_air.service.ConnectionService;
import top.srcandy.terminal_air.service.CredentialsService;
import top.srcandy.terminal_air.utils.AESUtils;
import top.srcandy.terminal_air.utils.KeyUtils;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    private ConnectionMapper connectionMapper;

    private IConnectionDao connectionDao;

    @Autowired
    private ConnectionConverter connectConverter;

    @Autowired
    private CredentialsService credentialsService;


    public ConnectionServiceImpl(IConnectionDao connectionDao) {
        this.connectionDao = connectionDao;
    }

    @Override
    public ResponseResult<List<ConnectionVo>> list(Long userid) {
        List<Connection> connections = connectionMapper.selectByConnectCreaterUid(userid);
        connections.forEach(connection -> {
            if (connection.getUuid() == null || connection.getUuid().isEmpty()) {
                connection.setUuid(KeyUtils.generateUUID());
                connectionMapper.updateConnectUuid(connection);
            }
        });
        List<ConnectionVo> res = connectConverter.connectionList2ConnectionVOList(connections);
        return ResponseResult.success(res);
    }

    @Override
    public ResponseResult<List<ConnectionVo>> list2(Long userid) {
        List<ConnectionDto> connections = connectionDao.selectCredentialsByUserId(userid);
        return null;
    }

    @Override
    public ResponseResult<ConnectionVo> insertConnect(AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        User user = SecuritySessionUtils.getUser();

        List<Connection> userConnects = connectionMapper.selectByConnectCreaterUid(user.getUid());

        for (Connection userConnect : userConnects) {
            if (userConnect.getHost().equals(request.getHost())) {
                return ResponseResult.fail(null, "连接已存在");
            }
        }

        String userSalt = user.getSalt();
        // 插入连接
        Connection connect = Connection.builder()
                .user_id(user.getUid())
                .host(request.getHost())
                .port(request.getPort())
                .uuid(KeyUtils.generateUUID())
                .username(request.getUsername())
                .password(request.getPassword().equals("") ? "" : AESUtils.encryptToHex(request.getPassword(), userSalt))
                .name(request.getName())
                .method(request.getMethod())
                .build();
        connectionMapper.insertConnect(connect);
        return ResponseResult.success(connectConverter.connection2ConnectionVO(connect));
    }

    @Override
    public ResponseResult<ConnectionVo> updateConnect(UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        User user = SecuritySessionUtils.getUser();
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        Optional<Connection> optionalConnect = connectionMapper
                .selectByConnectCreaterUid(user.getUid())
                .stream()
                .filter(connectInfo -> connectInfo.getId().equals(request.getCid()))
                .findFirst();

        return optionalConnect.map(connect -> {
            String userSalt = user.getSalt();
            String requestPassword = request.getPassword();
            String storedPassword = connect.getPassword();
            // 先解密数据库中的密码
            String decryptedStoredPassword;
            try {
                decryptedStoredPassword = AESUtils.decryptFromHex(storedPassword, userSalt);
            } catch (Exception e) {
                throw new RuntimeException("密码解密失败", e);
            }
            // 明文密码与数据库中的密码不一致，说明密码有更新
            if (!requestPassword.equals(decryptedStoredPassword)) {
                try {
                    connect.setPassword(AESUtils.encryptToHex(requestPassword, userSalt));
                } catch (Exception e) {
                    throw new RuntimeException("密码加密失败", e);
                }
            }
            // 更新其他字段，但保留可能更新的密码
            Connection updatedConnect = connectConverter.request2connection(request);
            if (requestPassword.equals(storedPassword)) {
                updatedConnect.setPassword(storedPassword);
            }else {
                updatedConnect.setPassword(connect.getPassword());
            }
            if (requestPassword.equals("")) {
                updatedConnect.setPassword("");
            }
            if (request.getMethod().equals("key")){
                Credential credential = null;
                try {
                    credential = credentialsService.selectCredentialByUuid(request.getCredentialUUID());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updatedConnect.setCredentialId(credential.getId());
            }

            connectionMapper.updateConnect(updatedConnect);
            ConnectionVo connectionVO = connectConverter.connection2ConnectionVO(updatedConnect);
            return ResponseResult.success(connectionVO);
        }).orElseGet(() -> ResponseResult.fail(null, "连接不存在"));
    }

    @Override
    public ResponseResult<Connection> deleteConnect(String connectionUuid) {
        Long userId = SecuritySessionUtils.getUserId();
        // 判断用户是否有这个连接
        Optional<Connection> optionalConnectInfo = connectionMapper
                .selectByConnectCreaterUid(userId)
                .stream()
                .filter(c -> c.getUuid().equals(connectionUuid))
                .findFirst();

        return optionalConnectInfo
                .map(connectInfo -> {
                    connectionMapper.deleteConnectByUuid(connectionUuid);
                    return ResponseResult.success(connectInfo);
                })
                .orElseGet(() -> ResponseResult.fail(null, "连接不存在"));
    }


}
