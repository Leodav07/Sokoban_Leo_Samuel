package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

public class Jugador {

     // --- Variables de Lógica ---
    private int x, y; // Posición en la cuadrícula (grid)
    private int tileSize;

    // --- Variables de Animación y Movimiento Suave ---
    private boolean moviendose = false;
    private final Vector2 posVisual = new Vector2();    // Posición actual en píxeles para el dibujado
    private final Vector2 posAnterior = new Vector2();  // Desde dónde empieza a moverse
    private final Vector2 posDestino = new Vector2();   // Hacia dónde se mueve
    private float moveTimer = 0f;
    private final float MOVE_DURATION = 0.2f; // <-- ¡Puedes cambiar este valor para hacerlo más rápido o lento!
    private Animation<TextureRegion> animacionActual;
    private Animation<TextureRegion> idleAbajo, idleArriba, idleIzquierda, idleDerecha;
    private Animation<TextureRegion> caminarAbajo, caminarArriba, caminarIzquierda, caminarDerecha;
    private Animation<TextureRegion> empujarAbajo, empujarArriba, empujarIzquierda, empujarDerecha;

    private float tiempoAnimacion = 0f;
    private DireccionMovimiento direccionActual = DireccionMovimiento.ABAJO;
    private EstadoAnimacion estadoActual = EstadoAnimacion.IDLE;

    public enum DireccionMovimiento {
        ABAJO, ARRIBA, IZQUIERDA, DERECHA
    }

    public enum EstadoAnimacion {
        IDLE, CAMINANDO, EMPUJANDO
    }
    public Jugador(int x, int y, Texture jugadorImg, int tileSize) {
         this.x = x;
        this.y = y;
        this.tileSize = tileSize;

        // Posición inicial (visual y lógica son la misma)
        this.posVisual.set(x * tileSize, y * tileSize);
        this.posAnterior.set(posVisual);
        this.posDestino.set(posVisual);

        configurarAnimaciones(jugadorImg);
        animacionActual = idleAbajo;
    }

    public Jugador(Texture spritesheet, int x, int y, int tileSize) {
        this(x, y, spritesheet, tileSize);
    }

   private void configurarAnimaciones(Texture spritesheet) {
        // Analizando tu imagen, cada frame es de 16px de ancho y 24px de alto.
        int FRAME_WIDTH = 32;
        int FRAME_HEIGHT = 32;
        float DURACION_FRAME = 0.15f; // Animación más rápida y fluida

        TextureRegion[][] tmp = TextureRegion.split(spritesheet, FRAME_WIDTH, FRAME_HEIGHT);

        // Convertimos el array 2D a uno 1D para facilitar el acceso
        TextureRegion[] frames = new TextureRegion[tmp[0].length];
        for (int i = 0; i < tmp[0].length; i++) {
            frames[i] = tmp[0][i];
        }

        // --- Definición de animaciones según tu spritesheet ---
        // Mirando a la izquierda (IDLE)
        idleIzquierda = new Animation<>(DURACION_FRAME, frames[0]);
        // Mirando a la derecha (IDLE)
        idleDerecha = new Animation<>(DURACION_FRAME, frames[1]);
        // Caminando a la izquierda
        caminarIzquierda = new Animation<>(DURACION_FRAME, frames[0], frames[2], frames[0], frames[2]);
        caminarIzquierda.setPlayMode(Animation.PlayMode.LOOP);
        // Caminando a la derecha
        caminarDerecha = new Animation<>(DURACION_FRAME, frames[1], frames[3], frames[1], frames[3]);
        caminarDerecha.setPlayMode(Animation.PlayMode.LOOP);
        // Empujando a la izquierda
        empujarIzquierda = new Animation<>(DURACION_FRAME, frames[6], frames[4], frames[6], frames[4]);
        // Empujando a la derecha
        empujarDerecha = new Animation<>(DURACION_FRAME, frames[7], frames[5], frames[7], frames[5]);
        // Caminando hacia arriba (de espaldas)
        caminarAbajo = new Animation<>(DURACION_FRAME, frames[10], frames[11]);
        caminarAbajo.setPlayMode(Animation.PlayMode.LOOP);
        // Caminando hacia abajo (de frente)
        caminarArriba = new Animation<>(DURACION_FRAME, frames[8], frames[9]);
        caminarArriba.setPlayMode(Animation.PlayMode.LOOP);
        // Mirando hacia arriba (IDLE)
        idleAbajo = new Animation<>(DURACION_FRAME, frames[13]);
        // Mirando hacia abajo (IDLE)
        idleArriba = new Animation<>(DURACION_FRAME, frames[12]);
        // Empujando hacia arriba y abajo (usamos las de caminar como sustituto)
        empujarArriba = caminarArriba;
        empujarAbajo = caminarAbajo;
    }

    public void pausarAnimacion() {
        tiempoAnimacion = 0f;
    }

    public void forzarIdle() {
        if (!moviendose) {
            estadoActual = EstadoAnimacion.IDLE;
            tiempoAnimacion = 0f;
        }
    }

    private void crearAnimacionesFallback(Texture spritesheet, int frameWidth, int frameHeight) {
        TextureRegion frameUnico = new TextureRegion(spritesheet, 0, 0,
                Math.min(frameWidth, spritesheet.getWidth()),
                Math.min(frameHeight, spritesheet.getHeight()));

        idleAbajo = idleArriba = idleIzquierda = idleDerecha = crearAnimacionEstatica(frameUnico);
        caminarAbajo = caminarArriba = caminarIzquierda = caminarDerecha = crearAnimacionEstatica(frameUnico);
        empujarAbajo = empujarArriba = empujarIzquierda = empujarDerecha = crearAnimacionEstatica(frameUnico);
    }

    private Animation<TextureRegion> crearAnimacionEstatica(TextureRegion frame) {
        return new Animation<TextureRegion>(1f, frame);
    }

    public void update(float delta) {
        tiempoAnimacion += delta;

        // --- NUEVA LÓGICA DE MOVIMIENTO SUAVE ---
        if (moviendose) {
            moveTimer += delta;
            float progress = Math.min(1f, moveTimer / MOVE_DURATION);
            
            // Interpolation.smooth crea el efecto de aceleración y desaceleración
            posVisual.set(
                Interpolation.smooth.apply(posAnterior.x, posDestino.x, progress),
                Interpolation.smooth.apply(posAnterior.y, posDestino.y, progress)
            );

            if (progress >= 1.0f) {
                moviendose = false;
                estadoActual = EstadoAnimacion.IDLE;
            }
        }
        actualizarAnimacion();
    }

    private void actualizarDireccion(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            direccionActual = dx > 0 ? DireccionMovimiento.DERECHA : DireccionMovimiento.IZQUIERDA;
        } else {
            direccionActual = dy > 0 ? DireccionMovimiento.ARRIBA : DireccionMovimiento.ABAJO;
        }
    }

  private void actualizarAnimacion() {
        switch (estadoActual) {
            case IDLE:
                switch (direccionActual) {
                    case ABAJO: animacionActual = idleAbajo; break;
                    case ARRIBA: animacionActual = idleArriba; break;
                    case IZQUIERDA: animacionActual = idleIzquierda; break;
                    case DERECHA: animacionActual = idleDerecha; break;
                }
                break;
            case CAMINANDO:
                switch (direccionActual) {
                    case ABAJO: animacionActual = caminarAbajo; break;
                    case ARRIBA: animacionActual = caminarArriba; break;
                    case IZQUIERDA: animacionActual = caminarIzquierda; break;
                    case DERECHA: animacionActual = caminarDerecha; break;
                }
                break;
            case EMPUJANDO:
                switch (direccionActual) {
                    case ABAJO: animacionActual = empujarAbajo; break;
                    case ARRIBA: animacionActual = empujarArriba; break;
                    case IZQUIERDA: animacionActual = empujarIzquierda; break;
                    case DERECHA: animacionActual = empujarDerecha; break;
                }
                break;
        }
    }

   public void moverA(int nuevoX, int nuevoY) {
        iniciarMovimiento(nuevoX, nuevoY, EstadoAnimacion.CAMINANDO);
    }


     public void moverEmpujandoA(int nuevoX, int nuevoY) {
        iniciarMovimiento(nuevoX, nuevoY, EstadoAnimacion.EMPUJANDO);
    }

      private void iniciarMovimiento(int nuevoX, int nuevoY, EstadoAnimacion nuevoEstado) {
        if (!moviendose) {
            posAnterior.set(this.x * tileSize, this.y * tileSize);
            posDestino.set(nuevoX * tileSize, nuevoY * tileSize);
            
            this.x = nuevoX;
            this.y = nuevoY;

            moviendose = true;
            estadoActual = nuevoEstado;
            moveTimer = 0f;
            tiempoAnimacion = 0f;
        }
    }
      
    public void cambiarDireccion(DireccionMovimiento nuevaDireccion) {
        direccionActual = nuevaDireccion;
        if (!moviendose) {
            estadoActual = EstadoAnimacion.IDLE;
        }
    }

    public void establecerEstadoEmpujando(boolean empujando) {
        if (moviendose) {
            estadoActual = empujando ? EstadoAnimacion.EMPUJANDO : EstadoAnimacion.CAMINANDO;
        }
    }

    public void dibujar(SpriteBatch batch, int tileSize, int offsetX, int offsetY, int filas) {
        float screenX = offsetX + posVisual.x;
        // La coordenada Y en pantalla se invierte
        float screenY = offsetY + (filas * tileSize) - posVisual.y - tileSize;

        TextureRegion frameActual = animacionActual.getKeyFrame(tiempoAnimacion, true);
        batch.draw(frameActual, screenX, screenY, tileSize, tileSize);
    }

   

    public void dibujarEn(SpriteBatch batch, float screenX, float screenY, int tileSize) {
        TextureRegion frameActual = animacionActual.getKeyFrame(tiempoAnimacion, true);
        batch.draw(frameActual, screenX, screenY, tileSize, tileSize);
    }

    public void establecerDireccion(DireccionMovimiento direccion) {
        this.direccionActual = direccion;
    }

    public DireccionMovimiento getDireccion() {
        return direccionActual;
    }

    public EstadoAnimacion getEstadoAnimacion() {
        return estadoActual;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean estaMoviendose() {
        return moviendose;
    }

  

    public Texture getTextura() {
        TextureRegion frameActual = animacionActual.getKeyFrame(tiempoAnimacion, true);
        return frameActual.getTexture();
    }

    public TextureRegion getTextureRegionActual() {
        return animacionActual.getKeyFrame(tiempoAnimacion, true);
    }
    
}