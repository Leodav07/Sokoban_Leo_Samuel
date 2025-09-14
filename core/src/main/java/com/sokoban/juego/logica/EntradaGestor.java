///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.sokoban.juego.logica;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Input;
///**
// *
// * @author hnleo
// */
//public class EntradaGestor {
//     private JugadorExample jugador;
//    private float tiempoUltimoMovimiento = 0f;
//    private static final float DELAY = 0.08f; 
//   
//    public void update(float deltaTime) {
//        tiempoUltimoMovimiento += deltaTime;
//        
//        if (tiempoUltimoMovimiento < DELAY) {
//            return;
//        }
//        
//        boolean seMovio = false;
//        
//        if ( Gdx.input.isKeyPressed(Input.Keys.W)) {
//            
//        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
//            
//        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//            
//        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
//           
//        }
//        
//        if (seMovio) {
//            tiempoUltimoMovimiento = 0f;
//            jugador.setEstaMoviendo(true);
//        } else {
//            jugador.setEstaMoviendo(false);
//        }
//    }
// 
//}
