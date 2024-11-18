package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.mapper.ConnectManageMapper;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.ConnectManageService;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.util.List;

@Service
@Slf4j
public class ConnectManageServiceImpl implements ConnectManageService {
    @Autowired
    private ConnectManageMapper connectManageMapper;

    @Autowired
    private AuthService authService;


    @Override
    public ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(Long connectCreaterUid) {
        return ResponseResult.success(connectManageMapper.selectByConnectCreaterUid(connectCreaterUid));
    }

    @Override
    public ResponseResult<ConnectInfo> insertConnect(ConnectInfo connect) {
        connectManageMapper.insertConnect(connect);
        return ResponseResult.success(connect);
    }

    @Override
    public ResponseResult<ConnectInfo> updateConnect(ConnectInfo connect) {
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
