/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

import java.io.Serializable;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author hnleo
 */
public class PasswordSecure implements Serializable {

    protected String saltBase64;

    protected byte[] generarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;

    }

    protected String passwordHashed(String password, byte[] salt) throws InvalidKeySpecException, Exception {
        int itr = 65536;
        int keyL = 128;
        PBEKeySpec sp = new PBEKeySpec(password.toCharArray(), salt, itr, keyL);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = skf.generateSecret(sp).getEncoded();
        return Base64.getEncoder().encodeToString(hash);

    }
 

}
