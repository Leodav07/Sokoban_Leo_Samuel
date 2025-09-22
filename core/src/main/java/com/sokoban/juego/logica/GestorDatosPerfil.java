package com.sokoban.juego.logica;

import com.sokoban.juego.logica.accounts.Usuario;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GestorDatosPerfil {
    private static final String DATOS_PERFIL_ARCHIVO = "perfil_extra.dat";
    private static final String USERS_BASE_DIR = "users";
    private static GestorDatosPerfil instancia;

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

    // <<-- MÉTODO MODIFICADO para aceptar un nombre de usuario -->>
    private File obtenerArchivoPerfil(String username) {
        if (username == null || username.isEmpty()) return null;
        return new File(USERS_BASE_DIR + "/" + username, DATOS_PERFIL_ARCHIVO);
    }
    
    private File obtenerArchivoPerfil() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) return null;
        return obtenerArchivoPerfil(usuario.getUsername());
    }

    // <<-- NUEVO MÉTODO para cargar solo el avatar de un usuario específico -->>
    public String cargarAvatarDeUsuario(String username) {
        File archivo = obtenerArchivoPerfil(username);
        if (archivo == null || !archivo.exists()) {
            return "default_avatar.png"; // Avatar por defecto si el usuario no tiene perfil
        }
    
        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            // El primer dato guardado en el archivo es el nombre del avatar
            return raf.readUTF();
        } catch (IOException e) {
            System.err.println("Error al cargar avatar para " + username + ", usando default: " + e.getMessage());
            return "default_avatar.png";
        }
    }

    public DatosPerfil cargarDatosPerfil() {
        File archivo = obtenerArchivoPerfil();
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
        datos.historial.add(0, partida);
        while (datos.historial.size() > 50) {
            datos.historial.remove(datos.historial.size() - 1);
        }
        guardarDatosPerfil(datos);
    }
}