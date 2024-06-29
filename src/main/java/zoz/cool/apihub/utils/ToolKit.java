package zoz.cool.apihub.utils;

import cn.hutool.crypto.digest.BCrypt;

import java.util.UUID;

public class ToolKit {
    public static String getTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getEncryptPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static String getRandomCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
