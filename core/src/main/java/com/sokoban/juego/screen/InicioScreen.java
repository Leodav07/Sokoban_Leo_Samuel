/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;

/**
 *
 * @author hnleo
 */

public class InicioScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture logoTexture;

    public InicioScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        
        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            logoTexture = new Texture(Gdx.files.internal("menu/nintendo-logo.png"));
        } catch (Exception e) {
            Gdx.app.error("InicioScreen", "Error al cargar assets", e);
            game.setScreen(new LoginScreen(game));
            return;
        }

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        Image logo = new Image(logoTexture);
        
        Label credits = new Label("Creado por Leonardo y Samuel", skin, "subtitle");
        credits.setFontScale(0.3f);
        table.add(logo).padBottom(15);
        table.row();
        table.add(credits);

        stage.getRoot().getColor().a = 0;

        stage.getRoot().addAction(Actions.sequence(
            Actions.fadeIn(1.5f, Interpolation.sine),
            Actions.delay(2.0f),
            Actions.fadeOut(1.5f, Interpolation.sine),
            Actions.run(() -> {
                game.setScreen(new LoginScreen(game));
                dispose(); 
            })
        ));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (logoTexture != null) logoTexture.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}