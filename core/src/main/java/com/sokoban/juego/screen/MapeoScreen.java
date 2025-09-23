package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorMapeo;
import com.sokoban.juego.logica.SoundManager;

public class MapeoScreen implements Screen, InputProcessor {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;

    private TextButton btnArriba, btnAbajo, btnIzquierda, btnDerecha;
    private int keyArriba, keyAbajo, keyIzquierda, keyDerecha;

    private TextButton listeningButton = null;

    public MapeoScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));

        Gdx.input.setInputProcessor(this);

        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            backgroundTexture = new Texture("menu/fondoTabla.png");
            backgroundBatch = new SpriteBatch();
            backgroundCamera = new OrthographicCamera();
            backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        } catch (Exception e) {
            Gdx.app.error("KeymapScreen", "Error cargando assets", e);
            skin = new Skin();
        }

        GestorMapeo.getInstancia().cargarControles();
        keyArriba = GestorMapeo.ARRIBA;
        keyAbajo = GestorMapeo.ABAJO;
        keyIzquierda = GestorMapeo.IZQUIERDA;
        keyDerecha = GestorMapeo.DERECHA;

        createUI();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label(game.bundle.get("mapeo.mapeotitulo"), skin, "title");
        titleLabel.setFontScale(0.7f);
        root.add(titleLabel).padBottom(30).row();

        Table mappingTable = new Table();
        mappingTable.defaults().pad(10);

        btnArriba = new TextButton(Input.Keys.toString(keyArriba), skin);
        btnAbajo = new TextButton(Input.Keys.toString(keyAbajo), skin);
        btnIzquierda = new TextButton(Input.Keys.toString(keyIzquierda), skin);
        btnDerecha = new TextButton(Input.Keys.toString(keyDerecha), skin);

        // <<-- CAMBIO: Se reduce la escala de la fuente para el texto de los botones -->>
        float buttonTextScale = 0.5f;
        btnArriba.getLabel().setFontScale(buttonTextScale);
        btnAbajo.getLabel().setFontScale(buttonTextScale);
        btnIzquierda.getLabel().setFontScale(buttonTextScale);
        btnDerecha.getLabel().setFontScale(buttonTextScale);

        float buttonWidth = 200;
        float buttonHeight = 40;
        
        float labelScale = 0.5f;
        Label arribaLabel = new Label(game.bundle.get("mapeo.moverarriba"), skin);
        arribaLabel.setFontScale(labelScale);
        
        Label abajoLabel = new Label(game.bundle.get("mapeo.moverabajo"), skin);
        abajoLabel.setFontScale(labelScale);

        Label izquierdaLabel = new Label(game.bundle.get("mapeo.moverizquierda"), skin);
        izquierdaLabel.setFontScale(labelScale);

        Label derechaLabel = new Label(game.bundle.get("mapeo.moverderecha"), skin);
        derechaLabel.setFontScale(labelScale);

        mappingTable.add(arribaLabel).left();
        mappingTable.add(btnArriba).width(buttonWidth).height(buttonHeight).row();
        mappingTable.add(abajoLabel).left();
        mappingTable.add(btnAbajo).width(buttonWidth).height(buttonHeight).row();
        mappingTable.add(izquierdaLabel).left();
        mappingTable.add(btnIzquierda).width(buttonWidth).height(buttonHeight).row();
        mappingTable.add(derechaLabel).left();
        mappingTable.add(btnDerecha).width(buttonWidth).height(buttonHeight).row();

        root.add(mappingTable).row();

        TextButton guardarBtn = new TextButton(game.bundle.get("mapeo.guardar"), skin);
        TextButton volverBtn = new TextButton(game.bundle.get("mapeo.regresar"), skin);

        Table actionTable = new Table();
        actionTable.add(guardarBtn).width(150).height(40).pad(20);
        actionTable.add(volverBtn).width(150).height(40).pad(20);

        root.add(actionTable).padTop(30).row();

        addKeyListener(btnArriba, "arriba");
        addKeyListener(btnAbajo, "abajo");
        addKeyListener(btnIzquierda, "izquierda");
        addKeyListener(btnDerecha, "derecha");

        guardarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.GUARDADO);
                GestorMapeo.getInstancia().guardarControles(keyArriba, keyAbajo, keyIzquierda, keyDerecha);
            }
        });

        volverBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                game.setScreen(new CortinaTransicion(game, MapeoScreen.this, new ConfiguracionScreen(game)));
            }
        });
    }

    private void addKeyListener(TextButton button, final String keyId) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listeningButton != null) {
                    listeningButton.setText(Input.Keys.toString(getKeyCode(keyId)));
                    listeningButton.getLabel().setFontScale(0.5f); // Restaurar escala
                }
                listeningButton = button;
                button.setText(game.bundle.get("mapeo.pulsar"));
                button.getLabel().setFontScale(0.5f); // Texto "Pulsar..." a tamaño normal
                SoundManager.getInstance().play(SoundManager.SoundEffect.PAUSA);
            }
        });
    }

    private int getKeyCode(String keyId) {
        switch (keyId) {
            case "arriba":
                return keyArriba;
            case "abajo":
                return keyAbajo;
            case "izquierda":
                return keyIzquierda;
            case "derecha":
                return keyDerecha;
            default:
                return -1;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (listeningButton != null) {
            if (listeningButton == btnArriba) {
                keyArriba = keycode;
            } else if (listeningButton == btnAbajo) {
                keyAbajo = keycode;
            } else if (listeningButton == btnIzquierda) {
                keyIzquierda = keycode;
            } else if (listeningButton == btnDerecha) {
                keyDerecha = keycode;
            }

            listeningButton.setText(Input.Keys.toString(keycode));
            listeningButton.getLabel().setFontScale(0.5f); // Restaurar a escala pequeña
            listeningButton = null;
            SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
            return true;
        }
        return false;
    }

    // ... (El resto de la clase permanece igual) ...
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        backgroundViewport.apply();
        backgroundCamera.update();
        backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
        backgroundBatch.begin();
        backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        backgroundBatch.end();
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
        backgroundViewport.update(w, h, true);
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
        backgroundTexture.dispose();
        backgroundBatch.dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        return stage.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return stage.keyTyped(character);
    }

    @Override
    public boolean touchDown(int x, int y, int p, int b) {
        return stage.touchDown(x, y, p, b);
    }

    @Override
    public boolean touchUp(int x, int y, int p, int b) {
        return stage.touchUp(x, y, p, b);
    }

    @Override
    public boolean touchDragged(int x, int y, int p) {
        return stage.touchDragged(x, y, p);
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return stage.mouseMoved(x, y);
    }

    @Override
    public boolean scrolled(float aX, float aY) {
        return stage.scrolled(aX, aY);
    }

    @Override
    public boolean touchCancelled(int a, int b, int c, int d) {
        return false;
    }
}
