package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.ConnectionVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.model.Connection;
import top.srcandy.terminal_air.request.AddConnectionRequest;
import top.srcandy.terminal_air.request.UpdateConnectionRequest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface ConnectionService {

    ResponseResult<List<ConnectionVO>> list(Long userid);

    ResponseResult<List<Connection>> selectByConnectCreaterUid(Long connectCreaterUid);

    ResponseResult<Connection> insertConnect(AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<ConnectionVO> updateConnect(UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException;

    ResponseResult<Connection> deleteConnect(String connectionUuid);



}
