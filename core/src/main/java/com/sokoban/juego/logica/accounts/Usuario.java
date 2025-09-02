/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

import java.io.Serializable;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Calendar;

/**
 *
 * @author hnleo
 */
public class Usuario extends PasswordSecure implements Serializable {

    private String username;
    private String password;
    private String nombreCompleto;
    private Calendar fechaRegistro;

    public Usuario(String username, String password, String nombreCompleto) {
        this.username = username;

        byte[] salt = generarSalt();
        super.saltBase64 = Base64.getEncoder().encodeToString(salt);
        try {
            this.password = passwordHashed(password, salt);
        } catch (InvalidKeySpecException e) {
            System.out.println("Key inválida");
        } catch (Exception i) {
            System.out.println("Ocurrio un error: " + i.getMessage());
        }
        this.nombreCompleto = nombreCompleto;
        this.fechaRegistro = Calendar.getInstance();
    }

    public boolean verifyPass(String password) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        String hashIt = passwordHashed(password, salt);
        return hashIt.equals(this.password);
    }
    
    public String getUser(){
        return username;
    }
}
