package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;
import java.util.Locale;

public class MenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Main game;
    private GestorUsuarios gestor;

    private Texture backgroundTexture;
    private Texture titleTexture;
    private FitViewport viewport;
    private Camera camera;
    private SpriteBatch batch;
    
    // Elementos de la UI para animaciones
    private TextButton jugarButton;
    private TextButton configButton;
    private TextButton salirButton;
    private Table mainContent;
    private Table bottomRight;

    public MenuScreen(Main game) {
        this.game = game;
        gestor = GestorUsuarios.getInstancia();
    }

    @Override
    public void show() {
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("MenuScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
            game.setScreen(new LoginScreen(game));
            return;
        }

        String username = GestorUsuarios.usuarioActual.getUsername();
        float volumen = 0.5f;
        String idioma = "es";

        FileHandle userConfigDir = Gdx.files.local("users/" + username);
        FileHandle configFile = userConfigDir.child("config.txt");

        if (configFile.exists()) {
            try {
                String configContent = configFile.readString();
                String[] lines = configContent.split("\n");
                for (String line : lines) {
                    if (line.startsWith("volumen=")) {
                        volumen = Float.parseFloat(line.split("=")[1]);
                    } else if (line.startsWith("idioma=")) {
                        idioma = line.split("=")[1];
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("MenuScreen", "Error al leer config.txt para el usuario " + username, e);
            }
        }

        game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), new Locale(idioma));
        game.setVolumen(volumen);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("skin/mario_skin.json"));

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(384, 224, camera);
        viewport.apply(true);

        // Cargar las texturas
        backgroundTexture = new Texture("menu/fondo.png");
        titleTexture = new Texture("menu/titulo.png");
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        titleTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        createUI();
        addAnimations();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Crear botones mejorados sin título del menú
        jugarButton = new TextButton(game.bundle.get("menu.jugar"), skin);
        configButton = new TextButton(game.bundle.get("menu.miperfil"), skin);
        salirButton = new TextButton(game.bundle.get("menu.salir"), skin);

        // Agregar efectos hover a los botones
        addButtonEffects(jugarButton, Color.GREEN);
        addButtonEffects(configButton, Color.CYAN);
        addButtonEffects(salirButton, Color.RED);

        // Listeners de los botones
        jugarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                jugarButton.addAction(Actions.sequence(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.scaleTo(1f, 1f, 0.05f)
                ));
                Screen newScreen = new LvlSelectScreen(game);
                game.setScreen(new CortinaTransicion(game, MenuScreen.this, newScreen));
            }
        });

        configButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                configButton.addAction(Actions.sequence(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.scaleTo(1f, 1f, 0.05f)
                ));
                game.setScreen(new ConfiguracionScreen(game));
            }
        });

        salirButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                salirButton.addAction(Actions.sequence(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.scaleTo(1f, 1f, 0.05f)
                ));
                // Efecto de fade out antes de salir
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> {
                        GestorUsuarios.cerrarSesion();
                        game.setScreen(new LoginScreen(game));
                    })
                ));
            }
        });

        // Contenido principal sin título
        mainContent = new Table();
        mainContent.add(jugarButton).width(250).height(50).pad(10);
        mainContent.row();
        mainContent.add(configButton).width(250).height(50).pad(10);
        mainContent.row();
        mainContent.add(salirButton).width(200).height(45).padTop(20);

        root.add(mainContent).expand().center();
        root.row();

        // Barra inferior para configuración rápida
        Label gameConfigLabel = new Label("Configuracion", skin);
        gameConfigLabel.setColor(Color.LIGHT_GRAY);
        
        TextButton gameConfigButton = new TextButton("MI PERFIL", skin);
        addButtonEffects(gameConfigButton, Color.ORANGE);
        
        bottomRight = new Table();
        bottomRight.add(gameConfigLabel).padRight(15);
        bottomRight.add(gameConfigButton).width(120).height(30);
        
        gameConfigButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new ConfiguracionScreen(game));
            }
        });

        root.add(bottomRight).expandX().center().pad(20);
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
        // Animación de entrada para los botones principales (con delays escalonados)
        jugarButton.setColor(1, 1, 1, 0);
        jugarButton.setScale(0.8f);
        jugarButton.addAction(Actions.delay(0.2f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        configButton.setColor(1, 1, 1, 0);
        configButton.setScale(0.8f);
        configButton.addAction(Actions.delay(0.4f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        salirButton.setColor(1, 1, 1, 0);
        salirButton.setScale(0.8f);
        salirButton.addAction(Actions.delay(0.6f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        // Animación para la barra inferior
        bottomRight.setColor(1, 1, 1, 0);
        bottomRight.addAction(Actions.delay(0.8f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        // Animación sutil de flotación para el botón principal
        jugarButton.addAction(Actions.delay(1.2f, Actions.forever(Actions.sequence(
            Actions.moveBy(0, 3f, 2f, Interpolation.sine),
            Actions.moveBy(0, -3f, 2f, Interpolation.sine)
        ))));

        // Animación de pulsación para los botones
        addPulseAnimation(configButton, 1.5f);
        addPulseAnimation(salirButton, 1.8f);
    }

    private void addPulseAnimation(TextButton button, float delay) {
        button.addAction(Actions.delay(delay, Actions.forever(Actions.sequence(
            Actions.scaleTo(1.01f, 1.01f, 3f, Interpolation.sine),
            Actions.scaleTo(1f, 1f, 3f, Interpolation.sine)
        ))));
    }

    private void ventanaDialog(String mensaje) {
        Dialog dialog = new Dialog("Aviso", skin) {
            @Override
            protected void result(Object object) {
                Gdx.app.log("Dialog", "Botón presionado: " + object);
            }
        };
        dialog.text(mensaje);
        dialog.button("Aceptar", true);
        dialog.show(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderizar fondo con viewport pixelado
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Dibujar el fondo
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Dibujar el título centrado
        float titleX = (viewport.getWorldWidth() - titleTexture.getWidth()) / 2;
        float titleY = viewport.getWorldHeight() * 0.75f - (titleTexture.getHeight() / 2);
        batch.draw(titleTexture, titleX, titleY);

        batch.end();

        // Renderizar UI con viewport escalado
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        viewport.update(width, height, true);
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
        backgroundTexture.dispose();
        titleTexture.dispose();
        stage.dispose();
        skin.dispose();
    }
}