package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.mapper.ConnectManageMapper;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.ConnectManageService;

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
    public ResponseResult<ConnectInfo> deleteConnect(Long cid) {
        connectManageMapper.deleteConnect(cid);
        return ResponseResult.success(null);
    }


}
