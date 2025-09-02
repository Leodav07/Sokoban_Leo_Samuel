/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.sokoban.juego.logica.MyFile;
import com.sokoban.juego.logica.accounts.Usuario;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.InputMismatchException;

/**
 *
 * @author hnleo
 */
public class GestorUsuarios {

    private MyFile mf = new MyFile();
    private Usuario usuario;
    private ObjectOutputStream oos;
    private static GestorUsuarios instancia;
    private static Usuario usuarioActual;

    public boolean registrarUsuario(String username, String password, String nombreCompleto) {

        try {

            if (!existeUsuario(username)) {
                mf.setFile(username);
                mf.crearFolder();
                usuario = new Usuario(username, password, nombreCompleto);
                oos = new ObjectOutputStream(new FileOutputStream("users" + "/" + username + "/" + username + ".dat"));
                oos.writeObject(usuario);
                System.out.println("Usuario creado correctamente.");
                return true;
            } else {
                System.out.println("Nombre de usuario ya existe.");

            }

        } catch (IOException io) {
            System.out.println("Ocurrio un error en disco: " + io.getMessage());
        } catch (NullPointerException nu) {
            System.out.println("Objeto File vacío. " + nu.getMessage());
        } catch (InputMismatchException in) {
            System.out.println("Dato no compatible. " + in.getMessage());
        }
        return false;
    }

    public boolean loginUsuario(String username, String password) {
        Usuario userLeido;
        try {
            if (existeUsuario(username)) {

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users" + "/" + username + "/" + username + ".dat"));
                userLeido = (Usuario) ois.readObject();
                userLeido.verifyPass(password);
                return true;

            }
        } catch (Exception e) {
            System.out.println("Ocurrio un error: " + e.getMessage());
        }

        return false;
    }

    public boolean existeUsuario(String username) {
        File usuarioCarpeta = new File("users/" + username);
        return usuarioCarpeta.exists() && usuarioCarpeta.isDirectory();
    }

    public static GestorUsuarios getInstancia() {
        if (instancia == null) {
            instancia = new GestorUsuarios();
        }
        return instancia;
    }

    public static void main(String[] args) {
        GestorUsuarios gs = new GestorUsuarios();
        if (gs.loginUsuario("popo", "holapopo")) {
            System.out.println("Correcto inicio de sesion");

        } else {
            System.out.println("Ocurrio un error");
        }
    }

}
