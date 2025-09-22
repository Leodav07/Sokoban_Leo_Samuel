package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.Partida;
import com.sokoban.juego.logica.SoundManager;
import java.util.List;

public class HistorialPartidasScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;

    private List<Partida> historial;
    private int paginaActual = 0;
    private final int PARTIDAS_POR_PAGINA = 5;
    private Table historialTable;
    private Label paginacionLabel;

    public HistorialPartidasScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        try {
              TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            backgroundTexture = new Texture("menu/fondo.png");
            backgroundBatch = new SpriteBatch();
            backgroundCamera = new OrthographicCamera();
            backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        } catch (Exception e) {
            Gdx.app.error("HistorialScreen", "Error cargando assets", e);
            skin = new Skin();
        }

        historial = GestorDatosPerfil.getInstancia().cargarDatosPerfil().historial;

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label(game.bundle.get("historial.historialdepartidas"), skin, "title");
        titleLabel.setFontScale(0.3f);
        root.add(titleLabel).padBottom(10).row();

        historialTable = new Table(skin);
        root.add(historialTable).expand().fill().row();

        Table navTable = new Table();
        TextButton anteriorBtn = new TextButton("<"+ game.bundle.get("historial.anterior"), skin);
        anteriorBtn.getLabel().setFontScale(0.5f);
        TextButton siguienteBtn = new TextButton(game.bundle.get("historial.siguiente") +">", skin);
        siguienteBtn.getLabel().setFontScale(0.5f);
        paginacionLabel = new Label("", skin);

        navTable.add(anteriorBtn).pad(10);
        navTable.add(paginacionLabel).pad(10);
        navTable.add(siguienteBtn).pad(10);

        actualizarTablaHistorial();

        anteriorBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paginaActual > 0) {
                    paginaActual--;
                    SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                    actualizarTablaHistorial();
                }
            }
        });

        siguienteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ((paginaActual + 1) * PARTIDAS_POR_PAGINA < historial.size()) {
                    paginaActual++;
                       SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                    actualizarTablaHistorial();
                }
            }
        });

        root.add(navTable).pad(5).row();

        TextButton regresarBtn = new TextButton(game.bundle.get("historial.regresar"), skin);
        regresarBtn.getLabel().setFontScale(0.5f);
        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                game.setScreen(new MiPerfilScreen(game));
                dispose();
            }
        });
        root.add(regresarBtn).padTop(10);
    }

    private void actualizarTablaHistorial() {
        historialTable.clear();
        historialTable.top();
        historialTable.defaults().left().pad(5);

        Label nivelLabel = new Label(game.bundle.get("historial.nivel"), skin, "subtitle");
        nivelLabel.setFontScale(0.5f);
        historialTable.add(nivelLabel).width(60);
        
         Label estadoLabel = new Label(game.bundle.get("historial.estado"), skin, "subtitle");
        estadoLabel.setFontScale(0.5f);
        historialTable.add(estadoLabel).expandX();

        Label puntajeLabel = new Label(game.bundle.get("historial.puntaje"), skin, "subtitle");
        puntajeLabel.setFontScale(0.5f);
        historialTable.add(puntajeLabel).expandX();

        Label movimientoLabel = new Label(game.bundle.get("historial.movimiento"), skin, "subtitle");
        movimientoLabel.setFontScale(0.5f);
        historialTable.add(movimientoLabel).width(80);

        Label tiempoLabel = new Label(game.bundle.get("historial.tiempo"), skin, "subtitle");
        tiempoLabel.setFontScale(0.5f);
        historialTable.add(tiempoLabel).width(100);

        Label fechaLabel = new Label(game.bundle.get("historial.fecha"), skin, "subtitle");
        fechaLabel.setFontScale(0.5f);
        historialTable.add(fechaLabel).expandX().row();

        historialTable.add("").height(2).colspan(6).growX().row();

        if (historial.isEmpty()) {
            Label emptyLabel = new Label(game.bundle.get("historial.nopartidajugada"), skin);
            emptyLabel.setFontScale(0.5f);
            historialTable.add(emptyLabel).colspan(6).pad(20);

        } else {
            int inicio = paginaActual * PARTIDAS_POR_PAGINA;
            int fin = Math.min(inicio + PARTIDAS_POR_PAGINA, historial.size());
            for (int i = inicio; i < fin; i++) {
                Partida p = historial.get(i);
                
                historialTable.add(String.valueOf(p.nivelId));
                historialTable.add(estadoLabel);
                historialTable.add(String.valueOf(p.puntaje));
                historialTable.add(String.valueOf(p.movimientos));
                historialTable.add(p.getTiempoFormateado());
                historialTable.add(p.getFechaFormateada()).row();
            }
        }

        int totalPaginas = (int) Math.ceil((double) historial.size() / PARTIDAS_POR_PAGINA);
        if (totalPaginas == 0) {
            totalPaginas = 1;
        }
        paginacionLabel.setText(game.bundle.get("historial.pag") + (paginaActual + 1) + game.bundle.get("historial.de") + totalPaginas);
        paginacionLabel.setFontScale(0.4f);
    }

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
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
        if (backgroundViewport != null) {
            backgroundViewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (backgroundBatch != null) {
            backgroundBatch.dispose();
        }
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
