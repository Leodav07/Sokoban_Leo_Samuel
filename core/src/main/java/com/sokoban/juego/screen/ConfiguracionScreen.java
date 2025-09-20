package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorConfiguracion;
import com.sokoban.juego.logica.GestorUsuarios;
import java.io.IOException;
import java.util.Locale;

public class ConfiguracionScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    
    // UI Elements
    private Slider volumenSlider;
    private SelectBox<String> idiomaSelectBox;

    // <<--- CAMBIO: Añadido sistema de fondo y viewport como en las otras pantallas --->>
    private final OrthographicCamera backgroundCamera;
    private final FitViewport backgroundViewport;
    private SpriteBatch backgroundBatch;

    // <<--- CAMBIO: Añadido sistema de diálogo personalizado --->>
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private BitmapFont dialogFont;
    private String mensajeDialog = "";
    private boolean mostrarDialog = false;
    private final GlyphLayout layout = new GlyphLayout();
    private float dialogAlpha = 0f;
    private float dialogScale = 0.5f;
    private final OrthographicCamera dialogCamera;
    private final ScreenViewport dialogViewport;

    public ConfiguracionScreen(Main game) {
        this.game = game;
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("ConfiguracionScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
            game.setScreen(new LoginScreen(game));
        }
        
        this.backgroundCamera = new OrthographicCamera();
        this.backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        this.dialogCamera = new OrthographicCamera();
        this.dialogViewport = new ScreenViewport(dialogCamera);
    }

     @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // <<--- CAMBIO: Usando la skin de Mario y cargando recursos de forma segura --->>
        try {
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"));
            backgroundTexture = new Texture(Gdx.files.internal("menu/fondo.png"));
            backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            cuadroTexture = new Texture(Gdx.files.internal("skin/cuadro.png"));
            cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            dialogFont = skin.getFont("default-font");
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionScreen", "Error al cargar assets", e);
            // Fallback a skin por defecto para evitar crasheo
            skin = new Skin(Gdx.files.internal("uiskin.json")); 
        }

        backgroundBatch = new SpriteBatch();
        dialogBatch = new SpriteBatch();
        
        createUI();
    }
    
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        // <<--- CAMBIO: Cargando configuración desde el nuevo GestorConfiguracion --->>
        Object[] config = GestorConfiguracion.getInstancia().cargarConfiguracion();
        float volumen = (Float) config[0];
        String idioma = (String) config[1];

        Label titleLabel = new Label("CONFIGURACION", skin, "title");
        
        volumenSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumenSlider.setValue(volumen);

        idiomaSelectBox = new SelectBox<>(skin);
        idiomaSelectBox.setItems("es", "en", "fr");
        idiomaSelectBox.setSelected(idioma);

        TextButton guardarButton = new TextButton("GUARDAR", skin);
        TextButton volverButton = new TextButton("VOLVER", skin);
        
        addHoverEffect(guardarButton, Color.GREEN);
        addHoverEffect(volverButton, Color.CORAL);

        table.add(titleLabel).colspan(2).padBottom(40);
        table.row();
        table.add(new Label("Volumen", skin)).pad(10);
        table.add(volumenSlider).width(300).height(30).pad(10);
        table.row();
        table.add(new Label("Idioma", skin)).pad(10);
        table.add(idiomaSelectBox).width(300).height(50).pad(10);
        table.row().padTop(30);
        table.add(guardarButton).width(200).height(50).pad(10);
        table.add(volverButton).width(200).height(50).pad(10);

        guardarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleGuardar();
            }
        });

        volverButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // <<--- CAMBIO: Usando la transición de cortina --->>
                game.setScreen(new CortinaTransicion(game, ConfiguracionScreen.this, new MenuScreen(game)));
            }
        });
    }
    
    private void handleGuardar() {
        mostrarDialog("Guardando cambios...");
        
        new Thread(() -> {
            try {
                // <<--- CAMBIO: Guardando a través del nuevo GestorConfiguracion --->>
                GestorConfiguracion.getInstancia().guardarConfiguracion(
                        volumenSlider.getValue(),
                        idiomaSelectBox.getSelected()
                );

                // Actualizar la configuración del juego en tiempo real
                Locale locale = new Locale(idiomaSelectBox.getSelected());
                game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
                game.setVolumen(volumenSlider.getValue());
                
                Gdx.app.postRunnable(() -> {
                    mostrarDialog("¡Cambios guardados!");
                    Timer.schedule(new Timer.Task() {
                        @Override public void run() { ocultarDialog(); }
                    }, 2f);
                });

            } catch (IOException e) {
                Gdx.app.postRunnable(() -> {
                    mostrarDialog("Error al guardar: " + e.getMessage());
                    Timer.schedule(new Timer.Task() {
                        @Override public void run() { ocultarDialog(); }
                    }, 3f);
                });
            }
        }).start();
    }
    
   

      @Override
    public void render(float delta) {
        // <<--- CAMBIO: Bucle de renderizado corregido y con fondo --->>
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundViewport.apply();
        backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
        backgroundBatch.begin();
        backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        backgroundBatch.end();

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();

        if (mostrarDialog) {
            dialogViewport.apply();
            renderDialog();
        }
    }
    
    private void renderDialog() {
        float screenWidth = dialogViewport.getScreenWidth();
        float screenHeight = dialogViewport.getScreenHeight();

        dialogFont.getData().setScale(dialogScale * 0.8f);
        layout.setText(dialogFont, mensajeDialog);
        
        float textWidth = layout.width;
        float paddingX = 40 * dialogScale;
        float maxWidth = Math.min(screenWidth * 0.8f, 600 * dialogScale);
        
        float dialogWidth = Math.max(200 * dialogScale, Math.min(maxWidth, textWidth + paddingX * 2));
        float dialogHeight;

        if (textWidth + paddingX * 2 > maxWidth) {
            float wrapWidth = maxWidth - paddingX * 2;
            layout.setText(dialogFont, mensajeDialog, Color.BLACK, wrapWidth, 1, true);
            dialogHeight = Math.max(80 * dialogScale, layout.height + (30 * dialogScale) * 2);
        } else {
            dialogHeight = Math.max(80 * dialogScale, layout.height + (30 * dialogScale) * 2);
        }

        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;

        dialogCamera.update();
        dialogBatch.setProjectionMatrix(dialogCamera.combined);
        dialogBatch.begin();
        dialogBatch.setColor(1, 1, 1, dialogAlpha);
        dialogBatch.draw(cuadroTexture, dialogX, dialogY, dialogWidth, dialogHeight);

        float textX = dialogX + (dialogWidth - layout.width) / 2;
        float textY = dialogY + (dialogHeight + layout.height) / 2;
        dialogFont.setColor(0.1f, 0.1f, 0.1f, dialogAlpha);
        dialogFont.draw(dialogBatch, layout, textX, textY);
        
        dialogFont.getData().setScale(1f);
        dialogFont.setColor(Color.WHITE);
        dialogBatch.setColor(Color.WHITE);
        dialogBatch.end();
    }
    
    private void mostrarDialog(String mensaje) {
        this.mensajeDialog = mensaje;
        this.mostrarDialog = true;
        this.dialogAlpha = 0f;
        this.dialogScale = 0.5f;

        animateDialog(true);
    }
    
     private void addHoverEffect(TextButton button, Color hoverColor) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out));
                button.setColor(hoverColor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                button.setColor(Color.BLACK);
            }
        });
    }
     
     private void animateDialog(boolean show) {
        if (show) {
            Timer.schedule(new Timer.Task() {
                float time = 0f;

                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.3f) {
                        dialogAlpha = 1f;
                        dialogScale = 1f;
                        this.cancel();
                        return;
                    }
                    dialogAlpha = Interpolation.pow2Out.apply(time / 0.3f);
                    dialogScale = 0.5f + (0.5f * Interpolation.bounceOut.apply(time / 0.3f));
                }
            }, 0f, 0.02f);
        } else {
            Timer.schedule(new Timer.Task() {
                float time = 0f;

                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.2f) {
                        mostrarDialog = false;
                        mensajeDialog = "";
                        this.cancel();
                        return;
                    }
                    dialogAlpha = 1f - (time / 0.2f);
                    dialogScale = 1f - (0.3f * (time / 0.2f));
                }
            }, 0f, 0.02f);
        }
    }

     private void ocultarDialog() {
        animateDialog(false);
    }

   @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        backgroundViewport.update(width, height, true);
        dialogViewport.update(width, height, true);
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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose();
        if (dialogBatch != null) dialogBatch.dispose();
        if (cuadroTexture != null) cuadroTexture.dispose();
    }
}
