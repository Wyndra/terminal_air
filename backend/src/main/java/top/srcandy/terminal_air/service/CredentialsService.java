package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.CredentialVo;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;
import top.srcandy.terminal_air.request.CredentialStatusShortTokenRequest;

import java.util.List;

public interface CredentialsService {

    CredentialVo generateKeyPair(String name, String tags) throws Exception;


    List<CredentialVo> listCredentials() throws Exception;

    int countCredentialsByUserId();

    Credential selectCredentialById(Long id) throws Exception;

    Credential selectCredentialByUuid(String uuid) throws Exception;

    Credential getCredentialByUuidSkipAuth(String uuid) throws Exception;

    void updateCredentialStatus(CredentialStatusRequest request) throws Exception;

    void updateCredentialStatusByShortToken(CredentialStatusShortTokenRequest request) throws Exception;

    CredentialVo updateCredentialConnectId(CredentialConnectionRequest request) throws Exception;

    String generateInstallShell(String uuid,String extra) throws Exception;

    String getInstallShellUrl(String endpoint, String uuid) throws Exception;

    List<CredentialVo> selectBoundCredentialsByConnectionId(String connectionUuid) throws Exception;

    void deleteCredential(String uuid) throws Exception;

}
