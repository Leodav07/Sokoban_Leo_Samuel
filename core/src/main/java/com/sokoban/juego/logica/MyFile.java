/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author hnleo
 */
public class MyFile {

    private File file = null;

    public void setFile(String user) { //Para crear el folder
        file = new File("users/" + user);
    }

    public void crearFolder() throws IOException {
        if (file.mkdirs()) {
            System.out.println("Se ha creado con éxito.");
        } else {
            System.out.println("No se pudo crear.");
        }
    }

    public static void main(String[] args) {
        MyFile mf = new MyFile();

        try {
            mf.setFile("hola");
            mf.crearFolder();

        } catch (IOException e) {
            System.out.println("Ocurrio un error en disco: " + e.getMessage());
        }
    }
}
