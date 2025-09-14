/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.awt.Image;

/**
 *
 * @author hnleo
 */
public class MapaDos extends MapaBase {

    private final int[][] layout = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5},
        {1, 0, 0, 0, 0, 0, 0, 0, 1, 5, 5, 5, 5},
        {1, 0, 0, 2, 0, 0, 0, 2, 1, 5, 5, 5, 5},
        {1, 1, 1, 1, 0, 0, 0, 0, 1, 5, 5, 5, 5},
        {5, 5, 5, 1, 0, 2, 0, 0, 1, 1, 5, 5, 5},
        {1, 1, 1, 1, 0, 0, 0, 2, 0, 1, 5, 5, 5},
        {1, 3, 3, 0, 2, 0, 1, 1, 0, 1, 1, 1, 1},
        {1, 3, 3, 0, 0, 2, 1, 1, 0, 0, 0, 0, 1},
        {1, 3, 3, 0, 0, 0, 0, 2, 0, 4, 0, 0, 1},
        {1, 3, 1, 1, 1, 2, 1, 1, 1, 0, 1, 0, 1},
        {1, 3, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5}
    };

    private static final int MOVIMIENTOS_OBJETIVO = 300; // Movimientos para 3 estrellas
    private static final long TIEMPO_OBJETIVO = 180000; // 2 minutos en milisegundos

    public MapaDos(int filas, int columnas, Texture muroImg, Texture cajaImg,
            Texture metaImg, Texture sueloImg, Texture jugadorImg, Texture cajaObjetivo, Texture Fondo) {
        super(filas, columnas, muroImg, cajaImg, metaImg, sueloImg, jugadorImg, cajaObjetivo, Fondo, 1); // Nivel ID = 1
    }

    @Override
    protected int[][] getLayout() {
        return layout;
    }

    @Override
    protected int getMovimientosObjetivo() {
        return MOVIMIENTOS_OBJETIVO;
    }

    @Override
    protected long getTiempoObjetivo() {
        return TIEMPO_OBJETIVO;
    }

    @Override
    protected void onNivelCompletadoCustom() {
        System.out.println("¡Has completado el primer nivel del juego!");
        System.out.println("El nivel 2 ha sido desbloqueado.");

        //Aqui se podria agregar la musica de victoria //No olvidar la musica
    }

    @Override
    protected void onFinalizarNivelCustom() {
        // Comportamiento al finalizar el nivel 1
        System.out.println("Saliendo del nivel 1...");
    }

}
