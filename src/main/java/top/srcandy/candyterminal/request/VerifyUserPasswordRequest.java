package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class VerifyUserPasswordRequest {
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
