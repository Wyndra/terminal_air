package top.srcandy.candyterminal.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.converter.ConnectConverter;
import top.srcandy.candyterminal.mapper.ConnectManageMapper;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.AddConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectRequest;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.ConnectManageService;
import top.srcandy.candyterminal.utils.AESUtils;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@Slf4j
public class ConnectManageServiceImpl implements ConnectManageService {
    @Autowired
    private ConnectManageMapper connectManageMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private ConnectConverter connectConverter;


    public ResponseResult<List<ConnectInfo>> getUserConnects(String token) {
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        log.info("user:{}", user);
        return ResponseResult.success(connectManageMapper.selectByConnectCreaterUid(user.getUid()));
    }


    @Override
    public ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(Long connectCreaterUid) {
        return ResponseResult.success(connectManageMapper.selectByConnectCreaterUid(connectCreaterUid));
    }

    @Override
    public ResponseResult<ConnectInfo> insertConnect(String token, AddConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        // 获取用户的连接信息
        List<ConnectInfo> userConnects = connectManageMapper.selectByConnectCreaterUid(user.getUid());
        log.info("userConnects:{}", userConnects);

        // 判断新增的连接是否已经存在
        for (ConnectInfo userConnect : userConnects) {
            if (userConnect.getConnectHost().equals(request.getHost())) {
                return ResponseResult.fail(null, "连接已存在");
            }
        }

        String user_salt = user.getSalt();

        // 插入连接
        ConnectInfo connect = ConnectInfo.builder()
                .connect_creater_uid(user.getUid())
                .connectHost(request.getHost())
                .connectPort(request.getPort())
                .connectUsername(request.getUsername())
                .connectPwd(AESUtils.encryptToHex(request.getPassword(), user_salt))
                .connectName(request.getName())
                .connectMethod(request.getMethod())
                .build();
        connectManageMapper.insertConnect(connect);
        return ResponseResult.success(connect);
    }

    @Override
    public ResponseResult<ConnectInfo> updateConnect(String token, UpdateConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        // 获取用户的连接信息
        ConnectInfo connect = connectManageMapper
                .selectByConnectCreaterUid(user.getUid())
                .stream()
                .filter(connectInfo -> connectInfo.getCid().equals(request.getCid()))
                .findFirst()
                .orElse(null);

        if (connect == null) {
            return ResponseResult.fail(null, "连接不存在");
        }

        // 更新连接
        connectConverter.updateConnectRequestToConnectInfo(request, connect);
        // 密钥加密
        String user_salt = user.getSalt();
        connect.setConnectPwd(AESUtils.encryptToHex(request.getPassword(), user_salt));
        connectManageMapper.updateConnect(connect);
        return ResponseResult.success(connect);
    }

    @Override
    public ResponseResult<ConnectInfo> deleteConnect(String token, Long cid) {
        // 提取 token 并解析用户名
        String tokenNoBearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(tokenNoBearer).get("username").asString();

        // 验证用户是否存在
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }

        // 获取用户的连接信息
        ConnectInfo connectInfo = connectManageMapper
                .selectByConnectCreaterUid(user.getUid())
                .stream()
                .filter(connect -> connect.getCid().equals(cid))
                .findFirst()
                .orElse(null);

        if (connectInfo == null) {
            return ResponseResult.fail(null, "连接不存在");
        }

        // 删除连接
        connectManageMapper.deleteConnect(cid);
        return ResponseResult.success(null);
    }



}
