package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private ConnectionConverter connectConverter;

    @Autowired
    private CredentialsService credentialsService;


    public ResponseResult<List<ConnectionVo>> list(Long userid) {
        List<Connection> connections = connectionMapper.selectByConnectCreaterUid(userid);
        connections.forEach(connection -> {
            if (connection.getConnectionUuid() == null || connection.getConnectionUuid().isEmpty()) {
                connection.setConnectionUuid(KeyUtils.generateUUID());
                connectionMapper.updateConnectUuid(connection);
            }
        });
        return ResponseResult.success(connectConverter.connectionList2ConnectionVOList(connections));
    }


    @Override
    public ResponseResult<List<Connection>> selectByConnectCreaterUid(Long connectCreaterUid) {
        return ResponseResult.success(connectionMapper.selectByConnectCreaterUid(connectCreaterUid));
    }



    @Override
    public ResponseResult<Connection> insertConnect(AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        User user = SecuritySessionUtils.getUser();

        List<Connection> userConnects = connectionMapper.selectByConnectCreaterUid(user.getUid());

        for (Connection userConnect : userConnects) {
            if (userConnect.getConnectHost().equals(request.getHost())) {
                return ResponseResult.fail(null, "连接已存在");
            }
        }

        String userSalt = user.getSalt();
        // 插入连接
        Connection connect = Connection.builder()
                .connect_creater_uid(user.getUid())
                .connectHost(request.getHost())
                .connectPort(request.getPort())
                .connectionUuid(KeyUtils.generateUUID())
                .connectUsername(request.getUsername())
                .connectPwd(request.getPassword().equals("") ? "" : AESUtils.encryptToHex(request.getPassword(), userSalt))
                .connectName(request.getName())
                .connectMethod(request.getMethod())
                .build();
        connectionMapper.insertConnect(connect);
        return ResponseResult.success(connect);
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
                .filter(connectInfo -> connectInfo.getCid().equals(request.getCid()))
                .findFirst();

        return optionalConnect.map(connect -> {
            String userSalt = user.getSalt();
            String requestPassword = request.getPassword();
            String storedPassword = connect.getConnectPwd();
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
                    connect.setConnectPwd(AESUtils.encryptToHex(requestPassword, userSalt));
                } catch (Exception e) {
                    throw new RuntimeException("密码加密失败", e);
                }
            }
            // 更新其他字段，但保留可能更新的密码
            Connection updatedConnect = connectConverter.request2connection(request);
            if (requestPassword.equals(storedPassword)) {
                updatedConnect.setConnectPwd(storedPassword);
            }else {
                updatedConnect.setConnectPwd(connect.getConnectPwd());
            }
            if (requestPassword.equals("")) {
                updatedConnect.setConnectPwd("");
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
                .filter(c -> c.getConnectionUuid().equals(connectionUuid))
                .findFirst();

        return optionalConnectInfo
                .map(connectInfo -> {
                    connectionMapper.deleteConnectByUuid(connectionUuid);
                    return ResponseResult.success(connectInfo);
                })
                .orElseGet(() -> ResponseResult.fail(null, "连接不存在"));
    }


}
