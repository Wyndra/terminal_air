package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.ConnectionVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.request.AddConnectionRequest;
import top.srcandy.candyterminal.request.UpdateConnectionRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface ConnectionService {

    ResponseResult<List<ConnectionVO>> list(String token);

    ResponseResult<List<Connection>> selectByConnectCreaterUid(Long connectCreaterUid);

    ResponseResult<Connection> insertConnect(String token, AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<ConnectionVO> updateConnect(String token, UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<Connection> deleteConnect(String token, Long cid);



}
