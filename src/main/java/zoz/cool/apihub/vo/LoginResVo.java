package zoz.cool.apihub.vo;

import lombok.Data;

@Data
public class LoginResVo {
    private String token;
    private String tokenHead;
    private Long expire;

    public LoginResVo(String token, String tokenHead, Long expire) {
        this.token = token;
        this.tokenHead = tokenHead;
        this.expire = expire;
    }
}
