/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.sokoban.juego.logica.GestorRanking;
import com.sokoban.juego.logica.OrdenamientoRanking;
import com.sokoban.juego.logica.GestorUsuarios;
import java.util.List;
/**
 *
 * @author hnleo
 */
public class RankingScreen implements Screen {
    
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;
    
    private List<OrdenamientoRanking> ranking;
    private int paginaActual = 0;
    private final int ENTRADAS_POR_PAGINA = 7;
    private Table rankingTable;
    private Label paginacionLabel;

    public RankingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        
        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            
            // Configurar filtrado nearest neighbor
            for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                region.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            backgroundTexture = new Texture("menu/fondo.png");
            backgroundBatch = new SpriteBatch();
            backgroundCamera = new OrthographicCamera();
            backgroundViewport = new FitViewport(384, 224, backgroundCamera);
        } catch (Exception e) {
            Gdx.app.error("RankingScreen", "Error cargando assets", e);
            skin = new Skin();
        }
        
        ranking = GestorRanking.getInstancia().leerRanking();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("RANKING GLOBAL", skin, "title");
        title.setFontScale(0.8f);
        root.add(title).padBottom(10).row();

        rankingTable = new Table(skin);
        root.add(rankingTable).expand().fillX().padLeft(50).padRight(50).row();

        Table navTable = new Table();
        TextButton anteriorBtn = new TextButton("< Anterior", skin);
        TextButton siguienteBtn = new TextButton("Siguiente >", skin);
        paginacionLabel = new Label("", skin);
        
        navTable.add(anteriorBtn).pad(10);
        navTable.add(paginacionLabel).pad(10);
        navTable.add(siguienteBtn).pad(10);
        
        actualizarTablaRanking();
        
        anteriorBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paginaActual > 0) {
                    paginaActual--;
                    actualizarTablaRanking();
                }
            }
        });
        
        siguienteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ((paginaActual + 1) * ENTRADAS_POR_PAGINA < ranking.size()) {
                    paginaActual++;
                    actualizarTablaRanking();
                }
            }
        });
        
        root.add(navTable).pad(5).row();

        TextButton regresarBtn = new TextButton("Regresar", skin);
        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        root.add(regresarBtn).padTop(10);
    }
    
    private void actualizarTablaRanking() {
        rankingTable.clear();
        rankingTable.top();
        rankingTable.defaults().pad(4);

        rankingTable.add(new Label("#", skin, "subtitle")).width(50);
        rankingTable.add(new Label("Usuario", skin, "subtitle")).expandX().left();
        rankingTable.add(new Label("Puntuacion", skin, "subtitle")).width(150).right().row();
        rankingTable.add("").height(2).colspan(3).growX().row();

        if (ranking.isEmpty()) {
            rankingTable.add(new Label("Aun no hay puntuaciones.", skin)).colspan(3).pad(20);
        } else {
            int inicio = paginaActual * ENTRADAS_POR_PAGINA;
            int fin = Math.min(inicio + ENTRADAS_POR_PAGINA, ranking.size());
            for (int i = inicio; i < fin; i++) {
                OrdenamientoRanking orden = ranking.get(i);
                boolean esJugadorActual = orden.getUsername().equals(GestorUsuarios.usuarioActual.getUsername());
                Color color = esJugadorActual ? Color.YELLOW : Color.WHITE;

                Label posLabel = new Label(String.valueOf(i + 1), skin);
                posLabel.setColor(color);
                Label userLabel = new Label(orden.getUsername(), skin);
                userLabel.setColor(color);
                Label scoreLabel = new Label(String.valueOf(orden.getScore()), skin);
                scoreLabel.setColor(color);
                
                rankingTable.add(posLabel);
                rankingTable.add(userLabel).left();
                rankingTable.add(scoreLabel).right().row();
            }
        }
        
        int totalPaginas = (int) Math.ceil((double) ranking.size() / ENTRADAS_POR_PAGINA);
        if (totalPaginas == 0) totalPaginas = 1;
        paginacionLabel.setText("Pag " + (paginaActual + 1) + " de " + totalPaginas);
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
}
