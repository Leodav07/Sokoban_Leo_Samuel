/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;

/**
 *
 * @author hnleo
 */
public class MenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private Main game;
    private GestorUsuarios gestor;

    public MenuScreen(Main game) {
        this.game = game;
        gestor = GestorUsuarios.getInstancia();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Usa la skin por defecto de LibGDX
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Root table: una fila central con el contenido y una fila inferior con el registro
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label("MENU PRINCIPAL", skin, "title");

        TextButton jugarButton = new TextButton("JUGAR", skin);

        TextButton configButton = new TextButton("CONFIGURACION", skin);

        TextButton salirButton = new TextButton("Cerrar Sesion", skin);

        Table content = new Table();
        content.add(titleLabel).colspan(2).padBottom(20);
        content.row();
        content.add(jugarButton).width(220);
        content.row();
        content.add(configButton).width(220);
        content.row();
        content.add(salirButton).colspan(2).padTop(20).width(160);

        // Coloca el contenido centrado
        root.add(content).expand().center();
        root.row();

        // --------- Barra inferior derecha ---------
//        Label registerLabel = new Label("¿No tienes una cuenta?", skin);
//        TextButton registerButton = new TextButton("Registrarse", skin);
//        Table bottomRight = new Table();
//        bottomRight.add(registerLabel).padRight(8);
//        bottomRight.add(registerButton);
//        registerButton.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
//                game.setScreen(new RegistroScreen(game));
//            }
//        });
//
//        // Abajo a la derecha
//        root.add(bottomRight).expandX().right().pad(10);
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
