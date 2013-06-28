/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.MDNSDiscovery;

import freenet.config.Config;
import freenet.config.InvalidConfigValueException;
import freenet.config.NodeNeedRestartException;
import freenet.config.SubConfig;
import freenet.support.api.StringArrCallback;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
/**
 *
 * @author Chinnu
 */
public class DigitalSignature {
    private static PublicKey publickey;
    private static PrivateKey privatekey;
    private static boolean generated = false;
    private static Properties prop = new Properties();    
    
    public static byte[] getPublicKey()  {
        byte[] key = null;
        if (!generated) {
            initialize();
        }
        if (generated) key = publickey.getEncoded();
        return key;
    }
    
    public static byte[] getSignature(String text)  {
        byte[] signature = null;
        if (!generated) {
            initialize();
        }
        if (generated){
                try { 
                    Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
                    dsa.initSign(privatekey);
                    byte[] buf = text.getBytes("UTF-8");
                    dsa.update(buf, 0, buf.length);
                    signature = dsa.sign();
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchProviderException ex) {
                    Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SignatureException ex) {
                    Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        return signature;
    }
    public static void initialize() {
        try {
            File file = new File("DSAconfig.properties");
            if (!file.exists()) {
                file.createNewFile();
                
                prop.load(new FileInputStream(file));
                generateProperties();
            }
            else if (!generated) {
                prop.load(new FileInputStream(file));
                generateProperties();
            }
            else {
                prop.load(new FileInputStream(file));
                pullProperties();
            }
        }
        catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void generateProperties() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();
        privatekey = pair.getPrivate();
        publickey = pair.getPublic();
        BASE64Encoder encoder = new BASE64Encoder();
        String pri = encoder.encode(privatekey.getEncoded());
        String pub = encoder.encode(publickey.getEncoded());
        System.out.println("here"+"  " +  pri+"  "+pub);
        prop.setProperty("DSAprivatekey",pri);
        prop.setProperty("DSApublickey",pub);
        generated = true;
        prop.store(new FileOutputStream("DSAconfig.properties"), null);
        
    }
    private static void pullProperties() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {

        String priv = prop.getProperty("DSAprivatekey");
        String publ = prop.getProperty("DSApublickey");
        if (priv.length()==0) System.out.println("zero");
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] pri = decoder.decodeBuffer(priv);
        byte[] pub = decoder.decodeBuffer(publ);
        X509EncodedKeySpec priKeySpec = new X509EncodedKeySpec(pri);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pub);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
        privatekey =keyFactory.generatePrivate(priKeySpec);
        publickey =keyFactory.generatePublic(pubKeySpec);
        generated = true;
        System.out.println("hero");
    }
    public static boolean verify(String data,byte[] signature,byte[] publicKey) {
        boolean verify = false;
        try {
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            byte[] buf = data.getBytes("UTF-8");
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pubKey);
            sig.update(buf, 0,buf.length);
            verify = sig.verify(signature);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DigitalSignature.class.getName()).log(Level.SEVERE, null, ex);
        }
        return verify;
    }

    
}
