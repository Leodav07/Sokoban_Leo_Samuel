package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LoginScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Main game;
    private GestorUsuarios gestor;
    private Texture backgroundTexture;

    // Para el diálogo personalizado mejorado
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private BitmapFont font;
    private BitmapFont dialogFont; // Fuente específica para diálogos
    private String mensajeDialog = "";
    private boolean mostrarDialog = false;
    private GlyphLayout layout;
    private float dialogAlpha = 0f;
    private float dialogScale = 0.5f;

    // Elementos de la UI para animaciones
    private Table mainContent;
    private Label titleLabel;
    private TextField usernameField;
    private TextField passwordField;
    private TextButton loginButton;
    private Table bottomRight;
    
    // Viewport y cámara para el fondo
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;
    private SpriteBatch backgroundBatch;
    
    // Viewport específico para diálogos
    private OrthographicCamera dialogCamera;
    private ScreenViewport dialogViewport;

    public LoginScreen(Main game) {
        this.game = game;
        gestor = GestorUsuarios.getInstancia();
    }

    @Override
    public void show() {
        // Configurar cámara y viewport para el fondo (pixelado, aspect ratio fijo)
        backgroundCamera = new OrthographicCamera();
        backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        backgroundBatch = new SpriteBatch();
        
        // Configurar cámara y viewport para diálogos (escalado suave)
        dialogCamera = new OrthographicCamera();
        dialogViewport = new ScreenViewport(dialogCamera);
        
        // Stage usa ScreenViewport para UI responsive
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("skin/mario_skin.json"));

        backgroundTexture = new Texture("menu/fondo.png");
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Inicializar diálogo personalizado mejorado
        dialogBatch = new SpriteBatch();
        cuadroTexture = new Texture("skin/cuadro.png");
        cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Fuentes: una básica y una mejorada para diálogos
        font = new BitmapFont();
        
        // Intentar usar una fuente del skin para diálogos, si no existe usar la básica
        try {
            dialogFont = skin.getFont("default-font");
            if (dialogFont == null) {
                dialogFont = font;
            }
        } catch (Exception e) {
            dialogFont = font;
        }
        
        layout = new GlyphLayout();

        createUI();
        addAnimations();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Título con estilo mejorado
        titleLabel = new Label("LOGIN", skin, "title");
        titleLabel.setColor(Color.YELLOW);

        // Campos de entrada mejorados
        usernameField = new TextField("", skin);
        usernameField.setMessageText("usuario");
        usernameField.setAlignment(1);

        passwordField = new TextField("", skin);
        passwordField.setMessageText("contrasena");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('●');
        passwordField.setAlignment(1);

        // Botón de login mejorado
        loginButton = new TextButton("INGRESAR", skin);

        // Agregar efectos hover al botón
        loginButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                loginButton.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out));
                loginButton.setColor(Color.LIGHT_GRAY);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                loginButton.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                loginButton.setColor(Color.BLACK); // Cambiado de WHITE a BLACK
            }
        });

        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loginButton.addAction(Actions.sequence(
                        Actions.scaleTo(0.95f, 0.95f, 0.05f),
                        Actions.scaleTo(1f, 1f, 0.05f)
                ));

                mostrarDialog("Verificando credenciales...");

                new Thread(() -> {
                    try {
                        boolean resultado = gestor.loginUsuario(usernameField.getText(), passwordField.getText());

                        Gdx.app.postRunnable(() -> {
                            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                @Override
                                public void run() {
                                    if (resultado) {
                                        mostrarDialog("¡Inicio de Sesion exitoso!");
                                        GestorProgreso.getInstancia().cargarProgreso();

                                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                            @Override
                                            public void run() {
                                                ocultarDialog();
                                                Screen newScreen = new MenuScreen(game);
                                                game.setScreen(new CortinaTransicion(game, LoginScreen.this, newScreen));
                                            }
                                        }, 2f);
                                    } else {
                                        mostrarDialog("Usuario o contrasena incorrectas");

                                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                            @Override
                                            public void run() {
                                                ocultarDialog();
                                            }
                                        }, 3f);
                                    }
                                }
                            }, 1.5f);
                        });
                    } catch (IOException io) {
                        Gdx.app.postRunnable(() -> {
                            mostrarDialog("Error en disco: " + io.getMessage());
                            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                @Override
                                public void run() {
                                    ocultarDialog();
                                }
                            }, 4f);
                        });
                    } catch (NoSuchAlgorithmException n) {
                        Gdx.app.postRunnable(() -> {
                            mostrarDialog("Error de encriptacion: " + n.getMessage());
                            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                @Override
                                public void run() {
                                    ocultarDialog();
                                }
                            }, 4f);
                        });
                    }
                }).start();
            }
        });

        // Contenido principal con mejor espaciado
        mainContent = new Table();
        mainContent.add(titleLabel).colspan(2).padBottom(30);
        mainContent.row();

        // Labels mejoradas para los campos
        Label userLabel = new Label("USUARIO", skin);
        userLabel.setColor(Color.RED); // Cambiado de CYAN a RED
        Label passLabel = new Label("CONTRASENA", skin);
        passLabel.setColor(Color.RED); // Cambiado de CYAN a RED

        mainContent.add(userLabel).pad(15).left();
        mainContent.add(usernameField).width(250).height(40).pad(15);
        mainContent.row();
        mainContent.add(passLabel).pad(15).left();
        mainContent.add(passwordField).width(250).height(40).pad(15);
        mainContent.row();
        mainContent.add(loginButton).colspan(2).padTop(30).width(200).height(50);

        root.add(mainContent).expand().center();
        
        // Barra de registro movida arriba - justo después del contenido principal
        Label registerLabel = new Label("¿No tienes una cuenta?", skin);
        registerLabel.setColor(Color.LIGHT_GRAY);

        TextButton registerButton = new TextButton("REGISTRARSE", skin);

        // Efectos hover para el botón de registro
        registerButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                registerButton.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out));
                registerButton.setColor(Color.GREEN);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                registerButton.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                registerButton.setColor(Color.BLACK); // Cambiado de WHITE a BLACK
            }
        });

        bottomRight = new Table();
        bottomRight.add(registerLabel).padRight(15);
        bottomRight.add(registerButton).width(150).height(35);

        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Screen newScreen = new RegistroScreen(game);
                stage.addAction(Actions.sequence(
                                         
                                                
                        Actions.fadeOut(0.3f),
                        Actions.run(() -> game.setScreen(new CortinaTransicion(game, LoginScreen.this, newScreen)))
                ));
            }
        });

        // Ahora se agrega justo debajo del contenido principal, no al final
        root.row();
        root.add(bottomRight).expandX().center().padTop(20); // Centrado y con padding arriba
    }

    private void addAnimations() {
        // Animación de entrada para el título
        titleLabel.setColor(1, 1, 1, 0);
        titleLabel.setScale(0.5f);
        titleLabel.addAction(Actions.parallel(
                Actions.fadeIn(1f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 1f, Interpolation.bounceOut)
        ));

        // Animación de entrada para los campos (con delay)
        usernameField.setColor(1, 1, 1, 0);
        usernameField.addAction(Actions.delay(0.3f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        passwordField.setColor(1, 1, 1, 0);
        passwordField.addAction(Actions.delay(0.5f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        // Animación de entrada para el botón
        loginButton.setColor(1, 1, 1, 0);
        loginButton.setScale(0.8f);
        loginButton.addAction(Actions.delay(0.7f, Actions.parallel(
                Actions.fadeIn(0.5f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.bounceOut)
        )));

        // Animación para la barra inferior
        bottomRight.setColor(1, 1, 1, 0);
        bottomRight.addAction(Actions.delay(1f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        // Animación sutil de "respiración" para el título
        titleLabel.addAction(Actions.forever(Actions.sequence(
                Actions.scaleTo(1.02f, 1.02f, 2f, Interpolation.sine),
                Actions.scaleTo(1f, 1f, 2f, Interpolation.sine)
        )));
    }

    private void mostrarDialog(String mensaje) {
        this.mensajeDialog = mensaje;
        this.mostrarDialog = true;
        this.dialogAlpha = 0f;
        this.dialogScale = 0.5f;

        animateDialog(true);
    }

    private void ocultarDialog() {
        animateDialog(false);
    }

    private void animateDialog(boolean show) {
        if (show) {
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
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
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. RENDERIZAR FONDO CON VIEWPORT PIXELADO
        backgroundViewport.apply();
        backgroundCamera.update();
        backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
        
        backgroundBatch.begin();
        // El fondo se dibuja en el espacio del mundo (384x224)
        backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        backgroundBatch.end();

        // 2. RENDERIZAR UI CON VIEWPORT ESCALADO
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();

        // 3. RENDERIZAR DIÁLOGO CON VIEWPORT DE PANTALLA
        if (mostrarDialog) {
            dialogViewport.apply();
            renderDialog();
        }
    }

    private void renderDialog() {
        // Usar las dimensiones del viewport del diálogo para cálculos
        float screenWidth = dialogViewport.getScreenWidth();
        float screenHeight = dialogViewport.getScreenHeight();

        // Preparar texto primero para medir sus dimensiones
        dialogFont.getData().setScale(dialogScale * 0.8f);
        layout.setText(dialogFont, mensajeDialog);
        
        // Calcular tamaño del diálogo basado en el texto + padding
        float textWidth = layout.width;
        float textHeight = layout.height;
        
        // Padding alrededor del texto
        float paddingX = 40 * dialogScale;
        float paddingY = 30 * dialogScale;
        
        // Tamaño mínimo y máximo para el diálogo
        float minWidth = 200 * dialogScale;
        float maxWidth = Math.min(screenWidth * 0.8f, 600 * dialogScale);
        float minHeight = 80 * dialogScale;
        float maxHeight = Math.min(screenHeight * 0.6f, 300 * dialogScale);
        
        // Calcular dimensiones finales del diálogo
        float dialogWidth = Math.max(minWidth, Math.min(maxWidth, textWidth + paddingX * 2));
        float dialogHeight = Math.max(minHeight, Math.min(maxHeight, textHeight + paddingY * 2));
        
        // Si el texto es muy largo, ajustar para texto multi-línea
        if (textWidth + paddingX * 2 > maxWidth) {
            // Recalcular el texto con wrap para que quepa en el ancho máximo
            float wrapWidth = maxWidth - paddingX * 2;
            layout.setText(dialogFont, mensajeDialog, Color.BLACK, wrapWidth, 1, true);
            dialogHeight = Math.max(minHeight, layout.height + paddingY * 2);
        }

        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;

        dialogCamera.update();
        dialogBatch.setProjectionMatrix(dialogCamera.combined);
        dialogBatch.begin();

        // Aplicar transparencia
        dialogBatch.setColor(1, 1, 1, dialogAlpha);

        // Dibujar el cuadro de fondo con el tamaño ajustado
        dialogBatch.draw(cuadroTexture, dialogX, dialogY, dialogWidth, dialogHeight);

        // Calcular posición del texto centrada
        float textX = dialogX + (dialogWidth - layout.width) / 2;
        float textY = dialogY + (dialogHeight + layout.height) / 2;

        // Configurar fuente con transparencia
        dialogFont.setColor(0.1f, 0.1f, 0.1f, dialogAlpha); // Texto negro semi-transparente

        // Dibujar el texto (ya tiene el wrap aplicado si es necesario)
        dialogFont.draw(dialogBatch, layout, textX, textY);

        // Restaurar configuración de la fuente
        dialogFont.getData().setScale(1f);
        dialogFont.setColor(Color.WHITE);

        // Restaurar color del batch
        dialogBatch.setColor(Color.WHITE);

        dialogBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Actualizar todos los viewports
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
        stage.dispose();
        skin.dispose();
        
        if (backgroundBatch != null) {
            backgroundBatch.dispose();
        }
        if (dialogBatch != null) {
            dialogBatch.dispose();
        }
        if (cuadroTexture != null) {
            cuadroTexture.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        // No disponer dialogFont si es la misma que font o viene del skin
        if (dialogFont != null && dialogFont != font) {
            // Solo disponer si no viene del skin
            try {
                if (skin.getFont("default-font") != dialogFont) {
                    dialogFont.dispose();
                }
            } catch (Exception e) {
                // Si hay error, no disponer para evitar crashes
            }
        }
    }
}