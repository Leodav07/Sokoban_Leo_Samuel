/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

/**
 *
 * @author hnleo
 */
public class OrdenamientoRanking implements Comparable<OrdenamientoRanking> {

    private String username;
    private int score;

    public OrdenamientoRanking(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
    
    public void setScore(int score){
        this.score = score;
    }

    @Override
    public int compareTo(OrdenamientoRanking o) {
        return Integer.compare(o.score, this.score);
    }

}
