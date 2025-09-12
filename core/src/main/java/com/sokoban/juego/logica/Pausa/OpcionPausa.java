/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Pausa;

/**
 *
 * @author hnleo
 */
public enum OpcionPausa {
        CONTINUAR("Continuar", 0),
    REINICIAR("Reiniciar Nivel", 1),
    MENU_PRINCIPAL("Menú Principal", 2),
    SALIR_JUEGO("Salir del Juego", 3);
    
    private final String texto;
    private final int indice;
    
    OpcionPausa(String texto, int indice) {
        this.texto = texto;
        this.indice = indice;
    }
    
    public String getTexto() {
        return texto; 
    }
    public int getIndice() { 
        return indice; 
    }
    
    public static OpcionPausa getOpcionPorIndice(int indice) {
        for (OpcionPausa opcion : values()) {
            if (opcion.indice == indice) {
                return opcion;
            }
        }
        return CONTINUAR;
    }
    
    public static int getTotalOpciones() {
        return values().length;
    }
}
