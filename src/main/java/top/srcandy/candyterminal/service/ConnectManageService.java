package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.request.AddConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface ConnectManageService {

    ResponseResult<List<ConnectInfo>> list(String token);

    ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(Long connectCreaterUid);

    ResponseResult<ConnectInfo> insertConnect(String token, AddConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<ConnectInfo> updateConnect(String token, UpdateConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<ConnectInfo> deleteConnect(String token, Long cid);



}
