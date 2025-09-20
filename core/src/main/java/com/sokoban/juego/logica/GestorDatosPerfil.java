/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.sokoban.juego.logica.accounts.Usuario;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/**
 *
 * @author hnleo
 */
public class GestorDatosPerfil {
     private static final String DATOS_PERFIL_ARCHIVO = "perfil_extra.dat";
    private static final String USERS_BASE_DIR = "users";
    private static GestorDatosPerfil instancia;

    // Clase interna para contener los datos cargados
    public static class DatosPerfil {
        public String avatar;
        public long ultimaSesion;
        public List<Partida> historial;

        public DatosPerfil(String avatar, long ultimaSesion, List<Partida> historial) {
            this.avatar = avatar;
            this.ultimaSesion = ultimaSesion;
            this.historial = historial;
        }
    }
    
      private GestorDatosPerfil() {}

    public static GestorDatosPerfil getInstancia() {
        if (instancia == null) {
            instancia = new GestorDatosPerfil();
        }
        return instancia;
    }

    private File obtenerArchivoPerfil() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) return null;
        return new File(USERS_BASE_DIR + "/" + usuario.getUsername(), DATOS_PERFIL_ARCHIVO);
    }

    public DatosPerfil cargarDatosPerfil() {
        File archivo = obtenerArchivoPerfil();
        // Valores por defecto si no hay archivo
        if (archivo == null || !archivo.exists()) {
            return new DatosPerfil("default_avatar.png", 0, new ArrayList<>());
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            String avatar = raf.readUTF();
            long ultimaSesion = raf.readLong();
            int historialCount = raf.readInt();
            List<Partida> historial = new ArrayList<>();
            for (int i = 0; i < historialCount; i++) {
                historial.add(Partida.leerDeArchivo(raf));
            }
            return new DatosPerfil(avatar, ultimaSesion, historial);
        } catch (IOException e) {
            System.err.println("Error al cargar datos del perfil, usando defaults: " + e.getMessage());
            return new DatosPerfil("default_avatar.png", 0, new ArrayList<>());
        }
    }

    public void guardarDatosPerfil(DatosPerfil datos) {
        File archivo = obtenerArchivoPerfil();
        if (archivo == null) return;

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            raf.writeUTF(datos.avatar);
            raf.writeLong(datos.ultimaSesion);
            raf.writeInt(datos.historial.size());
            for (Partida p : datos.historial) {
                p.escribirEnArchivo(raf);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar datos del perfil: " + e.getMessage());
        }
    }

    public void guardarAvatar(String nombreAvatar) {
        DatosPerfil datos = cargarDatosPerfil();
        datos.avatar = nombreAvatar;
        guardarDatosPerfil(datos);
    }

    public void guardarUltimoLogin() {
        DatosPerfil datos = cargarDatosPerfil();
        datos.ultimaSesion = Calendar.getInstance().getTimeInMillis();
        guardarDatosPerfil(datos);
    }

    public void agregarHistorialPartida(Partida partida) {
        DatosPerfil datos = cargarDatosPerfil();
        datos.historial.add(0, partida); // Añade al principio para que sea el más reciente
        // Limitar el historial a, por ejemplo, las últimas 50 partidas
        while (datos.historial.size() > 50) {
            datos.historial.remove(datos.historial.size() - 1);
        }
        guardarDatosPerfil(datos);
    }
}
