package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.bean.vo.ConnectionVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.converter.ConnectionConverter;
import top.srcandy.candyterminal.mapper.ConnectionMapper;
import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.AddConnectionRequest;
import top.srcandy.candyterminal.request.UpdateConnectionRequest;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.ConnectionService;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.AESUtils;
import top.srcandy.candyterminal.utils.JWTUtil;

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
    private AuthService authService;

    @Autowired
    private ConnectionConverter connectConverter;

    @Autowired
    private CredentialsService credentialsService;


    public ResponseResult<List<ConnectionVO>> list(String token) {
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        log.info("user:{}", user);
        List<Connection> connections = connectionMapper.selectByConnectCreaterUid(user.getUid());

        return ResponseResult.success(connectConverter.connectionList2ConnectionVOList(connections));
    }


    @Override
    public ResponseResult<List<Connection>> selectByConnectCreaterUid(Long connectCreaterUid) {
        return ResponseResult.success(connectionMapper.selectByConnectCreaterUid(connectCreaterUid));
    }



    @Override
    public ResponseResult<Connection> insertConnect(String token, AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        // 获取用户的连接信息
        List<Connection> userConnects = connectionMapper.selectByConnectCreaterUid(user.getUid());
        log.info("userConnects:{}", userConnects);

        // 判断新增的连接是否已经存在
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
                .connectUsername(request.getUsername())
                .connectPwd(request.getPassword().equals("") ? "" : AESUtils.encryptToHex(request.getPassword(), userSalt))
                .connectName(request.getName())
                .connectMethod(request.getMethod())
                .build();
        connectionMapper.insertConnect(connect);
        return ResponseResult.success(connect);
    }

    @Override
    public ResponseResult<ConnectionVO> updateConnect(String token, UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        User user = authService.getUserByUsername(username);
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
                    credential = credentialsService.selectCredentialByUuid(token.substring(7), request.getCredentialUUID());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updatedConnect.setCredentialId(credential.getId());
            }

            connectionMapper.updateConnect(updatedConnect);
            ConnectionVO connectionVO = connectConverter.connection2ConnectionVO(updatedConnect);
            return ResponseResult.success(connectionVO);
        }).orElseGet(() -> ResponseResult.fail(null, "连接不存在"));
    }

    @Override
    public ResponseResult<Connection> deleteConnect(String token, Long cid) {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        Optional<User> optionalUser = Optional.ofNullable(authService.getUserByUsername(username));
        if (optionalUser.isEmpty()) {
            return ResponseResult.fail(null, "用户不存在");
        }

        Optional<Connection> optionalConnectInfo = connectionMapper
                .selectByConnectCreaterUid(optionalUser.get().getUid())
                .stream()
                .filter(connect -> connect.getCid().equals(cid))
                .findFirst();
        return optionalConnectInfo
                .map(connectInfo -> {
                    connectionMapper.deleteConnect(cid);
                    return ResponseResult.success(connectInfo);
                })
                .orElseGet(() -> ResponseResult.fail(null, "连接不存在"));
    }


}
