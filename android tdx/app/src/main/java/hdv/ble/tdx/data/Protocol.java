package hdv.ble.tdx.data;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import hdv.ble.tdx.util.CommonUtils;

/**
 * Created by Ann on 2/23/16.
 */
public class Protocol {
    public static byte HEADER = (byte) (byte) 0xCA;


    public static byte OPCODE_LOGON = (byte) 0x01;
    public static byte OPCODE_CMD1 = (byte) 0x02;
    public static byte OPCODE_CMD2 = (byte) 0x03;
    public static byte OPCODE_CMD3 = (byte) 0x04;
    public static byte OPCODE_CMD4 = (byte) 0x05;
    public static byte OPCODE_FWVER = (byte) 0x06;
    public static byte OPCODE_PIN = (byte) 0x07;
    public static byte OPCODE_NAME = (byte) 0x08;
    public static byte OPCODE_STATUS = (byte) 0x09;
    public static byte OPCODE_READ_SMARTKEY = (byte) 0x0A;
    public static byte OPCODE_WRITE_SMARTKEY = (byte) 0x0B;

    public static byte STATUS_CODE_SUCCESS = (byte) 0x0;
    public static byte STATUS_CODE_BUSY = (byte) 0x44;
    public static byte STATUS_CODE_ERRORPIN = (byte) 0x55;
    public static byte STATUS_CODE_MALFUNCTION = (byte) 0x96;

    public static byte HEADER_OFFSET = (byte) 0x00;
    public static byte OPCODE_OFFSET = (byte) 0x01;
    public static byte LENGTH_OFFSET = (byte) 0x02;
    public static byte DATA_OFFSET = (byte) 0x07;

    public static byte[] generateCmd(byte opcode, String pin, byte[] data){
        try {
        /*
        Encrypted
         */
            byte[] bytesPinData = new byte[4 + data.length];
            for (int i = 0; i < pin.getBytes().length; i++) {
                bytesPinData[i] = pin.getBytes()[i];
            }
            for (int i = 0; i < data.length; i++) {
                bytesPinData[4 + i] = data[i];
            }
            byte[] bytesEncrypted = CommonUtils.encryptePin(bytesPinData);

        /*
        Calculate CRC
         */
            byte crc = CommonUtils.calculateCRC(bytesEncrypted, data);

        /*
        Add to result
         */
            byte lengthResult = (byte) (8 + data.length);
            byte[] result = new byte[lengthResult];

            result[0] = HEADER;
            result[1] = opcode;
            result[2] = (byte) (bytesEncrypted.length + data.length);
            result[3] = bytesEncrypted[3];
            result[4] = bytesEncrypted[2];
            result[5] = bytesEncrypted[1];
            result[6] = bytesEncrypted[0];
            for (int i = 0; i < data.length; i++) {
                result[7 + i] = data[i];
            }
            result[7 + data.length] = crc;
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static byte[] sendCMD1(String pin, byte value)
    {
        return generateCmd(OPCODE_CMD1,pin, new byte[]{value});
    }

    public static byte[] sendCMD2(String pin, byte value)
    {
        return generateCmd(OPCODE_CMD2,pin, new byte[]{value});
    }

    public static byte[] sendCMD3(String pin, byte value)
    {
        return generateCmd(OPCODE_CMD3,pin, new byte[]{value});
    }

    public static byte[] sendCMD4(String pin, byte value)
    {
        return generateCmd(OPCODE_CMD4,pin, new byte[]{value});
    }

    public static byte[] version(String pin)
    {
        return generateCmd(OPCODE_FWVER,pin, new byte[]{1});
    }

    public static byte[] readStatus(String pin)
    {
        return generateCmd(OPCODE_STATUS,pin, new byte[]{1});
    }

    public static byte[] logOn(String pin)
    {
        return generateCmd(OPCODE_LOGON,pin, new byte[]{1});
    }

    public static byte[] rename(String pin, String newName)
    {
        return generateCmd(OPCODE_NAME,pin,newName.getBytes());
    }

    public static byte[] changePINSMK(String pin, String pinsmk, String time)
    {
        String sData = pinsmk + time;
        return generateCmd(OPCODE_WRITE_SMARTKEY,pin,sData.getBytes());
    }

    public static byte[] readPINSMK(String pin)
    {
        return generateCmd(OPCODE_READ_SMARTKEY,pin,new byte[]{1});
    }

    public static byte[] changePin(String pin, String newPin)
    {
        //tripledes pin and newpin
        /*
        Use the encrypt TripleDES
        Key: oldpin*6
        Message: 8 byte newpin
        Mode: ECB
         */
        try {
            byte[] bytesTripleDes = encrypt(pin, newPin);
            return generateCmd(OPCODE_PIN, pin, bytesTripleDes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(String oldPin, String newPin) throws Exception {
        // Create an array to hold the key

        byte[] bytesNewPin = new byte[8];
        for (int i = 0; i < 4; i++)
        {
            bytesNewPin[i] = newPin.getBytes()[i];
        }

        byte[] bytesOldPin = new byte[24];
        for (int i = 0; i < 6; i++) {

            for (int i1 = 0; i1 < oldPin.getBytes().length; i1++) {
                bytesOldPin[i*4 + i1] = oldPin.getBytes()[i1];
            }

        }
        final SecretKey key = new SecretKeySpec(bytesOldPin, "DESede");
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final byte[] cipherText = cipher.doFinal(bytesNewPin);

        return Arrays.copyOfRange(cipherText,0,8);
    }

}
