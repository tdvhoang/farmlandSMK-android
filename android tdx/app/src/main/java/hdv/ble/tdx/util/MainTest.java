package hdv.ble.tdx.util;

import hdv.ble.tdx.data.Protocol;

/**
 * Created by Ann on 2/24/16.
 */
public class MainTest {
    public static void main(String[] args){
        byte[] bytes = new byte[8];
        bytes[0] = 0x33;
        bytes[1] = 0x33;
        bytes[2] = 0x33;
        bytes[3] = 0x33;
        bytes[4] = 0;
        bytes[5] = 0;
        bytes[6] = 0;
        bytes[7] = 0;
        //b0 dc 3a 21 64 de 49 73

//        try {
//            System.out.println(CommonUtils.convertByteToString(Protocol.encrypt("1234","5678")));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(CommonUtils.convertByteToString(CommonUtils.encryptePin(bytes)));
//        System.out.println(CommonUtils.convertByteToString(Protocol.findbyke("8888")));
//        System.out.println(CommonUtils.convertByteToString(Protocol.setLockOff("8888")));
//        System.out.println(CommonUtils.convertByteToString(Protocol.setLockOn("8888")));
//        System.out.println(CommonUtils.convertByteToString(Protocol.setVibrateOn("8888")));
//        System.out.println(CommonUtils.convertByteToString(Protocol.setVibrateOff("8888")));
//        System.out.println(CommonUtils.convertByteToString(Protocol.readStatus("8888")));
        System.out.println(CommonUtils.convertByteToString(Protocol.changePin("8888","3333")));
        ;
        testVersion();
//        161D33827F06197BD2597AF789EEBC87

    }

    public static void testVersion(){
        byte[] bytes = {(byte) 0xCA, (byte) 0x8F,0x05 ,0x31 ,0x2E, 0x30, 0x2E ,0x38, (byte) 0xF5};
        if(bytes.length > 5 ){
            byte[] version = new byte[bytes.length - 4];
            for (int i = 3; i < bytes.length - 1; i++) {
                version[i-3] = bytes[i];

            }
            String sVersion = new String(version);
            System.out.print(sVersion);

        }
    }
}
