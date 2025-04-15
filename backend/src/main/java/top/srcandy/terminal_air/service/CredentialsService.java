package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.CredentialVO;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;

import java.util.List;

public interface CredentialsService {

    CredentialVO generateKeyPair(String name, String tags) throws Exception;


    List<CredentialVO> listCredentials() throws Exception;

    int countCredentialsByUserId();

    Credential selectCredentialById(Long id) throws Exception;

    Credential selectCredentialByUuid(String uuid) throws Exception;

    Credential getCredentialByUuidSkipAuth(String uuid) throws Exception;

    void updateCredentialStatus(CredentialStatusRequest request) throws Exception;

    CredentialVO updateCredentialConnectId(CredentialConnectionRequest request) throws Exception;

    String generateInstallShell(String token,String uuid,String endpoint) throws Exception;

    List<CredentialVO> selectBoundCredentialsByConnectionId(String connectionUuid) throws Exception;

    void deleteCredential(String uuid) throws Exception;

}
