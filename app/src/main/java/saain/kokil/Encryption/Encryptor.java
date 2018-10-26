package saain.kokil.Encryption;

import android.util.Log;


import org.jivesoftware.smack.util.stringencoder.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Encryptor {

    private static final String LOGTAG="ENCRYPTOR";

    public static String encrypt(String key,String initVector,String text){
        try{
            IvParameterSpec ivParameterSpec=new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec secretKeySpec=new SecretKeySpec(key.getBytes("UTF-8"),"AES");

            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,ivParameterSpec);

            byte[] encrypted=cipher.doFinal(text.getBytes());
            Log.d(LOGTAG,"Text: "+text+" Encrypted Text: "+ Base64.encodeToString(encrypted));

            return Base64.encodeToString(encrypted);
        }
        catch (Exception e){
            Log.d(LOGTAG,"ERROR");
        }

        return null;
    }

    public static String decrypt(String key,String initVector,String message){
        try{
            IvParameterSpec ivParameterSpec=new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec secretKeySpec=new SecretKeySpec(key.getBytes("UTF-8"),"AES");

            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE,secretKeySpec,ivParameterSpec);

            byte[] msg=cipher.doFinal(Base64.decode(message));
            return new String(msg);

        }
        catch(Exception e){

        }

        return null;
    }
}
