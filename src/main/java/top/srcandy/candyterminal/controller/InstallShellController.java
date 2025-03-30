package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.service.CredentialsService;
import java.util.Base64;

@RestController
@Slf4j
@Validated
@Tag(name = "InstallShell Service", description = "Install sh script")
@RequestMapping("/install")
public class InstallShellController {

    @Autowired
    private CredentialsService credentialsService;

    @GetMapping(value = "/install.sh", produces = "text/plain;charset=UTF-8")
    @AuthAccess
    public String generateInstallShell(@RequestParam String token, @RequestParam Long id) throws Exception {
        String publicKey = credentialsService.selectCredentialById(token.substring(7), id).getPublicKey();

        // Encode public key using Base64 to prevent shell parsing issues
        String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getBytes());

        return String.format("#!/bin/bash\n" +
                "\n" +
                "echo \"Decoding SSH public key...\"\n" +
                "public_key=$(echo '%s' | base64 -d)\n" +
                "\n" +
                "files=$(find /home /root -name 'authorized_keys' 2>/dev/null)\n" +
                "\n" +
                "if [ -z \"$files\" ]; then\n" +
                "    echo \"No authorized_keys file found, creating one...\"\n" +
                "    mkdir -p /root/.ssh\n" +
                "    touch /root/.ssh/authorized_keys\n" +
                "    chmod 600 /root/.ssh/authorized_keys\n" +
                "    files=\"/root/.ssh/authorized_keys\"\n" +
                "fi\n" +
                "\n" +
                "for file in $files; do\n" +
                "    echo \"$public_key\" >> \"$file\"\n" +
                "done\n" +
                "\n" +
                "echo \"SSH key installed successfully.\"\n" +
                "echo \"You can now SSH into the system using the private key.\"\n", encodedPublicKey);
    }
}
