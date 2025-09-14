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

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label(game.bundle.get("menu.titulo"), skin, "title");
        TextButton jugarButton = new TextButton(game.bundle.get("menu.jugar"), skin);
        TextButton configButton = new TextButton(game.bundle.get("menu.miperfil"), skin);
        TextButton salirButton = new TextButton(game.bundle.get("menu.salir"), skin);

        Table content = new Table();
        content.add(titleLabel).colspan(2).padBottom(20);
        content.row();
        content.add(jugarButton).width(220).pad(5);
        content.row();
        content.add(configButton).width(220).pad(5);
        content.row();
        content.add(salirButton).colspan(2).padTop(20).width(160);

        root.add(content).expand().center();
        root.row();

        jugarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                game.setScreen(new LvlSelectScreen(game));

                game.setScreen(new LvlSelectScreen(game));

            }
        });

        configButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new ConfiguracionScreen(game));
            }
        });

        salirButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GestorUsuarios.cerrarSesion();
                game.setScreen(new LoginScreen(game));
            }
        });

        Label gameConfigLabel = new Label(game.bundle.get("menu.miperfil"), skin);
        TextButton gameConfigButton = new TextButton(game.bundle.get("menu.miperfil"), skin);
        Table bottomRight = new Table();
        bottomRight.add(gameConfigLabel).padRight(8);
        bottomRight.add(gameConfigButton);
        gameConfigButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new ConfiguracionScreen(game));
            }
        });

        root.add(bottomRight).expandX().right().pad(10);

        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        viewport = new FitViewport(448, 224, camera);
        viewport.apply(true); // Aplica y centra la cámara
        
        // --- CARGAR LAS TEXTURAS ---
        backgroundTexture = new Texture("menu/fondo.png");
        titleTexture = new Texture("menu/titulo.png");

        // Asegúrate de usar el filtro Nearest para el pixel art
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        titleTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

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

        camera.update(); // Siempre actualiza la cámara

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Dibuja el fondo, que ocupe todo el viewport
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // 2. Dibuja el título centrado (ajusta las coordenadas X, Y según el tamaño de tu título)
        // Para centrarlo: (ancho_viewport / 2) - (ancho_titulo / 2)
        float titleX = (viewport.getWorldWidth() - titleTexture.getWidth()) / 2;
        float titleY = viewport.getWorldHeight() * 0.75f - (titleTexture.getHeight() / 2); // Un poco más arriba
        batch.draw(titleTexture, titleX, titleY);

        // 3. Si tienes botones o texto para "Play", "Exit", etc.
        // float playButtonX = (viewport.getWorldWidth() - playButtonTexture.getWidth()) / 2;
        // float playButtonY = viewport.getWorldHeight() * 0.4f;
        // batch.draw(playButtonTexture, playButtonX, playButtonY);
        batch.end();

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
