package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class ConfiguracionScreen implements Screen {

    private Stage stage;
    private Table table;
    private Slider volumenSlider;
    private SelectBox<String> idiomaSelectBox;
    private TextButton guardarButton, volverButton;
    private String username;
    private Main game;
    private I18NBundle bundle;

    public ConfiguracionScreen(Main game) {
        this.game = game;
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("ConfiguracionScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
            game.setScreen(new LoginScreen(game));
            return;
        }
        username = GestorUsuarios.usuarioActual.getUsername();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        float volumen = 0.5f;
        String idioma = "es";

        FileHandle userConfigDir = Gdx.files.local("users/" + username);
        if (!userConfigDir.exists()) {
            userConfigDir.mkdirs();
        }
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
                Gdx.app.error("ConfiguracionScreen", "Error al leer config.txt", e);
            }
        }

        volumenSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumenSlider.setValue(volumen);

        idiomaSelectBox = new SelectBox<>(skin);
        idiomaSelectBox.setItems("es", "en", "fr");
        idiomaSelectBox.setSelected(idioma);

        guardarButton = new TextButton("Guardar", skin);
        volverButton = new TextButton("Volver", skin);

        table.add(new Label("Volumen", skin)).pad(10);
        table.add(volumenSlider).width(200).pad(10);
        table.row();
        table.add(new Label("Idioma", skin)).pad(10);
        table.add(idiomaSelectBox).width(200).pad(10);
        table.row();
        table.add(guardarButton).colspan(2).pad(10);
        table.row();
        table.add(volverButton).colspan(2).pad(10);

        guardarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Dialog guardando = new Dialog("Aviso", skin);
                guardando.text("Guardando cambios...");
                guardando.show(stage);
                new Thread(() -> {
                    try {
                        boolean result = guardarConfiguracion();

                        Gdx.app.postRunnable(() -> {
                            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                @Override
                                public void run() {
                                    guardando.hide();
                                    if (result) {
                                        Dialog nuevoDialog = new Dialog("Aviso", skin);
                                        nuevoDialog.text("Cambios guardados correctamente.");
                                        nuevoDialog.button("Aceptar", true);
                                        nuevoDialog.show(stage);

                                    } else {
                                        Dialog falsoDialog = new Dialog("Ocurrio un error al guardar cambios", skin);

                                        falsoDialog.text("Cambios guardados correctamente.");
                                        falsoDialog.button("Aceptar", true);
                                        falsoDialog.show(stage);
                                    }

                                }
                            }, 2f); // <-- segundos de espera extra (2 segundos aquí)
                        });
                    } catch (Exception io) {
                        Dialog errorDialog = new Dialog("Error", skin);
                        errorDialog.text("Ocurrio un error. " + io.getMessage());
                        errorDialog.button("Aceptar", true);
                        errorDialog.show(stage);
                    }

                }).start();
                ;
            }
        });

        volverButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    private boolean guardarConfiguracion() {
        FileHandle userConfigDir = Gdx.files.local("users/" + username);
        if (!userConfigDir.exists()) {
            userConfigDir.mkdirs();
        }
        FileHandle configFile = userConfigDir.child("config.txt");

        StringBuilder sb = new StringBuilder();
        sb.append("volumen=").append(volumenSlider.getValue()).append("\n");
        sb.append("idioma=").append(idiomaSelectBox.getSelected());

        configFile.writeString(sb.toString(), false);

        Gdx.app.log("ConfiguracionScreen", "Configuración guardada para " + username);

        Locale locale = new Locale(idiomaSelectBox.getSelected());
        game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
        game.setVolumen(volumenSlider.getValue());
        return true;
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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

    @Override
    public void dispose() {
        stage.dispose();

    }
}
