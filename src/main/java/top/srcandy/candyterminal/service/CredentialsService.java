package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.model.Credential;

import java.security.NoSuchAlgorithmException;

public interface CredentialsService {

    Credential generateKeyPair(String token, Long connectId, String name, String passphrase) throws Exception;

}
