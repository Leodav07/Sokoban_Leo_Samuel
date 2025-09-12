package com.sokoban.juego.logica.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sokoban.juego.logica.Caja;
import com.sokoban.juego.logica.Colisiones;
import com.sokoban.juego.logica.Elemento;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.JuegoUI;
import com.sokoban.juego.logica.Jugador;
import com.sokoban.juego.logica.Motor;
import com.sokoban.juego.logica.Muro;
import com.sokoban.juego.logica.Objetivo;
import com.sokoban.juego.logica.Pausa.EstadoJuego;
import com.sokoban.juego.logica.Pausa.GestorDePausa;
import com.sokoban.juego.logica.Terreno;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.logica.Pausa.MenuPausaListener;

/**
 *
 * @author hnleo
 */

public abstract class MapaBase implements MenuPausaListener{
    protected Elemento[][] mapa;
    protected int filas, columnas;
    protected Texture muroImg, cajaImg, metaImg, sueloImg, jugadorImg;
    protected Jugador jugador;
    protected Motor motorMovimiento;
    protected Thread hiloColisionador;
    protected Colisiones colisionador;
    protected final int TILE = 64;
    
    protected JuegoUI gameUI;
    protected GestorProgreso gestorProgreso;
    protected GestorDePausa gestorPausa;
    protected int nivelId;
    protected boolean nivelCompletado = false;
    protected boolean mostrandoResultados = false;
    protected long tiempoMostrandoResultados = 0;
    protected final long DURACION_RESULTADOS = 3000;
    
    protected MapaBaseListener mapaListener;
    
    public interface MapaBaseListener {
        void onVolverMenuPrincipal();
        void onSalirJuego();
        void onReiniciarNivel();
    }
    
    protected abstract int[][] getLayout();
    protected abstract int getMovimientosObjetivo();
    protected abstract long getTiempoObjetivo();
    
    public MapaBase(int filas, int columnas, Texture muroImg, Texture cajaImg, 
                   Texture metaImg, Texture sueloImg, Texture jugadorImg, int nivelId) {
        this.filas = filas;
        this.columnas = columnas;
        this.muroImg = muroImg;
        this.cajaImg = cajaImg;
        this.metaImg = metaImg;
        this.sueloImg = sueloImg;
        this.jugadorImg = jugadorImg;
        this.nivelId = nivelId;
        
        mapa = new Elemento[filas][columnas];
        gameUI = new JuegoUI();
        gestorProgreso = GestorProgreso.getInstancia();
        gestorPausa = new GestorDePausa(); 
        gestorPausa.setMenuPausaListener(this); 
    }
    
    public void setMapaListener(MapaBaseListener listener) {
        this.mapaListener = listener;
    }
    
    public void cargarMapa() {
        int[][] layout = getLayout();
        
        new Thread(() -> {
            for (int y = 0; y < filas; y++) {
                for (int x = 0; x < columnas; x++) {
                    switch (layout[y][x]) {
                        case 0: mapa[y][x] = new Terreno(x, y, sueloImg); break;
                        case 1: mapa[y][x] = new Muro(x, y, muroImg); break;
                        case 2: mapa[y][x] = new Caja(x, y, cajaImg); break;
                        case 3: mapa[y][x] = new Objetivo(x, y, metaImg); break;
                        case 4:
                            mapa[y][x] = new Terreno(x, y, sueloImg);
                            jugador = new Jugador(x, y, jugadorImg);
                            break;
                    }
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            if (jugador != null) {
                motorMovimiento = new Motor(mapa, layout, jugador, filas, columnas, 
                                          cajaImg, metaImg, sueloImg);
                gameUI.iniciarNivel(nivelId, getMovimientosObjetivo(), getTiempoObjetivo());
            }
        }).start();
    }
    
    public void update(float delta) {
        if (!gestorPausa.estaPausado() && jugador != null) {
            jugador.update(delta);
        }
        
        gestorPausa.manejarInput();
        
        if (mostrandoResultados && !gestorPausa.estaPausado()) {
            if (System.currentTimeMillis() - tiempoMostrandoResultados > DURACION_RESULTADOS) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || 
                    Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    finalizarNivel();
                }
            }
        }
    }
    
    public void iniciarColisiones() {
        colisionador = new Colisiones(this);
        hiloColisionador = new Thread(colisionador);
        hiloColisionador.start();
    }
    
    public void detenerColisiones() {
        if (colisionador != null) {
            colisionador.detener();
        }
    }
    
    public void verificarTeclas() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (gestorPausa.estaPausado()) {
                gestorPausa.reanudar();
            } else {
                gestorPausa.pausar();
            }
            return;
        }
        
        if (motorMovimiento == null || nivelCompletado || 
            mostrandoResultados || gestorPausa.estaPausado()) {
            return;
        }
        
        boolean seMovio = false;
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            seMovio = motorMovimiento.moverJugador(0, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            seMovio = motorMovimiento.moverJugador(0, -1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            seMovio = motorMovimiento.moverJugador(-1, 0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            seMovio = motorMovimiento.moverJugador(1, 0);
        }
        
        if (seMovio && gameUI != null) {
            gameUI.incrementarMovimientos();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reiniciarNivel();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            gestorProgreso.mostrarEstadisticas();
        }
    }
    
    protected void onNivelCompletado() {
        nivelCompletado = true;
        mostrandoResultados = true;
        tiempoMostrandoResultados = System.currentTimeMillis();
        
        int movimientos = gameUI.getMovimientosRealizados();
        long tiempoReal = gameUI.getTiempoTranscurrido() - gestorPausa.getTiempoTotalPausado();
        
        gestorProgreso.completarNivel(nivelId, movimientos, tiempoReal);
        
        System.out.println("¡Nivel " + nivelId + " completado!");
        System.out.println("Estrellas obtenidas: " + gameUI.calcularEstrellas());
        System.out.println("Score: " + gameUI.getScoreActual());
        
        onNivelCompletadoCustom();
    }
    
    protected void onNivelCompletadoCustom() {
    }
    
    private void reiniciarNivel() {
        nivelCompletado = false;
        mostrandoResultados = false;
        gestorPausa.cambiarEstado(EstadoJuego.JUGANDO);
        gestorPausa.reiniciarTiempoPausa();
        
        mapa = new Elemento[filas][columnas];
        jugador = null;
        motorMovimiento = null;
        
        cargarMapa();
        System.out.println("Nivel reiniciado");
    }
    
    private void finalizarNivel() {
        System.out.println("Finalizando nivel " + nivelId);
        
        int siguienteNivel = nivelId + 1;
        if (siguienteNivel <= 7 && gestorProgreso.isNivelDesbloqueado(siguienteNivel)) {
            System.out.println("Cargando nivel " + siguienteNivel + "...");
        } else {
            System.out.println("Regresando al menú principal...");
        }
        
        onFinalizarNivelCustom();
    }
    
    protected void onFinalizarNivelCustom() {
        // Implementación por defecto vacía
    }
    
    @Override
    public void onContinuar() {
        gestorPausa.reanudar();
    }
    
    @Override
    public void onReiniciarNivel() {
        gestorPausa.reanudar();
        reiniciarNivel();
    }
    
    @Override
    public void onVolverMenuPrincipal() {
        detenerColisiones();
        if (mapaListener != null) {
            mapaListener.onVolverMenuPrincipal();
            
        }
    }
    
    @Override
    public void onSalirJuego() {
        detenerColisiones();
        if (mapaListener != null) {
            mapaListener.onSalirJuego();
        } else {
            Gdx.app.exit();
        }
    }
    
    public void dibujar(SpriteBatch batch) {
        int anchoMapa = columnas * TILE;
        int altoMapa = filas * TILE;
        int uiHeight = 120;
        int offsetX = (Gdx.graphics.getWidth() - anchoMapa) / 2;
        int offsetY = (Gdx.graphics.getHeight() - altoMapa - uiHeight) / 2;

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (mapa[y][x] != null) {
                    mapa[y][x].dibujar(batch, TILE, offsetX, offsetY, filas);
                }
            }
        }

        if (jugador != null) {
            jugador.dibujar(batch, TILE, offsetX, offsetY, filas);
        }
        
        if (gameUI != null && !mostrandoResultados) {
            gameUI.dibujar(batch, anchoMapa, altoMapa, gestorPausa.getTiempoTotalPausado());
        }
        
        if (mostrandoResultados && gameUI != null) {
            gameUI.mostrarResultadoNivel(batch);
        }
        
        if (gestorPausa.estaPausado()) {
            gestorPausa.getMenuPausa().dibujar(batch);
        }
    }
    
    public int getNivelId() { 
        return nivelId; 
    }
    public boolean isNivelCompletado() { 
        return nivelCompletado; 
    }
    public boolean isMostrandoResultados() {
        return mostrandoResultados; 
    }
    public JuegoUI getGameUI() {
        return gameUI;
    }
    public GestorDePausa getGestorPausa() { 
        return gestorPausa; 
    }
    
    public void dispose() {
        if (gameUI != null) {
            gameUI.dispose();
        }
        if (gestorPausa != null) {
            gestorPausa.dispose();
        }
    }
}