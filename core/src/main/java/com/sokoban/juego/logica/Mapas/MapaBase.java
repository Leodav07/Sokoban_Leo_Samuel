package com.sokoban.juego.logica.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sokoban.juego.logica.Caja;
import com.sokoban.juego.logica.Colisiones;
import com.sokoban.juego.logica.Elemento;
import com.sokoban.juego.logica.Fondo;
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.JuegoUI;
import com.sokoban.juego.logica.Jugador;
import com.sokoban.juego.logica.Motor;
import com.sokoban.juego.logica.Muro;
import com.sokoban.juego.logica.Objetivo;
import com.sokoban.juego.logica.Partida;
import com.sokoban.juego.logica.Pausa.EstadoJuego;
import com.sokoban.juego.logica.Pausa.GestorDePausa;
import com.sokoban.juego.logica.Terreno;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.logica.Pausa.MenuPausaListener;
import com.sokoban.juego.logica.SoundManager;
import com.sokoban.juego.logica.accounts.ProgresoPorNivel;
import com.sokoban.juego.niveles.NivelSieteScreen;

public abstract class MapaBase implements MenuPausaListener, Motor.MotorListener {

    protected Elemento[][] mapa;
    protected int filas, columnas;
    protected Texture muroImg, cajaImg, metaImg, sueloImg, jugadorImg, fondoImg;
    protected Jugador jugador;
    protected Motor motorMovimiento;
    protected Thread hiloColisionador;
    protected Colisiones colisionador;
    protected int TILE;
    private static final int GAME_WORLD_WIDTH = 800;
    private static final int GAME_WORLD_HEIGHT = 480;
    private static final int UI_PANEL_HEIGHT = 120;
    protected Texture cajaEnObjetivoImg;
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
        void onNivelFinalizado();
    }

    protected abstract int[][] getLayout();

    protected abstract int getMovimientosObjetivo();

    protected abstract long getTiempoObjetivo();

    public MapaBase(int filas, int columnas, Texture muroImg, Texture cajaImg,
            Texture metaImg, Texture sueloImg, Texture jugadorImg, Texture cajaEnObjetivoImg, Texture fondoImg, int nivelId) {
        this.filas = filas;
        this.columnas = columnas;
        this.muroImg = muroImg;
        this.cajaImg = cajaImg;
        this.metaImg = metaImg;
        this.sueloImg = sueloImg;
        this.jugadorImg = jugadorImg;
        this.nivelId = nivelId;
        this.cajaEnObjetivoImg = cajaEnObjetivoImg;
        this.fondoImg = fondoImg;

        int availableHeight = GAME_WORLD_HEIGHT - UI_PANEL_HEIGHT;
        int maxTileWidth = GAME_WORLD_WIDTH / columnas;
        int maxTileHeight = availableHeight / filas;
        this.TILE = Math.min(maxTileWidth, maxTileHeight);

        mapa = new Elemento[filas][columnas];
        gameUI = new JuegoUI();
        gestorProgreso = GestorProgreso.getInstancia();
        gestorPausa = new GestorDePausa();
        gestorPausa.setMenuPausaListener(this);
        
      //  SoundManager.getInstance().playMusic(SoundManager.MusicTrack.NIVEL_TEMA, true);
    }

    public void setMapaListener(MapaBaseListener listener) {
        this.mapaListener = listener;
    }

    public void cargarMapa() {
        int[][] layout = getLayout();

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                switch (layout[y][x]) {
                    case 0:
                        mapa[y][x] = new Terreno(x, y, sueloImg);
                        break;
                    case 1:
                        mapa[y][x] = new Muro(x, y, muroImg);
                        break;
                    case 2:
                        Caja caja = new Caja(x, y, cajaImg, cajaEnObjetivoImg, TILE);
                        caja.setEstaEnObjetivo(false);
                        mapa[y][x] = caja;
                        break;
                    case 3:
                        mapa[y][x] = new Objetivo(x, y, metaImg);
                        break;
                    case 4:
                        mapa[y][x] = new Terreno(x, y, sueloImg);
                        jugador = new Jugador(jugadorImg, x, y, TILE);
                        break;

                    case 5:
                        mapa[y][x] = new Fondo(x, y, fondoImg);
                        break;
                }
            }
        }

        if (jugador != null) {
            motorMovimiento = new Motor(mapa, layout, jugador, filas, columnas,
                    cajaImg, metaImg, sueloImg, TILE);

            motorMovimiento.setCajaEnObjetivoTexture(cajaEnObjetivoImg);

            motorMovimiento.setListener(this);
            gameUI.iniciarNivel(nivelId);
        }
    }

    public void update(float delta) {
        if (!gestorPausa.estaPausado() && jugador != null) {
            jugador.update(delta);
        }

        gestorPausa.manejarInput();

        if (mostrandoResultados && !gestorPausa.estaPausado()) {
            if (System.currentTimeMillis() - tiempoMostrandoResultados > DURACION_RESULTADOS) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                        || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (gestorPausa.estaPausado()) {
                gestorPausa.reanudar();
            } else {
                gestorPausa.pausar();
            }
            return;
        }

        if (motorMovimiento == null || nivelCompletado
                || mostrandoResultados || gestorPausa.estaPausado()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (motorMovimiento.estaEjecutandoUndo()) {
                return;
            }

            if (motorMovimiento.puedeHacerUndo()) {
                boolean regresarSolicitado = motorMovimiento.realizarUndo(
                        gameUI.getMovimientosRealizados(),
                        gameUI.getScoreActual()
                );
                if (regresarSolicitado) {
                    System.out.println("Regresar solicitado - procesando en hilo separado");
                } else {
                    System.out.println("No se pudo solicitar Regresar");
                }
            } else {

            }
            return;
        }

        if (motorMovimiento.estaEjecutandoUndo()) {

            return;
        }

        boolean seMovio = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            jugador.cambiarDireccion(Jugador.DireccionMovimiento.ARRIBA);
            seMovio = motorMovimiento.moverJugador(0, 1,
                    gameUI.getMovimientosRealizados(), gameUI.getScoreActual());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jugador.cambiarDireccion(Jugador.DireccionMovimiento.ABAJO);
            seMovio = motorMovimiento.moverJugador(0, -1,
                    gameUI.getMovimientosRealizados(), gameUI.getScoreActual());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            jugador.cambiarDireccion(Jugador.DireccionMovimiento.IZQUIERDA);
            seMovio = motorMovimiento.moverJugador(-1, 0,
                    gameUI.getMovimientosRealizados(), gameUI.getScoreActual());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            jugador.cambiarDireccion(Jugador.DireccionMovimiento.DERECHA);
            seMovio = motorMovimiento.moverJugador(1, 0,
                    gameUI.getMovimientosRealizados(), gameUI.getScoreActual());
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

    @Override
    public void onMovimientoRealizado() {
    }

    @Override
    public void onNivelCompletado() {
        onNivelCompletadoInterno();
    }

    @Override
    public void onMovimientoInvalido() {
    }

    @Override
    public void onUndoRealizado() {
        if (gameUI != null) {
            gameUI.decrementarMovimientos();
        }
    }

    @Override
    public void onUndoIniciado() {
    }

   protected void onNivelCompletadoInterno() {
    SoundManager.getInstance().stopMusic();
    nivelCompletado = true;
    mostrandoResultados = true;
    tiempoMostrandoResultados = System.currentTimeMillis();

    int movimientos = gameUI.getMovimientosRealizados();
    long tiempoReal = gameUI.getTiempoTranscurridoReal(gestorPausa.getTiempoTotalPausado());
    
    ProgresoPorNivel progreso = gestorProgreso.getProgresoPorNivel(nivelId);
    int puntajeFinal = progreso.calcularPuntaje(movimientos);

    gestorProgreso.completarNivel(nivelId, movimientos, tiempoReal);
    
    GestorDatosPerfil.getInstancia().agregarHistorialPartida(new Partida(nivelId, puntajeFinal, movimientos, tiempoReal, "Completada"));
    
    gameUI.setResultadoFinal(puntajeFinal, movimientos, tiempoReal);

    onNivelCompletadoCustom();
}

    protected void onNivelCompletadoCustom() {
    }
    
    private void guardarPartidaIncompletaYSalir() {
    if (!nivelCompletado) {
        int movimientos = gameUI.getMovimientosRealizados();
        long tiempoReal = gameUI.getTiempoTranscurridoReal(gestorPausa.getTiempoTotalPausado());

        if (movimientos > 0 || tiempoReal > 1000) {
            gestorProgreso.registrarTiempoYMovimientos(tiempoReal, movimientos);

            Partida partidaAbandonada = new Partida(nivelId, 0, movimientos, tiempoReal, "Abandonada");
            GestorDatosPerfil.getInstancia().agregarHistorialPartida(partidaAbandonada);
        }
    }
    detenerColisiones();
    if (motorMovimiento != null) {
        motorMovimiento.finalizarSistemaUndo();
    }
}

    private void reiniciarNivel() {
        nivelCompletado = false;
        mostrandoResultados = false;
        gestorPausa.cambiarEstado(EstadoJuego.JUGANDO);
        gestorPausa.reiniciarTiempoPausa();

        if (motorMovimiento != null) {
            motorMovimiento.finalizarSistemaUndo();
        }

        mapa = new Elemento[filas][columnas];
        jugador = null;
        motorMovimiento = null;

        cargarMapa();

    }

    private void finalizarNivel() {
      if(mapaListener!=null){
         mapaListener.onNivelFinalizado();
      }
    }

    protected void onFinalizarNivelCustom() {
    }

    @Override
    public void onContinuar() {
        SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
        gestorPausa.reanudar();
    }

    @Override
    public void onReiniciarNivel() {
        gestorPausa.reanudar();
        reiniciarNivel();
    }

    @Override
    public void onVolverMenuPrincipal() {
        guardarPartidaIncompletaYSalir();
        if (mapaListener != null) {
            mapaListener.onVolverMenuPrincipal();
        }
    }

    @Override
    public void onSalirJuego() {
        guardarPartidaIncompletaYSalir();
        if (mapaListener != null) {
            mapaListener.onSalirJuego();
        } else {
            Gdx.app.exit();
        }
    }

    public void dibujar(SpriteBatch batch) {
        boolean pausaEstabaDibujada = gestorPausa.estaPausado();

        int mapPixelWidth = columnas * TILE;
        int mapPixelHeight = filas * TILE;

        float offsetX = (GAME_WORLD_WIDTH - mapPixelWidth) / 2f;

        float availableHeight = GAME_WORLD_HEIGHT - UI_PANEL_HEIGHT;
        float offsetY = UI_PANEL_HEIGHT + (availableHeight - mapPixelHeight) / 2f;

        if (offsetY + mapPixelHeight > GAME_WORLD_HEIGHT) {
            offsetY = GAME_WORLD_HEIGHT - mapPixelHeight;
        }

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (mapa[y][x] != null) {
                    float screenY = offsetY + (filas - 1 - y) * TILE;
                    float screenX = offsetX + x * TILE;

                    if (mapa[y][x] instanceof Elemento) {
                        batch.draw(((Elemento) mapa[y][x]).getTextura(), screenX, screenY, TILE, TILE);
                    }
                }
            }
        }

        if (jugador != null) {
            float jugadorScreenX = offsetX + jugador.getX() * TILE;
            float jugadorScreenY = offsetY + (filas - 1 - jugador.getY()) * TILE;

            if (jugador.estaMoviendose()) {
                float progreso = (jugador.getPosX() % TILE) / TILE;

                float deltaX = (jugador.getPosX() - jugador.getX() * TILE);
                float deltaY = (jugador.getPosY() - jugador.getY() * TILE);

                jugadorScreenX += deltaX;
                jugadorScreenY -= deltaY;
            }

            TextureRegion frameJugador = jugador.getTextureRegionActual();
            batch.draw(frameJugador, jugadorScreenX, jugadorScreenY, TILE, TILE);
        }
        
        if (gameUI != null && !mostrandoResultados) {
            gameUI.dibujar(batch, GAME_WORLD_WIDTH, GAME_WORLD_HEIGHT, gestorPausa.getTiempoTotalPausado());
        }

        // Dibujar resultado del nivel si está completado
        if (mostrandoResultados && gameUI != null) {
            // Primero terminar el batch actual
         
            // Mostrar resultado usando el stage interno
            gameUI.mostrarResultadoNivel(batch);
            // Reiniciar batch para continuar
            batch.end();
            batch.begin();
        }

        if (gestorPausa.estaPausado()) {
            gestorPausa.getMenuPausa().dibujar(batch);
            batch.getProjectionMatrix().setToOrtho2D(0, 0, GAME_WORLD_WIDTH, GAME_WORLD_HEIGHT);
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
    
    public void resize(int width, int height) {
    if (gameUI != null) {
        gameUI.resize(width, height);
    }
}

    public void dispose() {
        if (motorMovimiento != null) {
            motorMovimiento.finalizarSistemaUndo();
        }

        if (gameUI != null) {
            gameUI.dispose();
        }
        if (gestorPausa != null) {
            gestorPausa.dispose();
        }
    }
}