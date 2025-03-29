package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.model.Credential;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface CredentialsService {

    Credential generateKeyPair(String token, String name,String tags) throws Exception;

    List<Credential> listCredentials(String token) throws Exception;

    void deleteCredential(String token, Long id) throws Exception;

}
