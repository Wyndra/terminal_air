package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.ConnectInfo;

import java.util.List;

public interface ConnectManageService {
    ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(Long connectCreaterUid);

    ResponseResult<ConnectInfo> insertConnect(ConnectInfo connect);

    ResponseResult<ConnectInfo> updateConnect(ConnectInfo connect);

    ResponseResult<ConnectInfo> deleteConnect(Long cid);

}
