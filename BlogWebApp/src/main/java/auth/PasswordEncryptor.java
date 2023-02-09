package auth;

import java.math.BigInteger;
import java.security.*;

public class PasswordEncryptor {
    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String res = no.toString(16);

            while (res.length() < 32) {
                res = "0" + res;
            }

            return res;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
