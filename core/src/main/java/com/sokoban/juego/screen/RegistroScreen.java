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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.SoundManager;
import java.io.IOException;

public class RegistroScreen implements Screen {

    private final Main game;
    private final GestorUsuarios gestor;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;

    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private BitmapFont dialogFont;
    private String mensajeDialog = "";
    private boolean mostrarDialog = false;
    private final GlyphLayout layout = new GlyphLayout();
    private float dialogAlpha = 0f;
    private float dialogScale = 0.5f;

    private Label titleLabel;
    private TextField usernameField;
    private TextField passwordField;
    private TextField nombreField;
    private TextButton registerButton;
    private Table bottomRight;

    private final OrthographicCamera backgroundCamera;
    private final FitViewport backgroundViewport;
    private final OrthographicCamera dialogCamera;
    private final ScreenViewport dialogViewport;
    private SpriteBatch backgroundBatch;

    public RegistroScreen(Main game) {
        this.game = game;
        this.gestor = GestorUsuarios.getInstancia();
        this.backgroundCamera = new OrthographicCamera();
        this.backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        this.dialogCamera = new OrthographicCamera();
        this.dialogViewport = new ScreenViewport(dialogCamera);
    }

   @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        try {
               TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            backgroundTexture = new Texture(Gdx.files.internal("menu/fondo.png"));
            backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            
            cuadroTexture = new Texture(Gdx.files.internal("skin/cuadro.png"));
            cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            if (skin.has("default-font", BitmapFont.class)) {
                dialogFont = skin.getFont("default-font");
            } else {
                Gdx.app.error("RegistroScreen", "La fuente 'default-font' no se encontró en el skin. Usando fuente por defecto.");
                dialogFont = new BitmapFont();
            }

        } catch (Exception e) {
            Gdx.app.error("RegistroScreen", "Error al cargar los assets. Verifica las rutas de los archivos.", e);
            skin = new Skin();
            dialogFont = new BitmapFont();
        }

        backgroundBatch = new SpriteBatch();
        dialogBatch = new SpriteBatch();

        createUI();
        addAnimations();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        titleLabel = new Label("REGISTRO", skin, "title");
        titleLabel.setColor(Color.YELLOW);

        // <<--- CAMBIO: Se inicializan los miembros de la clase, no se declaran de nuevo --->>
        usernameField = new TextField("", skin);
        usernameField.setMessageText("usuario");
        usernameField.setAlignment(1);

        passwordField = new TextField("", skin);
        passwordField.setMessageText("contrasena");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('●');
        passwordField.setAlignment(1);

        nombreField = new TextField("", skin);
        nombreField.setMessageText("nombre completo");
        nombreField.setAlignment(1);

        registerButton = new TextButton("REGISTRARSE", skin);
        addHoverEffect(registerButton, Color.LIGHT_GRAY);

        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                handleRegistration(usernameField.getText(), passwordField.getText(), nombreField.getText());
            }
        });

        Table mainContent = new Table();
        mainContent.add(titleLabel).colspan(2).padBottom(30);
        mainContent.row();
        mainContent.add(new Label("USUARIO", skin)).pad(15).left();
        mainContent.add(usernameField).width(250).height(40).pad(15);
        mainContent.row();
        mainContent.add(new Label("CONTRASENA", skin)).pad(15).left();
        mainContent.add(passwordField).width(250).height(40).pad(15);
        mainContent.row();
        mainContent.add(new Label("NOMBRE COMPLETO", skin)).pad(15).left();
        mainContent.add(nombreField).width(250).height(40).pad(15);
        mainContent.row();
        mainContent.add(registerButton).colspan(2).padTop(30).width(200).height(50);

        root.add(mainContent).expand().center();

        TextButton loginButton = new TextButton("INICIAR SESION", skin);
        addHoverEffect(loginButton, Color.GREEN);

        bottomRight = new Table();
        bottomRight.add(new Label("¿Ya tienes una cuenta?", skin)).padRight(15);
        bottomRight.add(loginButton).width(150).height(35);

        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                stage.addAction(Actions.sequence(
                        Actions.fadeOut(0.3f),
                        Actions.run(() -> game.setScreen(new LoginScreen(game)))
                ));
            }
        });

        root.row();
        root.add(bottomRight).expandX().center().padTop(20);
    }

    private void handleRegistration(String username, String password, String nombreCompleto) {
        if (username.isEmpty() || password.isEmpty() || nombreCompleto.isEmpty()) {
            SoundManager.getInstance().play(SoundManager.SoundEffect.ERROR_MENU);
            mostrarDialog("Todos los campos son obligatorios.");
            Timer.schedule(new Timer.Task() { @Override public void run() { ocultarDialog(); }}, 3f);
            return;
        }

        String mensajeError = gestor.obtenerMensajeDeErrorContraseña(password);
        if (!mensajeError.isEmpty()) {
            mostrarDialog(mensajeError);
            Timer.schedule(new Timer.Task() { @Override public void run() { ocultarDialog(); }}, 4f);
            return;
        }
         SoundManager.getInstance().play(SoundManager.SoundEffect.PAUSA);
        mostrarDialog("Creando tu cuenta...");

        new Thread(() -> {
            try {
                boolean exito = gestor.registrarUsuario(username, password, nombreCompleto);
                
                Gdx.app.postRunnable(() -> {
                    if (exito) {
                        SoundManager.getInstance().play(SoundManager.SoundEffect.GUARDADO);
                        mostrarDialog("¡Usuario registrado con éxito!");
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                ocultarDialog();
                                Screen newScreen = new LoginScreen(game);
                                game.setScreen(new CortinaTransicion(game, RegistroScreen.this, newScreen));
                            }
                        }, 2f);
                    } else {
                        SoundManager.getInstance().play(SoundManager.SoundEffect.ERROR_MENU);
                        mostrarDialog("El nombre de usuario ya existe.");
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                ocultarDialog();
                            }
                        }, 3f);
                    }
                });
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> {
                    SoundManager.getInstance().play(SoundManager.SoundEffect.ERROR_MENU);
                    mostrarDialog("Error de disco al guardar el usuario.");
                    Timer.schedule(new Timer.Task() { @Override public void run() { ocultarDialog(); }}, 4f);
                });
            } 
        }).start();
    }

    private void addAnimations() {
        titleLabel.setColor(1, 1, 1, 0);
        titleLabel.setScale(0.5f);
        titleLabel.addAction(Actions.parallel(
                Actions.fadeIn(1f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 1f, Interpolation.bounceOut)
        ));

        // <<--- AHORA ESTO FUNCIONARÁ CORRECTAMENTE --->>
        usernameField.setColor(1, 1, 1, 0);
        usernameField.addAction(Actions.delay(0.3f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        passwordField.setColor(1, 1, 1, 0);
        passwordField.addAction(Actions.delay(0.5f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));
        
        nombreField.setColor(1, 1, 1, 0);
        nombreField.addAction(Actions.delay(0.7f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        registerButton.setColor(1, 1, 1, 0);
        registerButton.setScale(0.8f);
        registerButton.addAction(Actions.delay(0.9f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.bounceOut)
        )));

        bottomRight.setColor(1, 1, 1, 0);
        bottomRight.addAction(Actions.delay(1.2f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        titleLabel.addAction(Actions.forever(Actions.sequence(
                Actions.scaleTo(1.02f, 1.02f, 2f, Interpolation.sine),
                Actions.scaleTo(1f, 1f, 2f, Interpolation.sine)
        )));
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. RENDERIZAR FONDO
        backgroundViewport.apply();
        backgroundCamera.update();
        backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
        backgroundBatch.begin();
        backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        backgroundBatch.end();

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
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose(); 
        if (dialogBatch != null) dialogBatch.dispose();
        if (cuadroTexture != null) cuadroTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Actualiza el viewport del fondo para que se ajuste a la ventana
        backgroundViewport.update(width, height, true);
        // Actualiza el viewport del diálogo
        dialogViewport.update(width, height, true);
        // Actualiza el viewport del Stage (la interfaz de usuario)
        stage.getViewport().update(width, height, true);
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