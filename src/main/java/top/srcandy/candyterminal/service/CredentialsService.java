package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.request.CredentialConnectionRequest;
import top.srcandy.candyterminal.request.CredentialStatusRequest;

import java.util.List;

public interface CredentialsService {

    Credential generateKeyPair(String token, String name,String tags) throws Exception;

    List<Credential> listCredentials(String token) throws Exception;

    int countCredentialsByUserId(String token);

    Credential selectCredentialById(String token, Long id) throws Exception;

    Credential selectCredentialByUuid(String token, String uuid) throws Exception;

    void updateCredentialStatus(String token, CredentialStatusRequest request) throws Exception;

    Credential updateCredentialConnectId(String token, CredentialConnectionRequest request) throws Exception;

    String generateInstallShell(String token, String uuid,String endpoint) throws Exception;

    void deleteCredential(String token, Long id) throws Exception;

}
