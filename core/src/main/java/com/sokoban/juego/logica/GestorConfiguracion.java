package com.sokoban.juego.logica;

import com.sokoban.juego.logica.accounts.Usuario;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GestorConfiguracion {

    private static final String CONFIG_FILE_NAME = "config.dat";
    private static final String USERS_BASE_DIR = "users";
    private static GestorConfiguracion instancia;

    private GestorConfiguracion() {}

    public static GestorConfiguracion getInstancia() {
        if (instancia == null) {
            instancia = new GestorConfiguracion();
        }
        return instancia;
    }

    private File obtenerArchivoConfig() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) {
            return null;
        }
        return new File(USERS_BASE_DIR + "/" + usuario.getUsername(), CONFIG_FILE_NAME);
    }

    public void guardarConfiguracion(float volumen, String idioma) throws IOException {
        File archivo = obtenerArchivoConfig();
        if (archivo == null) {
            throw new IOException("No hay un usuario logueado para guardar la configuración.");
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            raf.setLength(0); // Limpia el archivo antes de escribir para evitar datos basura
            raf.writeFloat(volumen);
            raf.writeUTF(idioma);
        }
        System.out.println("Configuración guardada para " + GestorUsuarios.usuarioActual.getUsername());
    }

    /**
     * Carga la configuración del usuario.
     * @return Un array de Object donde: [0] es el volumen (Float) y [1] es el idioma (String).
     */
    public Object[] cargarConfiguracion() {
        File archivo = obtenerArchivoConfig();
        // Valores por defecto
        Object[] defaultConfig = new Object[]{0.5f, "es"}; 

        if (archivo == null || !archivo.exists()) {
            System.out.println("No se encontró archivo de configuración, usando valores por defecto.");
            return defaultConfig;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            if (raf.length() == 0) return defaultConfig; // Archivo vacío
            
            float volumen = raf.readFloat();
            String idioma = raf.readUTF();
            return new Object[]{volumen, idioma};
        } catch (IOException e) {
            System.err.println("Error al cargar la configuración, usando valores por defecto: " + e.getMessage());
            return defaultConfig;
        }
    }
}