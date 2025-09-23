/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;
import com.badlogic.gdx.Input;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
/**
 *
 * @author hnleo
 */
public class GestorMapeo {
    private static final String CONTROLES_FILE_NAME = "controles.dat";
    private static final String USERS_BASE_DIR = "users";
    private static GestorMapeo instancia;

  
    public static int ARRIBA = Input.Keys.UP;
    public static int ABAJO = Input.Keys.DOWN;
    public static int IZQUIERDA = Input.Keys.LEFT;
    public static int DERECHA = Input.Keys.RIGHT;
    
    private GestorMapeo(){}
    
    public static GestorMapeo getInstancia(){
        if(instancia==null){
            instancia = new GestorMapeo();
            
        }
        return instancia;
    }
    
      private File obtenerArchivoControles() {
        if (GestorUsuarios.usuarioActual == null) {
            return null;
        }
        return new File(USERS_BASE_DIR + "/" + GestorUsuarios.usuarioActual.getUsername(), CONTROLES_FILE_NAME);
    }

    public void guardarControles(int arriba, int abajo, int izquierda, int derecha) {
        ARRIBA = arriba;
        ABAJO = abajo;
        IZQUIERDA = izquierda;
        DERECHA = derecha;

        File archivo = obtenerArchivoControles();
        if (archivo == null) return;

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            raf.setLength(0); 
            raf.writeInt(arriba);
            raf.writeInt(abajo);
            raf.writeInt(izquierda);
            raf.writeInt(derecha);
        } catch (IOException e) {
            System.err.println("Error al guardar los controles: " + e.getMessage());
        }
    }

    public void cargarControles() {
        File archivo = obtenerArchivoControles();
        if (archivo == null || !archivo.exists()) {
            resetToDefaults();
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            if (raf.length() < 16) { 
                resetToDefaults();
                return;
            }
            ARRIBA = raf.readInt();
            ABAJO = raf.readInt();
            IZQUIERDA = raf.readInt();
            DERECHA = raf.readInt();
        } catch (IOException e) {
            System.err.println("Error al cargar controles, usando defaults: " + e.getMessage());
            resetToDefaults();
        }
    }
    
    private void resetToDefaults() {
        ARRIBA = Input.Keys.UP;
        ABAJO = Input.Keys.DOWN;
        IZQUIERDA = Input.Keys.LEFT;
        DERECHA = Input.Keys.RIGHT;
    }
}
