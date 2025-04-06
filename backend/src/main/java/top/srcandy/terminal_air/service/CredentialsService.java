package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.bean.vo.CredentialVO;
import top.srcandy.terminal_air.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;

import java.util.List;

public interface CredentialsService {

    CredentialVO generateKeyPair(String token, String name, String tags) throws Exception;

//    Credential generateKeyPairFork(String token, String name,String tags) throws Exception;

    List<CredentialVO> listCredentials(String token) throws Exception;

    int countCredentialsByUserId(String token);

    Credential selectCredentialById(String token, Long id) throws Exception;

    Credential selectCredentialByUuid(String token, String uuid) throws Exception;

    Credential selectCredentialById(Long id) throws Exception;

    Credential selectCredentialByUuid(String uuid) throws Exception;

    void updateCredentialStatus(String token, CredentialStatusRequest request) throws Exception;

    CredentialVO updateCredentialConnectId(String token, CredentialConnectionRequest request) throws Exception;

    String generateInstallShell(String token, String uuid,String endpoint) throws Exception;

    /**
     * 通过连接id查询 已绑定 的凭据
     * @param token
     * @param connectId
     * @return
     * @throws Exception
     */
    List<Credential> selectBoundCredentialsByConnectionId(String token, String connectionUuid) throws Exception;

    void deleteCredential(String token, Long id) throws Exception;

}
