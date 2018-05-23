package hdv.ble.tdx.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ann on 2/23/16.
 */
public class CommonUtils {
    public static String convertByteToString(byte[] bytes){
        String result = "";
        for (byte mByte: bytes){
            result += " " + String.format("%X", mByte);
        }
        return result;
    }
    public static byte[] generateMd5(byte[] bytes){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return messageDigest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] encryptePin(byte[] bytes){
        try {
            byte[] result = new byte[4];
            byte[] md5Generated = generateMd5(bytes);
            result[0] = (byte) (md5Generated[0] ^ md5Generated[1] ^ md5Generated[2] ^ md5Generated[3]);
            result[1] = (byte) (md5Generated[4] ^ md5Generated[5] ^ md5Generated[6] ^ md5Generated[7]);
            result[2] = (byte) (md5Generated[8] ^ md5Generated[9] ^ md5Generated[10] ^ md5Generated[11]);
            result[3] = (byte) (md5Generated[12] ^ md5Generated[13] ^ md5Generated[14] ^ md5Generated[15]);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static byte calculateCRC(byte[] bytesEncrypted, byte[] bytesData){
        byte result = 0;

        for (byte aByte : bytesEncrypted) {
            result += aByte;
        }
        for (byte aByte : bytesData) {
            result += aByte;
        }
        return result;
    }
}
