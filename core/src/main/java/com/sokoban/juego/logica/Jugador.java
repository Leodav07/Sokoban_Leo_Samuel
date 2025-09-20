package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Jugador {

    private int x, y;
    private int tileSize;

    private float posX, posY;

    private boolean moviendose = false;
    private int destinoX, destinoY;
    private float velocidad = 80f;

    private Animation<TextureRegion> animacionActual;
    private Animation<TextureRegion> idleAbajo, idleArriba, idleIzquierda, idleDerecha;
    private Animation<TextureRegion> caminarAbajo, caminarArriba, caminarIzquierda, caminarDerecha;
    private Animation<TextureRegion> empujarAbajo, empujarArriba, empujarIzquierda, empujarDerecha;

    private float tiempoAnimacion = 0f;
    private DireccionMovimiento direccionActual = DireccionMovimiento.ARRIBA;
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

        this.posX = x * tileSize;
        this.posY = y * tileSize;

        this.destinoX = x;
        this.destinoY = y;

        configurarAnimaciones(jugadorImg);

        animacionActual = idleArriba;
    }

    public Jugador(Texture spritesheet, int x, int y, int tileSize) {
        this(x, y, spritesheet, tileSize);
    }

    private void configurarAnimaciones(Texture spritesheet) {
        int FRAME_WIDTH = 32;
        int FRAME_HEIGHT = 32;
        float DURACION_FRAME = 0.3f;
        float DURACION_IDLE = 2.0f;


        try {
            TextureRegion[] sprites = new TextureRegion[14];
            for (int i = 0; i < 14; i++) {
                sprites[i] = new TextureRegion(spritesheet, i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
            }

            idleAbajo = new Animation<TextureRegion>(DURACION_IDLE, sprites[13]);
            idleIzquierda = new Animation<TextureRegion>(DURACION_IDLE, sprites[0]);
            idleDerecha = new Animation<TextureRegion>(DURACION_IDLE, sprites[1]);
            idleArriba = new Animation<TextureRegion>(DURACION_IDLE, sprites[12]);

            caminarArriba = new Animation<TextureRegion>(DURACION_FRAME, sprites[9], sprites[8],sprites[9], sprites[8], sprites[12]);
            caminarIzquierda = new Animation<TextureRegion>(DURACION_FRAME, sprites[2], sprites[0], sprites[2], sprites[2], sprites[0], sprites[2], sprites[0]);
            caminarDerecha = new Animation<TextureRegion>(DURACION_FRAME, sprites[3], sprites[1], sprites[3], sprites[3], sprites[1], sprites[3],sprites[1]);
            caminarAbajo = new Animation<TextureRegion>(DURACION_FRAME, sprites[10], sprites[11],sprites[10], sprites[11], sprites[13]);

            empujarArriba = new Animation<TextureRegion>(DURACION_FRAME * 1.2f, sprites[9], sprites[8], sprites[9], sprites[8], sprites[12]);
            empujarIzquierda = new Animation<TextureRegion>(DURACION_FRAME * 1.2f, sprites[6], sprites[4],sprites[6], sprites[4], sprites[6], sprites[4]);
            empujarDerecha = new Animation<TextureRegion>(DURACION_FRAME * 1.2f, sprites[7], sprites[5],sprites[7], sprites[5],  sprites[7], sprites[5]);
            empujarAbajo = new Animation<TextureRegion>(DURACION_FRAME * 1.2f, sprites[10], sprites[11],sprites[10], sprites[11], sprites[13]);


        } catch (Exception e) {
            e.printStackTrace();
            crearAnimacionesFallback(spritesheet, FRAME_WIDTH, FRAME_HEIGHT);
        }
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
        if (moviendose) {
            tiempoAnimacion += delta;

            float targetPosX = destinoX * tileSize;
            float targetPosY = destinoY * tileSize;

            float dx = targetPosX - posX;
            float dy = targetPosY - posY;

            actualizarDireccion(dx, dy);

            float distancia = (float) Math.sqrt(dx * dx + dy * dy);
            float maxMovimiento = velocidad * delta;

            if (distancia <= maxMovimiento) {

                posX = targetPosX;
                posY = targetPosY;
                x = destinoX;
                y = destinoY;
                moviendose = false;

                estadoActual = EstadoAnimacion.IDLE;
                tiempoAnimacion = 0f;

            } else {
                posX += maxMovimiento * dx / distancia;
                posY += maxMovimiento * dy / distancia;
            }
        } else {
            tiempoAnimacion += delta * 0.5f;
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
                    case ABAJO:
                        animacionActual = idleAbajo;
                        break;
                    case ARRIBA:
                        animacionActual = idleArriba;
                        break;
                    case IZQUIERDA:
                        animacionActual = idleIzquierda;
                        break;
                    case DERECHA:
                        animacionActual = idleDerecha;
                        break;
                }
                break;
            case CAMINANDO:
                switch (direccionActual) {
                    case ABAJO:
                        animacionActual = caminarAbajo;
                        break;
                    case ARRIBA:
                        animacionActual = caminarArriba;
                        break;
                    case IZQUIERDA:
                        animacionActual = caminarIzquierda;
                        break;
                    case DERECHA:
                        animacionActual = caminarDerecha;
                        break;
                }
                break;
            case EMPUJANDO:
                switch (direccionActual) {
                    case ABAJO:
                        animacionActual = empujarAbajo;
                        break;
                    case ARRIBA:
                        animacionActual = empujarArriba;
                        break;
                    case IZQUIERDA:
                        animacionActual = empujarIzquierda;
                        break;
                    case DERECHA:
                        animacionActual = empujarDerecha;
                        break;
                }
                break;
        }
    }

    public void moverA(int nuevoX, int nuevoY) {
        if (!moviendose) {
            if (nuevoX > x) {
                direccionActual = DireccionMovimiento.DERECHA;
            } else if (nuevoX < x) {
                direccionActual = DireccionMovimiento.IZQUIERDA;
            } else if (nuevoY > y) {
                direccionActual = DireccionMovimiento.ARRIBA;
            } else if (nuevoY < y) {
                direccionActual = DireccionMovimiento.ABAJO;
            }

            destinoX = nuevoX;
            destinoY = nuevoY;
            moviendose = true;
            estadoActual = EstadoAnimacion.CAMINANDO;
            tiempoAnimacion = 0f;
        }
    }

    public void moverEmpujandoA(int nuevoX, int nuevoY) {
        if (!moviendose) {
            if (nuevoX > x) {
                direccionActual = DireccionMovimiento.DERECHA;
            } else if (nuevoX < x) {
                direccionActual = DireccionMovimiento.IZQUIERDA;
            } else if (nuevoY > y) {
                direccionActual = DireccionMovimiento.ABAJO;
            } else if (nuevoY < y) {
                direccionActual = DireccionMovimiento.ARRIBA;
            }

            destinoX = nuevoX;
            destinoY = nuevoY;
            moviendose = true;
            estadoActual = EstadoAnimacion.EMPUJANDO;
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
        dibujarConOffset(batch, tileSize, offsetX, offsetY, filas);
    }

    public void dibujarConOffset(SpriteBatch batch, int tileSize, int offsetX, int offsetY, int filas) {
        float screenX = offsetX + posX;
        float screenY = offsetY + (filas - 1 - (posY / tileSize)) * tileSize;

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

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public Texture getTextura() {
        TextureRegion frameActual = animacionActual.getKeyFrame(tiempoAnimacion, true);
        return frameActual.getTexture();
    }

    public TextureRegion getTextureRegionActual() {
        return animacionActual.getKeyFrame(tiempoAnimacion, true);
    }
}