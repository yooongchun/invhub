package zoz.cool.apihub.utils;

import cn.hutool.crypto.digest.BCrypt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static String genOrderId() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date()) + System.currentTimeMillis();
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String calFileHash(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = bis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            BigInteger bigInt = new BigInteger(1, hashBytes);
            StringBuilder hashHex = new StringBuilder(bigInt.toString(16));
            while (hashHex.length() < 32) {
                hashHex.insert(0, "0");
            }
            return hashHex.toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException("Could not calculate hash", ex);
        }
    }
}
