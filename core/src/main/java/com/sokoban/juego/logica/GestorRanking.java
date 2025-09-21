/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 * @author hnleo
 */
public class GestorRanking {
    private static GestorRanking instancia;

    private GestorRanking() {
    }

    public static GestorRanking getInstancia() {
        if (instancia == null) {
            instancia = new GestorRanking();
        }
        return instancia;
    }

    public synchronized List<OrdenamientoRanking> leerRanking() {
        List<OrdenamientoRanking> ranking = new ArrayList<>();
        File archivo = new File("ranking.dat");

        if (!archivo.exists()) {
            return ranking; 
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            int numEntries = raf.readInt();
            for (int i = 0; i < numEntries; i++) {
                String username = raf.readUTF();
                int score = raf.readInt();
                ranking.add(new OrdenamientoRanking(username, score));
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de ranking: " + e.getMessage());
        }
        return ranking;
    }

    private synchronized void escribirRanking(List<OrdenamientoRanking> ranking) {
        File archivo = new File("ranking.dat");
        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            raf.setLength(0);
            raf.writeInt(ranking.size());
            for (OrdenamientoRanking orden : ranking) {
                raf.writeUTF(orden.getUsername());
                raf.writeInt(orden.getScore());
            }
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de ranking: " + e.getMessage());
        }
    }

    public synchronized void actualizarOrdenamiento(String username, int score) {
        List<OrdenamientoRanking> ranking = leerRanking();
        boolean jugadorEncontrado = false;

        for (OrdenamientoRanking orden : ranking) {
            if (orden.getUsername().equals(username)) {
                if (score > orden.getScore()) {
                    orden.setScore(score);
                }
                jugadorEncontrado = true;
                break;
            }
        }

        if (!jugadorEncontrado) {
            ranking.add(new OrdenamientoRanking(username, score));
        }

        Collections.sort(ranking);
        escribirRanking(ranking);
    }
}
