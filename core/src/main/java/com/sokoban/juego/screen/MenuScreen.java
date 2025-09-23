package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.SoundManager;

public class MenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private final Main game;
    private Texture backgroundTexture;
    private Texture titleTexture;
    private final FitViewport viewport;
    private final Camera camera;
    private final SpriteBatch batch;

    // Elementos de la UI para animaciones
    private TextButton jugarButton;
    private TextButton miPerfilButton;
    private TextButton salirButton, rankingButton;
    private Table bottomRight;

    public MenuScreen(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(384, 224, camera);
    }

    @Override
    public void show() {
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("MenuScreen", "Acceso denegado. Redirigiendo a LoginScreen.");
            game.setScreen(new LoginScreen(game));
            return;
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

           TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
        viewport.apply(true);

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

        jugarButton = new TextButton(game.bundle.get("menu.jugar"), skin);
        miPerfilButton = new TextButton(game.bundle.get("menu.miperfil"), skin);
        rankingButton = new TextButton(game.bundle.get("menu.rankingglobal"), skin);
        salirButton = new TextButton(game.bundle.get("menu.salir"), skin);

        addButtonEffects(jugarButton, Color.GREEN);
        addButtonEffects(miPerfilButton, Color.CYAN);
        addButtonEffects(rankingButton, Color.RED);
        addButtonEffects(salirButton, Color.RED);

        jugarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Screen newScreen = new LvlSelectScreen(game);
                game.setScreen(new CortinaTransicion(game, MenuScreen.this, newScreen));
                 SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
            }
        });

        miPerfilButton.addListener(new ChangeListener() {
             @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Screen newScreen = new MiPerfilScreen(game);
                game.setScreen(new CortinaTransicion(game, MenuScreen.this, newScreen));
                  SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                  
            }
        });
        
         rankingButton.addListener(new ChangeListener() {
             @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Screen newScreen = new RankingScreen(game);
                game.setScreen(new CortinaTransicion(game, MenuScreen.this, newScreen));
            }
        });

        salirButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> {
                        GestorUsuarios.cerrarSesion();
                        game.setScreen(new CortinaTransicion(game, MenuScreen.this, new LoginScreen(game)));
                        SoundManager.getInstance().play(SoundManager.SoundEffect.ERROR_MENU);  
                    })
                ));
            }
        });

        Table mainContent = new Table();
        mainContent.add(jugarButton).width(250).height(50).pad(10);
        mainContent.row();
        mainContent.add(miPerfilButton).width(250).height(50).pad(10);
        mainContent.row();
        mainContent.add(rankingButton).width(250).height(50).pad(10);
        mainContent.row();
        mainContent.add(salirButton).width(200).height(45).padTop(20);

        root.add(mainContent).expand().center();
        root.row();

        TextButton gameConfigButton = new TextButton(game.bundle.get("menu.configuracion"), skin);
        addButtonEffects(gameConfigButton, Color.ORANGE);
        
        bottomRight = new Table();
        bottomRight.add(gameConfigButton).width(120).height(30);
        
        gameConfigButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
               Screen newScreen = new ConfiguracionScreen(game);
               game.setScreen(new CortinaTransicion(game, MenuScreen.this, newScreen));
               SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
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
        jugarButton.setColor(1, 1, 1, 0);
        jugarButton.setScale(0.8f);
        jugarButton.addAction(Actions.delay(0.2f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        miPerfilButton.setColor(1, 1, 1, 0);
        miPerfilButton.setScale(0.8f);
        miPerfilButton.addAction(Actions.delay(0.4f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));
        
         rankingButton.setColor(1, 1, 1, 0);
        rankingButton.setScale(0.8f);
        rankingButton.addAction(Actions.delay(0.4f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        salirButton.setColor(1, 1, 1, 0);
        salirButton.setScale(0.8f);
        salirButton.addAction(Actions.delay(0.6f, Actions.parallel(
            Actions.fadeIn(0.6f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.bounceOut)
        )));

        bottomRight.setColor(1, 1, 1, 0);
        bottomRight.addAction(Actions.delay(0.8f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        jugarButton.addAction(Actions.delay(1.2f, Actions.forever(Actions.sequence(
            Actions.moveBy(0, 3f, 2f, Interpolation.sine),
            Actions.moveBy(0, -3f, 2f, Interpolation.sine)
        ))));

        addPulseAnimation(miPerfilButton, 1.5f);
        addPulseAnimation(salirButton, 1.8f);
        addPulseAnimation(rankingButton, 1.8f);
    }

    private void addPulseAnimation(TextButton button, float delay) {
        button.addAction(Actions.delay(delay, Actions.forever(Actions.sequence(
            Actions.scaleTo(1.01f, 1.01f, 3f, Interpolation.sine),
            Actions.scaleTo(1f, 1f, 3f, Interpolation.sine)
        ))));
    }

    

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        float titleX = (viewport.getWorldWidth() - titleTexture.getWidth()) / 2;
        float titleY = viewport.getWorldHeight() * 0.75f - (titleTexture.getHeight() / 2);
        batch.draw(titleTexture, titleX, titleY);
        batch.end();

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
        if (batch != null) batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (titleTexture != null) titleTexture.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}