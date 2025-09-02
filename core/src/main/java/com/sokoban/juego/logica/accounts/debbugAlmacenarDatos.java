/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author hnleo
 */
public class debbugAlmacenarDatos {

    public static void main(String[] args) throws FileNotFoundException {
//        Usuario user = new Usuario("leohn", "holacomoestas", "Leonardo Lopez");
//    
//    try{
//        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(user.getUser()+".dat"));
//        oos.writeObject(user);
//        System.out.println("user guardado exitosamente.");
//    }catch(Exception e){
//        System.out.println("Ocurrio un error al guardar "+e.getMessage());
//    }

        Usuario userLeido;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("leohn.dat"));
            userLeido = (Usuario) ois.readObject();

            System.out.println("¿Contraseña correcta: " + userLeido.verifyPass("holacomoestas"));
            System.out.println("¿Contraseña incorrecta: " + userLeido.verifyPass("otraClave"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
