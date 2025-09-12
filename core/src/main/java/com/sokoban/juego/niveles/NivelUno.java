/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.niveles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Mapas.MapaUno;

/**
 *
 * @author hnleo
 */
public class NivelUno extends ApplicationAdapter{
    private SpriteBatch batch;
    private Texture muro, caja, objetivo, suelo, jugador;
    private MapaUno mapa;
   
    @Override
    public void create (){
        batch = new SpriteBatch();
        muro = new Texture("muro.png");
        caja = new Texture("caja.png");
        objetivo = new Texture("objetivo.png");
        suelo = new Texture("suelo.png");
        jugador = new Texture("jugador.png");
        
        mapa = new MapaUno(10, 12, muro, caja, objetivo, suelo, jugador);
        mapa.cargarMapa();
        mapa.iniciarColisiones();
    }
    
    @Override
    public void render(){
        mapa.update(Gdx.graphics.getDeltaTime());
        batch.begin();
        mapa.dibujar(batch);
        batch.end();
    }
    
    @Override
    public void dispose(){
         batch.dispose();
        muro.dispose();
        caja.dispose();
        objetivo.dispose();
        suelo.dispose();
        jugador.dispose();
        mapa.detenerColisiones();
    }
}
