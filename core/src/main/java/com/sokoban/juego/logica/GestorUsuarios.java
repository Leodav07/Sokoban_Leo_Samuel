package com.sokoban.juego.logica;

import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.logica.accounts.Usuario;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.regex.Pattern;

public class GestorUsuarios {

    private static final String USERS_BASE_DIR = "users";
    private static GestorUsuarios instancia;
    public static Usuario usuarioActual;

    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER_CASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
private static final Pattern VALID_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private GestorUsuarios() {
        // Asegura que el directorio base de usuarios exista
        File usersDir = new File(USERS_BASE_DIR);
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }
    }

    public static GestorUsuarios getInstancia() {
        if (instancia == null) {
            instancia = new GestorUsuarios();
        }
        return instancia;
    }

    public boolean registrarUsuario(String username, String password, String nombreCompleto) throws IOException {
        if (!esContraseñaValida(password)) {
            System.err.println("Intento de registro con contraseña inválida para el usuario: " + username);
            return false;
        }

        File carpetaUsuario = new File(USERS_BASE_DIR, username);
        if (carpetaUsuario.exists()) {
            System.out.println("El usuario ya existe: " + username);
            return false;
        }
        
        carpetaUsuario.mkdirs();

        File archivoUsuario = new File(carpetaUsuario, username + ".usr");

        try (RandomAccessFile raf = new RandomAccessFile(archivoUsuario, "rw")) {
            raf.writeUTF(username);
            raf.writeUTF(password);
            raf.writeUTF(nombreCompleto);
            raf.writeLong(Calendar.getInstance().getTimeInMillis());
        }
        
        GestorProgreso.getInstancia().inicializarNuevoUsuario(username);

        System.out.println("Usuario registrado exitosamente: " + username);
        return true;
    }
    
    public String loginUsuario(String username, String password) throws IOException {
    File archivoUsuario = new File(USERS_BASE_DIR + "/" + username, username + ".usr");

    if (!archivoUsuario.exists()) {
        System.out.println("Intento de login para usuario no existente: " + username);
        return null; 
    }

    try (RandomAccessFile raf = new RandomAccessFile(archivoUsuario, "r")) {
        String usuarioAlmacenado = raf.readUTF();
        String passwordAlmacenado = raf.readUTF();
        
        if (!usuarioAlmacenado.equals(username) || !passwordAlmacenado.equals(password)) {
            System.out.println("Usuario o contraseña incorrectos.");
            return null; 
        }

        String nombreCompleto = raf.readUTF();
        long fechaRegistroMillis = raf.readLong();
        
        usuarioActual = new Usuario(usuarioAlmacenado, passwordAlmacenado, nombreCompleto);
        Calendar fechaRegistro = Calendar.getInstance();
        fechaRegistro.setTimeInMillis(fechaRegistroMillis);
        usuarioActual.setFechaRegistro(fechaRegistro);

        System.out.println("Inicio de sesión exitoso para: " + username);
        
        GestorProgreso.getInstancia().cargarProgreso();
        GestorDatosPerfil.getInstancia().guardarUltimoLogin();

        Object[] config = GestorConfiguracion.getInstancia().cargarConfiguracion();
        String idiomaGuardado = (String) config[1];
        
        return idiomaGuardado; 
    }
}

    
    
    public String obtenerMensajeDeErrorContraseña(String password) {
        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (!UPPER_CASE_PATTERN.matcher(password).matches()) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!LOWER_CASE_PATTERN.matcher(password).matches()) {
            return "La contraseña debe contener al menos una letra minúscula.";
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            return "La contraseña debe contener al menos un dígito.";
        }
       
        return "";
    }

    public boolean esContraseñaValida(String password) {
        return VALID_PASSWORD_PATTERN.matcher(password).matches();
    }

    public static void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("Cerrando sesión para: " + usuarioActual.getUsername());
            GestorProgreso.getInstancia().guardarProgreso();
            usuarioActual = null;
        }
    }
}