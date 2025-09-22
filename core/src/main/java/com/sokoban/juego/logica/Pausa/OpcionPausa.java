/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Pausa;

import com.sokoban.juego.Main;
import static com.sokoban.juego.logica.Pausa.OpcionPausa.values;

/**
 *
 * @author hnleo
 */
public enum OpcionPausa {
    
        CONTINUAR(MenuPausa.game.bundle.get("enu.continuar"), 0),
    REINICIAR(MenuPausa.game.bundle.get("enu.reiniciar"), 1),
    MENU_PRINCIPAL(MenuPausa.game.bundle.get("enu.menu"), 2),
    SALIR_JUEGO(MenuPausa.game.bundle.get("enu.salir"), 3);
    
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
