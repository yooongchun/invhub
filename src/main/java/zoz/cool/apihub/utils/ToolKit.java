package zoz.cool.apihub.utils;

import cn.hutool.crypto.digest.BCrypt;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    public static boolean checkPassword(String rawPassword, String encryptedPassword) {
        return BCrypt.checkpw(rawPassword, encryptedPassword);
    }

    public static String getRandomCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    public static Long getUid() {
        long timestamp = System.currentTimeMillis();
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp * 10000 + randomNum;
    }
}
