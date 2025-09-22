package com.sokoban.juego.logica.Pausa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuPausa {

    private MenuPausaListener listener;
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private Table menuTable;
    private Label titleLabel;
    private TextButton[] botones;
    
    private int opcionSeleccionada = 0;
    private boolean visible = false;
    private boolean inputHabilitado = false;
    
    // Variables para efectos
    private float tiempoAnimacion = 0f;
    private float[] efectosBoton;
    private float efectoTitulo = 0f;
    private float particleTime = 0f;
    
    // Colores mejorados
    private final Color COLOR_FONDO = new Color(0, 0, 0, 0.85f);
    private final Color COLOR_PANEL = new Color(0.1f, 0.1f, 0.2f, 0.98f);
    private final Color COLOR_PANEL_BORDE = new Color(0.3f, 0.7f, 1f, 1f);
    private final Color COLOR_NORMAL = new Color(0.9f, 0.9f, 0.9f, 1f);
    private final Color COLOR_SELECCIONADO = new Color(1f, 1f, 0.2f, 1f);
    private final Color COLOR_HOVER = new Color(0.7f, 0.7f, 1f, 1f);
    private final Color COLOR_TITULO = new Color(0.2f, 1f, 1f, 1f);
    private final Color COLOR_GLOW_SELECCIONADO = new Color(1f, 1f, 0f, 0.4f);
    
    // Dimensiones aumentadas
    private final int PANEL_WIDTH = 600;  // Más grande
    private final int PANEL_HEIGHT = 450; // Más grande

    public MenuPausa() {
         efectosBoton = new float[OpcionPausa.getTotalOpciones()];
        inicializarUI();
       
    }
    
    
    private void inicializarUI() {
        try {
            // Usar la misma skin que LoginScreen
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            
            stage = new Stage(new ScreenViewport());
            shapeRenderer = new ShapeRenderer();
            
            crearMenuUI();
            
        } catch (Exception e) {
            System.out.println("Error cargando skin para menú de pausa: " + e.getMessage());
            // Fallback con skin básica si hay error
            skin = new Skin(Gdx.files.internal("uiskin.json"));
            stage = new Stage(new ScreenViewport());
            shapeRenderer = new ShapeRenderer();
            crearMenuUI();
        }
    }

    private void crearMenuUI() {
        // Tabla principal del menú
        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.setVisible(false);
        stage.addActor(menuTable);

        // Título con efecto
        titleLabel = new Label("PAUSA", skin, "title");
        titleLabel.setColor(COLOR_TITULO);
        titleLabel.setFontScale(1.5f); // Más grande

        // Crear botones para cada opción
        botones = new TextButton[OpcionPausa.getTotalOpciones()];
        
        for (OpcionPausa opcion : OpcionPausa.values()) {
            TextButton boton = new TextButton(opcion.getTexto(), skin);
            botones[opcion.getIndice()] = boton;
            
            // Agregar efectos hover
            agregarEfectoHover(boton);
            
            // Listener para clicks
            final int indice = opcion.getIndice();
            boton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (inputHabilitado) {
                        opcionSeleccionada = indice;
                        ejecutarOpcionSeleccionada();
                    }
                }
            });
        }

        // Construir la tabla del menú
        construirMenu();
    }

    private void construirMenu() {
        menuTable.clear();
        
        // Título
        menuTable.add(titleLabel).colspan(1).padTop(60).padBottom(40);
        menuTable.row();
        
        // Botones más grandes
        for (TextButton boton : botones) {
            menuTable.add(boton).width(350).height(70).pad(15); // Botones más grandes
            menuTable.row();
        }
        
        // Instrucciones
        Label instrucciones = new Label("↑↓ Navegar | ENTER Seleccionar | ESC Continuar", skin);
        instrucciones.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        instrucciones.setFontScale(0.9f);
        menuTable.add(instrucciones).padTop(40).padBottom(30);
        
        // Actualizar selección visual inicial
        actualizarSeleccionVisual();
    }

    private void agregarEfectoHover(TextButton boton) {
        boton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (inputHabilitado) {
                    int indice = obtenerIndiceBoton(boton);
                    if (indice != opcionSeleccionada) {
                        opcionSeleccionada = indice;
                        actualizarSeleccionVisual();
                    }
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // El color se maneja en actualizarSeleccionVisual()
            }
        });
    }

    private int obtenerIndiceBoton(TextButton boton) {
        for (int i = 0; i < botones.length; i++) {
            if (botones[i] == boton) {
                return i;
            }
        }
        return 0;
    }

    public void setListener(MenuPausaListener listener) {
        this.listener = listener;
    }

    public void mostrar() {
        visible = true;
        opcionSeleccionada = 0;
        menuTable.setVisible(true);
        inputHabilitado = false;
        tiempoAnimacion = 0f;
        particleTime = 0f;
        
        // Reset efectos
        for (int i = 0; i < efectosBoton.length; i++) {
            efectosBoton[i] = 0f;
        }
        
        // Animación de entrada mejorada
        menuTable.setColor(1, 1, 1, 0);
        menuTable.setScale(0.6f);
        menuTable.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(0.4f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.4f, Interpolation.elasticOut)
            ),
            Actions.run(() -> inputHabilitado = true)
        ));
        
        // Animar título con rebote
        titleLabel.setColor(COLOR_TITULO.r, COLOR_TITULO.g, COLOR_TITULO.b, 0);
        titleLabel.setScale(0.3f);
        titleLabel.addAction(Actions.parallel(
            Actions.alpha(1f, 0.6f, Interpolation.pow3Out),
            Actions.scaleTo(1.5f, 1.5f, 0.6f, Interpolation.elasticOut)
        ));
        
        // Animar botones con efectos escalonados
        for (int i = 0; i < botones.length; i++) {
            TextButton boton = botones[i];
            boton.setColor(1, 1, 1, 0);
            boton.setScale(0.5f);
            boton.addAction(Actions.delay(0.15f * i, Actions.parallel(
                Actions.fadeIn(0.4f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.4f, Interpolation.elasticOut)
            )));
        }
        
        actualizarSeleccionVisual();
    }

    public void ocultar() {
        if (!visible) return;
        
        inputHabilitado = false;
        
        // Animación de salida
        menuTable.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeOut(0.3f, Interpolation.pow2In),
                Actions.scaleTo(0.6f, 0.6f, 0.3f, Interpolation.pow2In)
            ),
            Actions.run(() -> {
                visible = false;
                menuTable.setVisible(false);
            })
        ));
    }

    public boolean isVisible() {
        return visible;
    }

    public void manejarInput() {
        if (!visible || !inputHabilitado) {
            return;
        }

        boolean cambioSeleccion = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada--;
            if (opcionSeleccionada < 0) {
                opcionSeleccionada = OpcionPausa.getTotalOpciones() - 1;
            }
            cambioSeleccion = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada++;
            if (opcionSeleccionada >= OpcionPausa.getTotalOpciones()) {
                opcionSeleccionada = 0;
            }
            cambioSeleccion = true;
        }

        if (cambioSeleccion) {
            actualizarSeleccionVisual();
            // Efecto sonoro aquí si tienes
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            ejecutarOpcionSeleccionada();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (listener != null) {
                listener.onContinuar();
            }
        }
    }
    
    private void actualizarSeleccionVisual() {
        for (int i = 0; i < botones.length; i++) {
            TextButton boton = botones[i];
            if (i == opcionSeleccionada) {
                // Botón seleccionado - efecto brillante
                boton.setColor(COLOR_SELECCIONADO);
                boton.clearActions();
                boton.addAction(Actions.forever(Actions.sequence(
                    Actions.scaleTo(1.1f, 1.1f, 0.5f, Interpolation.sine),
                    Actions.scaleTo(1.05f, 1.05f, 0.5f, Interpolation.sine)
                )));
                efectosBoton[i] = 1f; // Activar efecto
            } else {
                // Botón normal
                boton.setColor(COLOR_NORMAL);
                boton.clearActions();
                boton.setScale(1f);
                efectosBoton[i] = 0f; // Desactivar efecto
            }
        }
    }

    private void ejecutarOpcionSeleccionada() {
        if (listener == null || !inputHabilitado) {
            return;
        }

        // Efecto visual potente de selección
        TextButton botonSeleccionado = botones[opcionSeleccionada];
        botonSeleccionado.clearActions();
        botonSeleccionado.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1.3f, 1.3f, 0.1f, Interpolation.pow2Out),
                Actions.color(Color.WHITE, 0.1f)
            ),
            Actions.parallel(
                Actions.scaleTo(0.9f, 0.9f, 0.1f, Interpolation.pow2Out),
                Actions.color(COLOR_SELECCIONADO, 0.1f)
            ),
            Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out)
        ));

        OpcionPausa opcion = OpcionPausa.getOpcionPorIndice(opcionSeleccionada);

        switch (opcion) {
            case CONTINUAR:
                listener.onContinuar();
                break;
            case REINICIAR:
                listener.onReiniciarNivel();
                break;
            case MENU_PRINCIPAL:
                listener.onVolverMenuPrincipal();
                break;
            case SALIR_JUEGO:
                listener.onSalirJuego();
                break;
        }
    }

    public void dibujar(SpriteBatch batch) {
        if (!visible) {
            return;
        }

        // Actualizar tiempo para efectos
        tiempoAnimacion += Gdx.graphics.getDeltaTime();
        particleTime += Gdx.graphics.getDeltaTime();
        efectoTitulo += Gdx.graphics.getDeltaTime() * 2f;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        batch.end();

        // Habilitar blending para efectos
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        
        // Fondo con efecto de pulsación
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float alpha = 0.85f + 0.05f * MathUtils.sin(tiempoAnimacion * 2f);
        shapeRenderer.setColor(COLOR_FONDO.r, COLOR_FONDO.g, COLOR_FONDO.b, alpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Panel principal con efectos
        int panelX = (screenWidth - PANEL_WIDTH) / 2;
        int panelY = (screenHeight - PANEL_HEIGHT) / 2;

        // Glow exterior del panel
        dibujarGlowPanel(panelX, panelY);
        
        // Panel principal
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        shapeRenderer.end();

        // Bordes del panel con efecto brillante
        dibujarBordesBrillantes(panelX, panelY);
        
        // Efectos de partículas
        dibujarEfectosParticulas(panelX, panelY);

        // Glow para botón seleccionado
        dibujarGlowBotonSeleccionado();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Renderizar UI
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        batch.begin();
    }

    private void dibujarGlowPanel(int panelX, int panelY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Múltiples capas de glow
        for (int i = 0; i < 5; i++) {
            float expansion = (i + 1) * 8f;
            float alpha = (0.3f - i * 0.05f) * (0.7f + 0.3f * MathUtils.sin(tiempoAnimacion * 1.5f));
            
            shapeRenderer.setColor(COLOR_PANEL_BORDE.r, COLOR_PANEL_BORDE.g, COLOR_PANEL_BORDE.b, alpha);
            shapeRenderer.rect(panelX - expansion, panelY - expansion, 
                             PANEL_WIDTH + 2 * expansion, PANEL_HEIGHT + 2 * expansion);
        }
        
        shapeRenderer.end();
    }

    private void dibujarBordesBrillantes(int panelX, int panelY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Borde exterior brillante
        float brightness = 0.8f + 0.2f * MathUtils.sin(tiempoAnimacion * 3f);
        shapeRenderer.setColor(COLOR_PANEL_BORDE.r * brightness, 
                              COLOR_PANEL_BORDE.g * brightness, 
                              COLOR_PANEL_BORDE.b * brightness, 1f);
        
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(panelX - 2, panelY - 2, PANEL_WIDTH + 4, PANEL_HEIGHT + 4);
        
        // Borde interior
        shapeRenderer.setColor(COLOR_PANEL_BORDE);
        Gdx.gl.glLineWidth(1f);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f); // Reset line width
    }

    private void dibujarEfectosParticulas(int panelX, int panelY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Pequeñas partículas flotantes
        for (int i = 0; i < 15; i++) {
            float x = panelX + (i * 37) % PANEL_WIDTH;
            float y = panelY + ((i * 23 + particleTime * 20) % PANEL_HEIGHT);
            float alpha = 0.3f * MathUtils.sin(particleTime * 2f + i);
            
            if (alpha > 0) {
                shapeRenderer.setColor(COLOR_TITULO.r, COLOR_TITULO.g, COLOR_TITULO.b, alpha);
                shapeRenderer.circle(x, y, 2f);
            }
        }
        
        shapeRenderer.end();
    }
    
    
    private void dibujarGlowBotonSeleccionado() {
    if (opcionSeleccionada >= 0 && opcionSeleccionada < botones.length) {
        TextButton botonSeleccionado = botones[opcionSeleccionada];
        
        // Obtener posición más precisa del botón seleccionado
        float botonY = stage.getHeight() * 0.65f - (opcionSeleccionada * 100f);
        float botonX = stage.getWidth() * 0.5f;
        
        // Ajustar posición para que esté al lado izquierdo del texto
        float glowX = botonX - 250f; // Posición a la izquierda del botón
        float glowY = botonY - 40f;        // Misma altura que el botón
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Círculo pequeño al lado izquierdo del texto
        float glowSize = 8f + 2f * MathUtils.sin(tiempoAnimacion * 4f); // Mucho más pequeño
        float alpha = 0.8f + 0.2f * MathUtils.sin(tiempoAnimacion * 3f); // Más visible
        
        shapeRenderer.setColor(COLOR_GLOW_SELECCIONADO.r, COLOR_GLOW_SELECCIONADO.g, 
                             COLOR_GLOW_SELECCIONADO.b, alpha);
        shapeRenderer.circle(glowX, glowY, glowSize);
        
        // Opcional: agregar un círculo interno más brillante
        shapeRenderer.setColor(1f, 1f, 0.5f, alpha * 0.8f);
        shapeRenderer.circle(glowX, glowY, glowSize * 0.5f);
        
        shapeRenderer.end();
    }
}

    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}