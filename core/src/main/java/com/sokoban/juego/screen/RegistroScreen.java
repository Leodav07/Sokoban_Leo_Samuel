package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class RegistroScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Main game;
    private GestorUsuarios gestor;

    public RegistroScreen(Main game) {
        this.game = game;
        gestor = GestorUsuarios.getInstancia();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Usa la skin por defecto de LibGDX
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // --------- Contenido centrado ---------
        Label titleLabel = new Label("Registro", skin, "title");

        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("Usuario");

        TextField passwordField = new TextField("", skin);
        passwordField.setMessageText("Contraseña");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        TextField nombreField = new TextField("", skin);
        nombreField.setMessageText("Nombre Completo");

        TextButton loginButton = new TextButton("Registrarse", skin);
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                new Thread(() -> {
                    try {
                        if (!gestor.registrarUsuarios(usernameField.getText(), passwordField.getText(), nombreField.getText())) {
                            Gdx.app.postRunnable(() -> {
                                ventanaDialog("Elige otro nombre de usuario.");
                            });
                        } else {

                            Gdx.app.postRunnable(() -> {
                                ventanaDialog("Usuario registrado correctamente");
                            });
                        }
                    }catch (IOException io) {
                      ventanaDialog("Ocurrio un error en Disco. "+io.getMessage());
                    } catch (NoSuchAlgorithmException n) {
                      ventanaDialog("Error a hashear contraseña. "+ n.getMessage());
                    }
                }).start();
            }
        });

        Table content = new Table();
        content.add(titleLabel).colspan(2).padBottom(20);
        content.row();
        content.add(new Label("Usuario:", skin)).pad(10);
        content.add(usernameField).width(220);
        content.row();
        content.add(new Label("Contrasena:", skin)).pad(10);
        content.add(passwordField).width(220);
        content.row();
        content.add(new Label("Nombre Completo:", skin)).pad(10);
        content.add(nombreField).width(220);
        content.row();
        content.add(loginButton).colspan(2).padTop(20).width(160);

        // Coloca el contenido centrado
        root.add(content).expand().center();
        root.row();

        // --------- Barra inferior derecha ---------
        TextButton regresarButton = new TextButton("Regresar", skin);
        Table bottomRight = new Table();
        bottomRight.add(regresarButton);
        regresarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new LoginScreen(game));
            }
        });

        // Abajo a la derecha
        root.add(bottomRight).expandX().right().pad(10);
    }

    private void ventanaDialog(String mensaje) {
        Dialog dialog = new Dialog("Aviso", skin) {
            @Override
            protected void result(Object object) {
                System.out.println("Botón: " + object);
            }
        };
        dialog.text(mensaje);
        dialog.button("Aceptar", true);
        dialog.show(stage);

    }

    @Override
    public void render(float delta) {
        // Fondo negro (como antes)
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
        skin.dispose();
    }
}
