package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.request.AddConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface ConnectionService {

    ResponseResult<List<Connection>> list(String token);

    ResponseResult<List<Connection>> selectByConnectCreaterUid(Long connectCreaterUid);

    ResponseResult<Connection> insertConnect(String token, AddConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<Connection> updateConnect(String token, UpdateConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<Connection> deleteConnect(String token, Long cid);



}
