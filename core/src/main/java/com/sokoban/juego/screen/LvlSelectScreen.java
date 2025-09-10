/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.sokoban.juego.logica.Nivel;
import com.sokoban.juego.logica.accounts.GestorProgreso;

/**
 *
 * @author unwir
 */
public class LvlSelectScreen implements Screen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private BitmapFont font;

    private Nivel[] niveles;
    private int[][] conexiones; // [nivel_origen][nivel_destino]
    private int nivelSeleccionadoIndex;

    // Texturas para decoración del mapa
    private Texture fondoMapa;
    private Texture iconoCaja;
    private Texture iconoMeta;

    // Progreso del jugador
    private boolean[] nivelesCompletados;

    public LvlSelectScreen() {
        inicializar();
        crearMapa();
        cargarProgreso();
    }

    private void inicializar() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(400, 300, 0);
        camera.update();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        niveles = new Nivel[7];
        nivelesCompletados = new boolean[8]; // índice 0 no se usa
        nivelSeleccionadoIndex = 0; // Primer nivel

        // Definir conexiones entre niveles (nivel origen -> nivel destino)
        conexiones = new int[][]{
            {1, 2}, // Nivel 1 conecta con nivel 2
            {2, 3}, // Nivel 2 conecta con nivel 3
            {3, 4}, // Y así sucesivamente...
            {4, 5},
            {5, 6},
            {6, 7}
        };

//                for (int i = 1; i <= 7; i++) {
//            GestorProgreso.getInstancia().getProgresoPorNivel(i).setDesbloqueado(true);
//        }  
//                GestorProgreso.getInstancia().guardarProgreso();
    }

    private void crearMapa() {
        // Crear los 7 niveles en posiciones específicas tipo Mario World

        niveles[0] = new Nivel(1, "Inicio", 100f, 200f);
        niveles[1] = new Nivel(2, "Básico", 200f, 250f);
        niveles[2] = new Nivel(3, "Esquinas", 300f, 200f);
        niveles[3] = new Nivel(4, "Laberinto", 400f, 300f);
        niveles[4] = new Nivel(5, "Complejo", 500f, 250f);
        niveles[5] = new Nivel(6, "Desafío", 600f, 200f);
        niveles[6] = new Nivel(7, "FINAL", 700f, 300f);

        for (int i = 0; i < niveles.length; i++) {
            int nivelId = niveles[i].getId();
            if (GestorProgreso.getInstancia().isNivelDesbloqueado(nivelId)) {
                if (GestorProgreso.getInstancia().isNivelCompletado(nivelId)) {
                    niveles[i].setEstado(Nivel.COMPLETADO);
                } else {
                    niveles[i].setEstado(Nivel.DISPONIBLE);
                }
            } else {
                niveles[i].setEstado(Nivel.BLOQUEADO);
            }
        }

        // Marcar el primer nivel como seleccionado
        niveles[0].setEstado(Nivel.SELECCIONADO);
    }

    private void cargarProgreso() {
        // Aquí podrías cargar desde archivo o base de datos
        // Por ahora, simularemos algunos niveles completados para prueba

        // Aplicar progreso a los niveles
        for (int i = 0; i < niveles.length; i++) {
            int nivelId = niveles[i].getId();

            if (nivelesCompletados[nivelId]) {
                niveles[i].completar();
                // Desbloquear siguiente nivel
                if (i + 1 < niveles.length) {
                    if (niveles[i + 1].getEstado() == Nivel.BLOQUEADO) {
                        niveles[i + 1].setEstado(Nivel.DISPONIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        manejarInput();

        // Limpiar pantalla con color azul oscuro tipo Mario World
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Dibujar fondo del mapa
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Título del mapa
        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
        font.draw(batch, "SOKOBAN - MUNDO DE NIVELES", 20, 550);

        // Dibujar nubes decorativas simples
        font.getData().setScale(3.0f);
        font.setColor(Color.WHITE);
        font.draw(batch, "☁", 50, 450);
        font.draw(batch, "☁", 650, 480);
        font.draw(batch, "☁", 350, 420);

        batch.end();

        // Dibujar conexiones entre niveles (caminos)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int[] conexion : conexiones) {
            int origenIndex = conexion[0] - 1; // Convertir ID a índice
            int destinoIndex = conexion[1] - 1;

            if (origenIndex < niveles.length && destinoIndex < niveles.length) {
                Nivel origen = niveles[origenIndex];
                Nivel destino = niveles[destinoIndex];

                // Solo dibujar camino si el nivel origen está disponible o completado
                if (origen.puedeJugar() || origen.getEstado() == Nivel.SELECCIONADO) {
                    shapeRenderer.setColor(0.4f, 0.6f, 0.3f, 1); // Verde pasto
                } else {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1); // Gris bloqueado
                }

                dibujarCamino(origen.getX(), origen.getY(), destino.getX(), destino.getY(), 8);
            }
        }

        shapeRenderer.end();

        // Dibujar niveles como círculos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Nivel nivel : niveles) {
            Color color = nivel.getColor();
            float radio = nivel.esBoss() ? 35 : 25;

            // Sombra del círculo
            shapeRenderer.setColor(0, 0, 0, 0.3f);
            shapeRenderer.circle(nivel.getX() + 3, nivel.getY() - 3, radio + 2);

            // Borde del círculo
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(nivel.getX(), nivel.getY(), radio + 2);

            // Círculo principal
            shapeRenderer.setColor(color);
            shapeRenderer.circle(nivel.getX(), nivel.getY(), radio);

            // Efecto de brillo si está seleccionado
            if (nivel.getEstado() == Nivel.SELECCIONADO) {
                shapeRenderer.setColor(1, 1, 1, 0.3f);
                shapeRenderer.circle(nivel.getX(), nivel.getY(), radio - 5);
            }
        }

        shapeRenderer.end();

        // Dibujar texto de los niveles
        batch.begin();
        font.getData().setScale(1.2f);

        for (Nivel nivel : niveles) {
            // Número del nivel en el centro
            font.setColor(Color.BLACK);
            String numeroNivel = String.valueOf(nivel.getId());
            font.draw(batch, numeroNivel, nivel.getX() - 8, nivel.getY() + 6);

            // Nombre del nivel debajo
            font.getData().setScale(0.8f);
            font.setColor(Color.WHITE);
            float anchoTexto = nivel.getNombre().length();
            font.draw(batch, nivel.getNombre(), nivel.getX() - anchoTexto / 2, nivel.getY() - 50);

            // Estrella si está completado
            if (nivel.getEstado() == Nivel.COMPLETADO) {
                font.getData().setScale(1.5f);
                font.setColor(Color.GOLD);
                font.draw(batch, "★", nivel.getX() + 20, nivel.getY() + 25);
            }

            // Candado si está bloqueado
            if (nivel.getEstado() == Nivel.BLOQUEADO) {
                font.getData().setScale(1.2f);
                font.setColor(Color.RED);
                font.draw(batch, "🔒", nivel.getX() + 20, nivel.getY() + 20);
            }

            font.getData().setScale(1.2f);
        }

        // Información del nivel seleccionado
        if (nivelSeleccionadoIndex < niveles.length) {
            Nivel nivelActual = niveles[nivelSeleccionadoIndex];
            font.getData().setScale(1.5f);
            font.setColor(Color.YELLOW);
            font.draw(batch, "→ " + nivelActual.getNombre(), 50, 150);

            font.getData().setScale(1.0f);
            font.setColor(Color.WHITE);
            String estado = "";
            switch (nivelActual.getEstado()) {
                case Nivel.BLOQUEADO:
                    estado = "BLOQUEADO";
                    break;
                case Nivel.DISPONIBLE:
                    estado = "LISTO PARA JUGAR";
                    break;
                case Nivel.COMPLETADO:
                    estado = "COMPLETADO ★";
                    break;
                case Nivel.SELECCIONADO:
                    estado = "SELECCIONADO";
                    break;
            }
            font.draw(batch, "Estado: " + estado, 50, 120);
        }

        // Instrucciones
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        font.draw(batch, "CONTROLES:", 50, 80);
        font.draw(batch, "FLECHAS: Navegar  |  ENTER: Jugar  |  ESC: Menú", 50, 50);
        font.draw(batch, "CLICK: Seleccionar nivel", 50, 30);

        batch.end();
    }

    private void manejarInput() {
        // Navegación con teclado
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            moverSeleccion(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            moverSeleccion(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            buscarNivelEnDireccion(0, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            buscarNivelEnDireccion(0, -1);
        }

        // Iniciar nivel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (nivelSeleccionadoIndex < niveles.length) {
                Nivel nivel = niveles[nivelSeleccionadoIndex];
                if (nivel.puedeJugar()) {
                    iniciarNivel(nivel.getId());
                }
            }
        }

        // Volver al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // game.setScreen(new MenuPrincipalScreen(game));
            System.out.println("Volviendo al menú principal...");
        }

        // Click del mouse
        if (Gdx.input.justTouched()) {
            Vector3 clickPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(clickPos);

            for (int i = 0; i < niveles.length; i++) {
                Nivel nivel = niveles[i];
                float distancia = calcularDistancia(nivel.getX(), nivel.getY(),
                        clickPos.x, clickPos.y);

                float radio = nivel.esBoss() ? 35 : 25;
                if (distancia <= radio && nivel.puedeJugar()) {
                    seleccionarNivel(i);
                    // Doble click para iniciar
                    if (distancia <= radio - 10) {
                        iniciarNivel(nivel.getId());
                    }
                    break;
                }
            }
        }
    }

    private void moverSeleccion(int direccion) {
        int nuevoIndex = nivelSeleccionadoIndex;

        do {
            nuevoIndex += direccion;
            if (nuevoIndex < 0) {
                nuevoIndex = niveles.length - 1;
            }
            if (nuevoIndex >= niveles.length) {
                nuevoIndex = 0;
            }
        } while (!niveles[nuevoIndex].puedeJugar() && nuevoIndex != nivelSeleccionadoIndex);

        if (niveles[nuevoIndex].puedeJugar()) {
            seleccionarNivel(nuevoIndex);
        }
    }

    private void buscarNivelEnDireccion(float deltaX, float deltaY) {
        if (nivelSeleccionadoIndex >= niveles.length) {
            return;
        }

        Nivel nivelActual = niveles[nivelSeleccionadoIndex];
        int mejorIndex = -1;
        float menorDistancia = Float.MAX_VALUE;

        for (int i = 0; i < niveles.length; i++) {
            if (i == nivelSeleccionadoIndex || !niveles[i].puedeJugar()) {
                continue;
            }

            Nivel nivel = niveles[i];
            float dx = nivel.getX() - nivelActual.getX();
            float dy = nivel.getY() - nivelActual.getY();

            // Verificar si está en la dirección correcta
            if (deltaX > 0 && dx <= 0) {
                continue; // Buscando derecha pero nivel está a la izquierda
            }
            if (deltaX < 0 && dx >= 0) {
                continue; // Buscando izquierda pero nivel está a la derecha
            }
            if (deltaY > 0 && dy <= 0) {
                continue; // Buscando arriba pero nivel está abajo
            }
            if (deltaY < 0 && dy >= 0) {
                continue; // Buscando abajo pero nivel está arriba
            }
            float distancia = calcularDistancia(nivelActual.getX(), nivelActual.getY(),
                    nivel.getX(), nivel.getY());

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejorIndex = i;
            }
        }

        if (mejorIndex != -1) {
            seleccionarNivel(mejorIndex);
        }
    }

    private void seleccionarNivel(int index) {
        // Restaurar estado del nivel previamente seleccionado
        if (nivelSeleccionadoIndex < niveles.length) {
            Nivel nivelAnterior = niveles[nivelSeleccionadoIndex];
            if (nivelAnterior.getEstado() == Nivel.SELECCIONADO) {
                int nuevoEstado = nivelesCompletados[nivelAnterior.getId()]
                        ? Nivel.COMPLETADO : Nivel.DISPONIBLE;
                nivelAnterior.setEstado(nuevoEstado);
            }
        }

        // Seleccionar nuevo nivel
        nivelSeleccionadoIndex = index;
        niveles[index].setEstado(Nivel.SELECCIONADO);
    }

    private void iniciarNivel(int nivelId) {
        System.out.println("Iniciando nivel: " + nivelId);

        // this.setScreen(new GameScreenNiveles(game, nivelId));
    }

    public void marcarNivelCompletado(int nivelId) {
        nivelesCompletados[nivelId] = true;

        // Actualizar estado del nivel
        for (int i = 0; i < niveles.length; i++) {
            if (niveles[i].getId() == nivelId) {
                niveles[i].completar();

                // Desbloquear siguiente nivel
                if (i + 1 < niveles.length
                        && niveles[i + 1].getEstado() == Nivel.BLOQUEADO) {
                    niveles[i + 1].setEstado(Nivel.DISPONIBLE);
                }
                break;
            }
        }

        // Guardar progreso
        guardarProgreso();
    }

    private void guardarProgreso() {
        // Aquí guardarías en archivo o base de datos
        System.out.println("Progreso guardado");
    }

    private void dibujarCamino(float x1, float y1, float x2, float y2, float grosor) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float longitud = (float) Math.sqrt(dx * dx + dy * dy);

        if (longitud == 0) {
            return;
        }

        dx /= longitud;
        dy /= longitud;

        float perpX = -dy * grosor / 2;
        float perpY = dx * grosor / 2;

        // Dibujar rectángulo como línea gruesa usando triángulos
        shapeRenderer.triangle(x1 + perpX, y1 + perpY, x1 - perpX, y1 - perpY, x2 + perpX, y2 + perpY);
        shapeRenderer.triangle(x2 + perpX, y2 + perpY, x2 - perpX, y2 - perpY, x1 - perpX, y1 - perpY);
    }

    private float calcularDistancia(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (fondoMapa != null) {
            fondoMapa.dispose();
        }
        if (iconoCaja != null) {
            iconoCaja.dispose();
        }
        if (iconoMeta != null) {
            iconoMeta.dispose();
        }
    }
}
