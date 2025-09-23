package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorConfiguracion;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.SoundManager;

import java.io.IOException;
import java.util.Locale;

public class ConfiguracionScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;

    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;

    // Sistema de diálogos estilo RegistroScreen
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private BitmapFont dialogFont;
    private String mensajeDialog = "";
    private boolean mostrarDialog = false;
    private final GlyphLayout layout = new GlyphLayout();
    private float dialogAlpha = 0f;
    private float dialogScale = 0.5f;
    private OrthographicCamera dialogCamera;
    private ScreenViewport dialogViewport;

    private Slider volumenSlider;
    private SelectBox<String> idiomaSelectBox;
    private TextButton guardarButton;
    private TextButton volverButton;
    private Label titleLabel;
    private Label volumenLabel;
    private Label idiomaLabel;

    public ConfiguracionScreen(Main game) {
        this.game = game;
        this.dialogCamera = new OrthographicCamera();
        this.dialogViewport = new ScreenViewport(dialogCamera);

        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("ConfiguracionScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
        }
    }

    @Override
    public void show() {
        if (GestorUsuarios.usuarioActual == null) {
            game.setScreen(new LoginScreen(game));
            return;
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);

            backgroundTexture = new Texture("menu/fondo.png");
            backgroundBatch = new SpriteBatch();
            backgroundCamera = new OrthographicCamera();
            backgroundViewport = new FitViewport(384, 224, backgroundCamera);

            cuadroTexture = new Texture("skin/cuadro.png");
            cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            dialogBatch = new SpriteBatch();

            if (skin.has("default-font", BitmapFont.class)) {
                dialogFont = skin.getFont("default-font");
            } else {
                Gdx.app.error("ConfiguracionScreen", "La fuente 'default-font' no se encontró en el skin. Usando fuente por defecto.");
                dialogFont = new BitmapFont();
            }
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionScreen", "No se encontró la skin. Usando skin por defecto.", e);
            skin = new Skin();
            dialogFont = new BitmapFont();
        }

        createUI();
        addAnimations();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Object[] config = GestorConfiguracion.getInstancia().cargarConfiguracion();
        float volumen = (Float) config[0];
        String idioma = (String) config[1];
        // --- Título ---
        titleLabel = new Label(game.bundle.get("config.configuracion"), skin, "title");

        // --- Controles ---
        volumenLabel = new Label(game.bundle.get("config.volumen"), skin, "subtitle");
        volumenSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumenSlider.setValue(volumen);

        idiomaLabel = new Label(game.bundle.get("config.idioma"), skin, "subtitle");
        idiomaSelectBox = new SelectBox<>(skin);
        idiomaSelectBox.setItems("es", "en", "fr");
        idiomaSelectBox.setSelected(idioma);

        // --- Botones ---
        guardarButton = new TextButton(game.bundle.get("config.guardar"), skin);
        volverButton = new TextButton(game.bundle.get("config.volver"), skin);

        addButtonEffects(guardarButton, Color.GREEN);
        addButtonEffects(volverButton, Color.ORANGE);
        
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle(skin.get("default-horizontal", Slider.SliderStyle.class));
        float knobSize = 25f;
        float barHeight = 12f;
        sliderStyle.background.setMinHeight(barHeight);
        sliderStyle.knob.setMinHeight(knobSize);
        sliderStyle.knob.setMinWidth(knobSize);
        // Asegurarse de que los estados hover y presionado también sean grandes
        if (sliderStyle.knobOver != null) {
            sliderStyle.knobOver.setMinHeight(knobSize);
            sliderStyle.knobOver.setMinWidth(knobSize);
        }
        if (sliderStyle.knobDown != null) {
            sliderStyle.knobDown.setMinHeight(knobSize);
            sliderStyle.knobDown.setMinWidth(knobSize);
        }
        volumenSlider.setStyle(sliderStyle);

        // Layout principal centrado
        Table container = new Table();
        container.center();

        container.add(titleLabel).padBottom(80).row(); // Título más arriba

        // Sección de volumen
        Table volumenSection = new Table();
        volumenSection.add(volumenLabel).left().padBottom(15).row();
        volumenSection.add(volumenSlider).width(400).height(60).row(); // Slider más grueso y ancho

        container.add(volumenSection).padBottom(40).row();

        // Sección de idioma
        Table idiomaSection = new Table();
        idiomaSection.add(idiomaLabel).left().padBottom(15).row();
        idiomaSection.add(idiomaSelectBox).width(200).height(45).row(); // SelectBox más compacto

        container.add(idiomaSection).padBottom(80).row(); // Más separación antes de botones

        // Botones con mucha más separación
        Table buttonTable = new Table();
        buttonTable.add(guardarButton).width(160).height(50).padRight(75); // Mucha más separación
        buttonTable.add(volverButton).width(160).height(50).padLeft(75);

        container.add(buttonTable).row();

        root.add(container);

        // --- Listeners ---
        guardarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                handleGuardar();
            }
        });

        volverButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
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
                button.setColor(Color.BLACK);
            }
        });
    }

    private void addAnimations() {
        // Animación de entrada general
        stage.getRoot().setColor(1, 1, 1, 0);
        stage.getRoot().addAction(Actions.fadeIn(0.5f, Interpolation.pow2Out));

        // Animación del título
        titleLabel.setColor(1, 1, 1, 0);
        titleLabel.setScale(0.8f);
        titleLabel.addAction(Actions.delay(0.1f, Actions.parallel(
                Actions.fadeIn(0.6f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        // Animación de "respiración" para el título
        titleLabel.addAction(Actions.delay(0.8f, Actions.forever(Actions.sequence(
                Actions.scaleTo(1.02f, 1.02f, 2.5f, Interpolation.sine),
                Actions.scaleTo(1f, 1f, 2.5f, Interpolation.sine)
        ))));

        // Animación de labels
        volumenLabel.setColor(1, 1, 1, 0);
        volumenLabel.setX(volumenLabel.getX() - 50);
        volumenLabel.addAction(Actions.delay(0.3f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.moveBy(50, 0, 0.5f, Interpolation.pow2Out)
        )));

        idiomaLabel.setColor(1, 1, 1, 0);
        idiomaLabel.setX(idiomaLabel.getX() - 50);
        idiomaLabel.addAction(Actions.delay(0.5f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.moveBy(50, 0, 0.5f, Interpolation.pow2Out)
        )));

        // Animación del slider
        volumenSlider.setColor(1, 1, 1, 0);
        volumenSlider.setScale(0.9f);
        volumenSlider.addAction(Actions.delay(0.4f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.bounceOut)
        )));

        // Animación del selectbox
        idiomaSelectBox.setColor(1, 1, 1, 0);
        idiomaSelectBox.setScale(0.9f);
        idiomaSelectBox.addAction(Actions.delay(0.6f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.bounceOut)
        )));

        // Animación de botones
        guardarButton.setColor(1, 1, 1, 0);
        guardarButton.setScale(0.8f);
        guardarButton.addAction(Actions.delay(0.7f, Actions.parallel(
                Actions.fadeIn(0.6f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        volverButton.setColor(1, 1, 1, 0);
        volverButton.setScale(0.8f);
        volverButton.addAction(Actions.delay(0.8f, Actions.parallel(
                Actions.fadeIn(0.6f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        // Animaciones sutiles continuas
        addPulseAnimation(guardarButton, 1.2f);
        addPulseAnimation(volverButton, 1.4f);

        // Efecto flotante para el slider
        volumenSlider.addAction(Actions.delay(1.0f, Actions.forever(Actions.sequence(
                Actions.moveBy(0, 2f, 3f, Interpolation.sine),
                Actions.moveBy(0, -2f, 3f, Interpolation.sine)
        ))));
    }

    private void addPulseAnimation(Actor actor, float delay) {
        actor.addAction(Actions.delay(delay, Actions.forever(Actions.sequence(
                Actions.scaleTo(1.01f, 1.01f, 2.5f, Interpolation.sine),
                Actions.scaleTo(1f, 1f, 2.5f, Interpolation.sine)
        ))));
    }

    private void handleGuardar() {
        mostrarDialogoEstilizado(game.bundle.get("config.guardandocambios"));

        new Thread(() -> {
            try {
                GestorConfiguracion.getInstancia().guardarConfiguracion(
                        volumenSlider.getValue(),
                        idiomaSelectBox.getSelected()
                );

                Locale locale = new Locale(idiomaSelectBox.getSelected());
                game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
                game.setVolumen(volumenSlider.getValue());

                Gdx.app.postRunnable(() -> {
                    SoundManager.getInstance().play(SoundManager.SoundEffect.GUARDADO);
                    mostrarDialogoEstilizado(game.bundle.get("config.cambiosguardados"));
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            ocultarDialog();

                            Screen nextScreen = new MenuScreen(game);
                            game.setScreen(new CortinaTransicion(game, ConfiguracionScreen.this, nextScreen));
                        }
                    }, 2f);
                });

            } catch (IOException e) {
                Gdx.app.postRunnable(() -> {
                    SoundManager.getInstance().play(SoundManager.SoundEffect.ERROR_MENU);
                    mostrarDialogoEstilizado(game.bundle.get("config.nocambiosguardados"));
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            ocultarDialog();
                        }
                    }, 3f);
                });
            }
        }).start();
    }

    // Sistema de diálogos estilo RegistroScreen
    private void mostrarDialogoEstilizado(String mensaje) {
        this.mensajeDialog = mensaje;
        this.mostrarDialog = true;
        this.dialogAlpha = 0f;
        this.dialogScale = 0.5f;

        animateDialog(true);
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

    private void renderDialog() {
        float screenWidth = dialogViewport.getScreenWidth();
        float screenHeight = dialogViewport.getScreenHeight();

        dialogFont.getData().setScale(dialogScale * 1.0f); // Texto más grande
        layout.setText(dialogFont, mensajeDialog);

        float textWidth = layout.width;
        float paddingX = 60 * dialogScale; // Más padding horizontal
        float maxWidth = Math.min(screenWidth * 0.9f, 800 * dialogScale); // Diálogo más ancho

        float dialogWidth = Math.max(300 * dialogScale, Math.min(maxWidth, textWidth + paddingX * 2)); // Ancho mínimo mayor
        float dialogHeight;

        if (textWidth + paddingX * 2 > maxWidth) {
            float wrapWidth = maxWidth - paddingX * 2;
            layout.setText(dialogFont, mensajeDialog, Color.BLACK, wrapWidth, 1, true);
            dialogHeight = Math.max(120 * dialogScale, layout.height + (50 * dialogScale) * 2); // Altura mínima mayor
        } else {
            dialogHeight = Math.max(120 * dialogScale, layout.height + (50 * dialogScale) * 2); // Más padding vertical
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. RENDERIZAR FONDO
        if (backgroundTexture != null && backgroundBatch != null) {
            backgroundViewport.apply();
            backgroundCamera.update();
            backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
            backgroundBatch.begin();
            backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
            backgroundBatch.end();
        }

        // 2. RENDERIZAR UI
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();

        // 3. RENDERIZAR DIÁLOGO
        if (mostrarDialog) {
            dialogViewport.apply();
            renderDialog();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
        if (backgroundViewport != null) {
            backgroundViewport.update(width, height, true);
        }
        if (dialogViewport != null) {
            dialogViewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (backgroundBatch != null) {
            backgroundBatch.dispose();
        }
        if (dialogBatch != null) {
            dialogBatch.dispose();
        }
        if (cuadroTexture != null) {
            cuadroTexture.dispose();
        }
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
}
