package com.cau.visavau.security;

import com.cau.visavau.model.EncryptedResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;

import java.security.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

public class SecurityUtil {



    public static String getEncryptedPayload(KeyStore keyStore, String alias, String requestPayload, String keyId) throws KeyStoreException, NoSuchAlgorithmException, JOSEException {
        // Load the public key from the KeyStore
        Certificate cert = keyStore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();

        // Proceed with the encryption as before
        JWEHeader.Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)
                .keyID(keyId)
                .customParam("iat", System.currentTimeMillis()); // maybe
        JWEObject jweObject = new JWEObject(headerBuilder.build(), new Payload(requestPayload));

        // Encrypt using the loaded public key
        jweObject.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));

        return "{\"encData\":\"" + jweObject.serialize() + "\"}";
    }


    public static String getDecryptedPayload(KeyStore keyStore, String alias, String keyPassword, EncryptedResponse encryptedPayload) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, JOSEException, ParseException {
        // Load the private key from the KeyStore
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());

        // Parse the encrypted payload
        String response = encryptedPayload.getEncData();
        JWEObject jweObject = JWEObject.parse(response);

        // Decrypt using the loaded private key
        jweObject.decrypt(new RSADecrypter(privateKey));

        // Return the decrypted payload
        return jweObject.getPayload().toString();
    }

}
