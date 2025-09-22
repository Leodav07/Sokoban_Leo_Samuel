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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.GestorRanking;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.OrdenamientoRanking;
import com.sokoban.juego.logica.SoundManager;
import java.util.List;
import java.util.ArrayList;

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
    private final int ENTRADAS_POR_PAGINA = 5;
    private Table rankingTable;
    private Label paginacionLabel;
    
    private List<Texture> avatarTextures; 

    public RankingScreen(Main game) {
        this.game = game;
        this.avatarTextures = new ArrayList<>();
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
            Gdx.app.error("RankingScreen", "Error cargando assets", e);
            skin = new Skin();
        }

        ranking = GestorRanking.getInstancia().leerRanking();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label titleLabel = new Label(game.bundle.get("ran.rankingglobal"), skin, "title");
        titleLabel.setFontScale(0.3f);
        root.add(titleLabel).padTop(5).padBottom(5).row();

        Table containerTable = new Table();
        root.add(containerTable).width(580).height(280).pad(15).row();
        containerTable.add().expandY().row();

        rankingTable = new Table(skin);
        containerTable.add(rankingTable).expandX().fillX().padLeft(20).padRight(20).row();
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
        containerTable.add(regresarBtn).padTop(5).width(100).height(30);

        actualizarTablaRanking();

        anteriorBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paginaActual > 0) {
                    paginaActual--;
                    SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                    actualizarTablaRanking();
                }
            }
        });

        siguienteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ((paginaActual + 1) * ENTRADAS_POR_PAGINA < ranking.size()) {
                    paginaActual++;
                    SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                    actualizarTablaRanking();
                }
            }
        });

        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    private void actualizarTablaRanking() {
        rankingTable.clear();
        
        for (Texture texture : avatarTextures) {
            texture.dispose();
        }
        avatarTextures.clear();

        rankingTable.top();
        rankingTable.defaults().left().padTop(4).padBottom(4);
        
        float nuevaEscala = 0.4f;

        // --- Headers ---
        Label posHeader = new Label("#", skin);
        posHeader.setFontScale(nuevaEscala);
        Label userHeader = new Label(game.bundle.get("ran.usuario"), skin);
        userHeader.setFontScale(nuevaEscala);
        Label scoreHeader = new Label(game.bundle.get("ran.puntuacion"), skin);
        scoreHeader.setFontScale(nuevaEscala);

        rankingTable.add(posHeader).width(50);
        rankingTable.add(userHeader).expandX().colspan(2).padLeft(5); 
        // <<-- CAMBIO: Se reduce el ancho de la columna para un mejor ajuste visual -->>
        rankingTable.add(scoreHeader).width(120).right().padRight(10).row();
        
        rankingTable.add().height(1).colspan(4).growX().padTop(2).padBottom(2).row();

        if (ranking.isEmpty()) {
            Label emptyLabel = new Label(game.bundle.get("ran.aunnohaypuntuacion"), skin);
            emptyLabel.setFontScale(nuevaEscala);
            rankingTable.add(emptyLabel).colspan(4).pad(15).center();
        } else {
            int inicio = paginaActual * ENTRADAS_POR_PAGINA;
            int fin = Math.min(inicio + ENTRADAS_POR_PAGINA, ranking.size());
            
            for (int i = inicio; i < fin; i++) {
                OrdenamientoRanking orden = ranking.get(i);
                
                boolean esJugadorActual = false;
                if (GestorUsuarios.usuarioActual != null) {
                    esJugadorActual = orden.getUsername().equals(GestorUsuarios.usuarioActual.getUsername());
                }
                Color color = esJugadorActual ? Color.YELLOW : Color.WHITE;
                
                String avatarFileName = GestorDatosPerfil.getInstancia().cargarAvatarDeUsuario(orden.getUsername());
                Texture avatarTexture;
                try {
                    avatarTexture = new Texture(Gdx.files.internal("avatares/" + avatarFileName));
                } catch (Exception e) {
                    avatarTexture = new Texture(Gdx.files.internal("avatares/default_avatar.png"));
                }
                avatarTextures.add(avatarTexture);
                Image avatarImage = new Image(avatarTexture);

                Label posLabel = new Label(String.valueOf(i + 1), skin);
                posLabel.setFontScale(nuevaEscala);
                posLabel.setColor(color);
                
                Label userLabel = new Label(orden.getUsername(), skin);
                userLabel.setFontScale(nuevaEscala);
                userLabel.setColor(color);
                
                Label scoreLabel = new Label(String.valueOf(orden.getScore()), skin);
                scoreLabel.setFontScale(nuevaEscala);
                scoreLabel.setColor(color);

                rankingTable.add(posLabel);
                rankingTable.add(avatarImage).size(32).padLeft(5).padRight(2);
                rankingTable.add(userLabel).left();
                rankingTable.add(scoreLabel).right().padRight(10).row();
            }
        }

        int totalPaginas = (int) Math.ceil((double) ranking.size() / ENTRADAS_POR_PAGINA);
        totalPaginas = Math.max(1, totalPaginas);
        
        paginacionLabel.setText(String.format("%s %d %s %d", game.bundle.get("historial.pag"),
                (paginaActual + 1), game.bundle.get("historial.de"), totalPaginas));
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
        if (stage != null) stage.getViewport().update(width, height, true);
        if (backgroundViewport != null) backgroundViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose();
        
        for (Texture texture : avatarTextures) {
            texture.dispose();
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}