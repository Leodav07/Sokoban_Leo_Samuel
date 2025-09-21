/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author hnleo
 */
public class Partida {
     public int nivelId;
    public int puntaje;
    public int movimientos;
    public long tiempoMs;
    public long timestamp;
    public String estado;

    public Partida(int nivelId, int puntaje, int movimientos, long tiempoMs, String estado) {
        this.nivelId = nivelId;
        this.puntaje = puntaje;
        this.movimientos = movimientos;
        this.tiempoMs = tiempoMs;
        this.timestamp = System.currentTimeMillis();
        this.estado = estado;
    }

    private Partida() {} // Constructor para la carga desde archivo

    public String getFechaFormateada() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(timestamp));
    }

    public String getTiempoFormateado() {
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos %= 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public void escribirEnArchivo(RandomAccessFile raf) throws IOException {
        raf.writeInt(nivelId);
        raf.writeInt(puntaje);
        raf.writeInt(movimientos);
        raf.writeLong(tiempoMs);
        raf.writeLong(timestamp);
        raf.writeUTF(estado);
    }

    public static Partida leerDeArchivo(RandomAccessFile raf) throws IOException {
        Partida p = new Partida();
        p.nivelId = raf.readInt();
        p.puntaje = raf.readInt();
        p.movimientos = raf.readInt();
        p.tiempoMs = raf.readLong();
        p.timestamp = raf.readLong();
        try{
            p.estado = raf.readUTF();
        }catch(java.io.EOFException e){
            p.estado = "Completado";
        }
                 
        return p;
    }
}
