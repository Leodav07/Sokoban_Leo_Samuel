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
    private final int PARTIDAS_POR_PAGINA = 6;
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
            backgroundTexture = new Texture("menu/fondoTabla.png");
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
        root.add(titleLabel).padTop(5).padBottom(5).row();

        Table containerTable = new Table();
        root.add(containerTable).width(580).height(280).pad(15).row();
        containerTable.add().expandY().row();

        historialTable = new Table(skin);
        containerTable.add(historialTable).expandX().left().padLeft(20).row(); // <<-- AJUSTE: Menos padding para moverlo más a la izquierda
        
        containerTable.add().expandY().row();

        Table navTable = new Table();
        TextButton anteriorBtn = new TextButton("<" + game.bundle.get("historial.anterior"), skin);
        anteriorBtn.getLabel().setFontScale(0.4f);
        TextButton siguienteBtn = new TextButton(game.bundle.get("historial.siguiente") + ">", skin);
        siguienteBtn.getLabel().setFontScale(0.4f);
        paginacionLabel = new Label("", skin);

        navTable.add(anteriorBtn).pad(5).width(80).height(25);
        navTable.add(paginacionLabel).pad(5).expandX();
        navTable.add(siguienteBtn).pad(5).width(80).height(25);

        containerTable.add(navTable).fillX().padTop(5).padBottom(5).row();
        
        TextButton regresarBtn = new TextButton(game.bundle.get("historial.regresar"), skin);
        regresarBtn.getLabel().setFontScale(0.4f);
        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                game.setScreen(new MiPerfilScreen(game));
                dispose();
            }
        });
        containerTable.add(regresarBtn).padTop(5).width(100).height(30);

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
    }

    private void actualizarTablaHistorial() {
        historialTable.clear();
        historialTable.top();
        historialTable.defaults().left().padTop(4).padBottom(4); 

        // --- Headers ---
        Label nivelLabel = new Label(game.bundle.get("historial.nivel"), skin);
        nivelLabel.setFontScale(0.35f);
        historialTable.add(nivelLabel).width(50);
        
        Label estadoLabel = new Label(game.bundle.get("historial.estado"), skin);
        estadoLabel.setFontScale(0.35f);
        historialTable.add(estadoLabel).width(110).padLeft(20); // <<-- CAMBIO: Más ancho para "Status"
        
        Label puntajeLabel = new Label(game.bundle.get("historial.puntaje"), skin);
        puntajeLabel.setFontScale(0.35f);
        historialTable.add(puntajeLabel).width(60).padLeft(25); // <<-- CAMBIO: Más padding para separar de "Status"

        Label movimientoLabel = new Label(game.bundle.get("historial.movimiento"), skin);
        movimientoLabel.setFontScale(0.35f);
        historialTable.add(movimientoLabel).width(60).padLeft(10);

        Label tiempoLabel = new Label(game.bundle.get("historial.tiempo"), skin);
        tiempoLabel.setFontScale(0.35f);
        historialTable.add(tiempoLabel).width(70).padLeft(10);

        Label fechaLabel = new Label(game.bundle.get("historial.fecha"), skin);
        fechaLabel.setFontScale(0.35f);
        historialTable.add(fechaLabel).width(120).padLeft(10).row();

        if (historial.isEmpty()) {
            Label emptyLabel = new Label(game.bundle.get("historial.nopartidajugada"), skin);
            emptyLabel.setFontScale(0.4f);
            historialTable.add(emptyLabel).colspan(6).pad(15).center();
        } else {
            int inicio = paginaActual * PARTIDAS_POR_PAGINA;
            int fin = Math.min(inicio + PARTIDAS_POR_PAGINA, historial.size());
            
            for (int i = inicio; i < fin; i++) {
                Partida p = historial.get(i);
                
                Label nivelLbl = new Label(String.valueOf(p.nivelId), skin);
                nivelLbl.setFontScale(0.35f);
                historialTable.add(nivelLbl);
                
                Label estadoLbl = new Label(p.estado, skin);
                estadoLbl.setFontScale(0.35f);
                historialTable.add(estadoLbl).padLeft(20); // <<-- CAMBIO: Mismo padding que el header
                
                Label puntajeLbl = new Label(String.valueOf(p.puntaje), skin);
                puntajeLbl.setFontScale(0.35f);
                historialTable.add(puntajeLbl).padLeft(25); // <<-- CAMBIO: Mismo padding que el header
                
                Label movimientosLbl = new Label(String.valueOf(p.movimientos), skin);
                movimientosLbl.setFontScale(0.35f);
                historialTable.add(movimientosLbl).padLeft(10);
                
                Label tiempoLbl = new Label(p.getTiempoFormateado(), skin);
                tiempoLbl.setFontScale(0.35f);
                historialTable.add(tiempoLbl).padLeft(10);
                
                Label fechaLbl = new Label(p.getFechaFormateada(), skin);
                fechaLbl.setFontScale(0.35f);
                historialTable.add(fechaLbl).padLeft(10).row();
            }
        }

        int totalPaginas = (int) Math.ceil((double) historial.size() / PARTIDAS_POR_PAGINA);
        totalPaginas = Math.max(1, totalPaginas);
        paginacionLabel.setText(String.format("%s %d %s %d", game.bundle.get("historial.pag"), 
                               (paginaActual + 1), game.bundle.get("historial.de"), totalPaginas));
        paginacionLabel.setFontScale(0.35f);
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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}