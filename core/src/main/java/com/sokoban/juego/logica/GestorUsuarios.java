package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sokoban.juego.logica.accounts.Usuario;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;


public class GestorUsuarios {

    private static GestorUsuarios instancia;
    public static Usuario usuarioActual;

    public GestorUsuarios() {
        FileHandle usersFolder = Gdx.files.local("users");
        if (!usersFolder.exists()) {
            usersFolder.mkdirs();
        }
    }

   
    public boolean registrarUsuarios(String username, String password, String nombreCompleto)
            throws IOException, NoSuchAlgorithmException {

        FileHandle carpetaUsuario = Gdx.files.local("users/" + username);
        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdirs(); 

            FileHandle fileHandle = carpetaUsuario.child(username + ".usr");
            File file = fileHandle.file(); 

            
            try (RandomAccessFile fileUser = new RandomAccessFile(file, "rw")) {
                fileUser.writeUTF(username);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
                String passwordHash = Base64.getEncoder().encodeToString(hash);

                fileUser.writeUTF(passwordHash);
                fileUser.writeUTF(nombreCompleto);
                fileUser.writeLong(Calendar.getInstance().getTimeInMillis());
            } 

            return true;
        } else {
            System.out.println("Ya existe usuario: " + username);
            return false;
        }
    }

    
    public boolean loginUsuario(String username, String password)
            throws IOException, NoSuchAlgorithmException {
        RandomAccessFile fileUser = null; 
        try {
            FileHandle archivoUser = Gdx.files.local("users/" + username + "/" + username + ".usr");
            if (!archivoUser.exists()) {
                System.out.println("Usuario no existe: " + username);
                return false;
            }

            File file = archivoUser.file();
            fileUser = new RandomAccessFile(file, "r"); // Asigna la instancia aquí

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            String passwordHash = Base64.getEncoder().encodeToString(hash);

            String usuarioAlmacenado = fileUser.readUTF();
            String passwordAlmacenado = fileUser.readUTF();
            String nombreCompletoAlmacenado = fileUser.readUTF();
            long fechaRegistro = fileUser.readLong();

            if (usuarioAlmacenado.equals(username) && passwordAlmacenado.equals(passwordHash)) {
                System.out.println("Inicio de sesión exitoso: " + username);
                // Almacena el usuario actual para usarlo en otras pantallas
                usuarioActual = new Usuario(usuarioAlmacenado, password, nombreCompletoAlmacenado);
                // Puedes establecer la fecha de registro si es necesaria en el objeto Usuario
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(fechaRegistro);
                usuarioActual.setFechaRegistro(cal);
                return true;
            } else {
                System.out.println("Usuario o contraseña incorrectos.");
                return false;
            }
        } finally {
            if (fileUser != null) {
                fileUser.close();
            }
        }
    }

    
    public static void cerrarSesion() {
        usuarioActual = null;
        System.out.println("Sesión cerrada.");
    }

   
    public static GestorUsuarios getInstancia() {
        if (instancia == null) {
            instancia = new GestorUsuarios();
        }
        return instancia;
    }
}