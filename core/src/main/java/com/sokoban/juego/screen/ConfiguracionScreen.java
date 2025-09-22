package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.I18NBundle;
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
    
    // Elementos para el fondo (similar al MenuScreen)
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;

    // UI Elements
    private Slider volumenSlider;
    private SelectBox<String> idiomaSelectBox;
    private TextButton guardarButton;
    private TextButton volverButton;

    public ConfiguracionScreen(Main game) {
        this.game = game;
        
        // Comprobación de seguridad
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("ConfiguracionScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
        }
    }

    @Override
    public void show() {
        // Redirección si el usuario no está logueado
        if (GestorUsuarios.usuarioActual == null) {
            game.setScreen(new LoginScreen(game));
            return;
        }

        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            
            backgroundTexture = new Texture("menu/fondo.png");
            backgroundBatch = new SpriteBatch();
            backgroundCamera = new OrthographicCamera();
            backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionScreen", "No se encontró la skin de Mario. Usando skin por defecto.", e);
            skin = new Skin();
        }
        
        createUI();
        addAnimations();
    }
    
    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        // Cargar configuración actual
        Object[] config = GestorConfiguracion.getInstancia().cargarConfiguracion();
        float volumen = (Float) config[0];
        String idioma = (String) config[1];

        // Título principal
        Label titleLabel = new Label(game.bundle.get("config.configuracion"), skin, "title");
        
        // Crear controles con la skin de Mario
        Label volumenLabel = new Label(game.bundle.get("config.volumen"), skin, "subtitle");
        volumenSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumenSlider.setValue(volumen);

        Label idiomaLabel = new Label(game.bundle.get("config.idioma"), skin, "subtitle");
        idiomaSelectBox = new SelectBox<>(skin);
        idiomaSelectBox.setItems("es", "en", "fr");
        idiomaSelectBox.setSelected(idioma);

        guardarButton = new TextButton(game.bundle.get("config.guardar"), skin);
        volverButton = new TextButton(game.bundle.get("config.volver"), skin);

        // Aplicar efectos de hover similares al MenuScreen
        addButtonEffects(guardarButton, Color.GREEN);
        addButtonEffects(volverButton, Color.ORANGE);

        // Layout principal
        Table mainContent = new Table();
        
        // Título
        mainContent.add(titleLabel).colspan(2).padBottom(40);
        mainContent.row();
        
        // Controles de configuración con mejor espaciado
        mainContent.add(volumenLabel).padRight(20).padBottom(20);
        mainContent.add(volumenSlider).width(400).height(40).padBottom(20);
        mainContent.row();
        
        mainContent.add(idiomaLabel).padRight(20).padBottom(30);
        mainContent.add(idiomaSelectBox).width(300).height(50).padBottom(30);
        mainContent.row();
        
        // Botones con mejor espaciado
        Table buttonTable = new Table();
        buttonTable.add(guardarButton).width(200).height(50).padRight(20);
        buttonTable.add(volverButton).width(200).height(50);
        
        mainContent.add(buttonTable).colspan(2).padTop(20);
        
        root.add(mainContent).expand().center();

        // Agregar listeners
        guardarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                handleGuardar();
            }
        });

        volverButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new CortinaTransicion(game, ConfiguracionScreen.this, new MenuScreen(game)));
            }
        });
    }
    
    private void addButtonEffects(TextButton button, Color hoverColor) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out));
                button.setColor(hoverColor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                button.setColor(Color.WHITE);
            }
        });
    }
    
    private void addAnimations() {
        // Animación de entrada para todos los elementos
        stage.getRoot().setColor(1, 1, 1, 0);
        stage.getRoot().addAction(Actions.fadeIn(0.5f, Interpolation.pow2Out));
        
        // Animaciones escalonadas para los botones
        guardarButton.setColor(1, 1, 1, 0);
        guardarButton.setScale(0.8f);
        guardarButton.addAction(Actions.delay(0.3f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        volverButton.setColor(1, 1, 1, 0);
        volverButton.setScale(0.8f);
        volverButton.addAction(Actions.delay(0.4f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));
        
        // Animación sutil para el slider
        volumenSlider.addAction(Actions.delay(0.6f, Actions.forever(Actions.sequence(
            Actions.scaleTo(1.01f, 1.01f, 2f, Interpolation.sine),
            Actions.scaleTo(1f, 1f, 2f, Interpolation.sine)
        ))));
    }
    
    private void handleGuardar() {
        // Mostrar diálogo con estilo Mario
        mostrarDialogoEstilizado(game.bundle.get("config.guardando"), game.bundle.get("config.guardandocambios"));
        
        new Thread(() -> {
            try {
                // Guardar configuración
                GestorConfiguracion.getInstancia().guardarConfiguracion(
                        volumenSlider.getValue(),
                        idiomaSelectBox.getSelected()
                );

                Locale locale = new Locale(idiomaSelectBox.getSelected());
                game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
                game.setVolumen(volumenSlider.getValue());
                
                Gdx.app.postRunnable(() -> mostrarDialogoEstilizado(game.bundle.get("config.exito"), game.bundle.get("config.cambiosguardados")));

            } catch (IOException e) {
                Gdx.app.postRunnable(() -> mostrarDialogoEstilizado(game.bundle.get("config.error"),game.bundle.get("config.nocambiosguardados")));
            }
        }).start();
    }
    
    private void mostrarDialogoEstilizado(String titulo, String mensaje) {
        Dialog dialog = new Dialog(titulo, skin) {
            @Override
            protected void result(Object object) {
                // Animación al cerrar el diálogo
                this.addAction(Actions.sequence(
                    Actions.scaleTo(0.8f, 0.8f, 0.1f),
                    Actions.fadeOut(0.1f),
                    Actions.removeActor()
                ));
            }
        };
        
        dialog.text(mensaje);
        dialog.button(game.bundle.get("config.aceptar"), true);
        
        // Animación de entrada para el diálogo
        dialog.setScale(0.8f);
        dialog.setColor(1, 1, 1, 0);
        dialog.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.bounceOut),
            Actions.fadeIn(0.2f)
        ));
        
        dialog.show(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Dibujar fondo si está disponible
        if (backgroundTexture != null && backgroundBatch != null) {
            backgroundViewport.apply();
            backgroundCamera.update();
            backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
            backgroundBatch.begin();
            backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
            backgroundBatch.end();
        }

        // Dibujar UI
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
        if (backgroundViewport != null) backgroundViewport.update(width, height, true);
    }
    
    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose();
    }

    // Métodos no utilizados
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
}