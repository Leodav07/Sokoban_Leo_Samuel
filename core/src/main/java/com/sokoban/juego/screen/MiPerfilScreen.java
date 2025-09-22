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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.accounts.GestorProgreso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MiPerfilScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;
    private Texture avatarTexture;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public MiPerfilScreen(Main game) {
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
            Gdx.app.error("MiPerfilScreen", "Error cargando assets", e);
            skin = new Skin(); 
        }

        GestorDatosPerfil.DatosPerfil datosPerfil = GestorDatosPerfil.getInstancia().cargarDatosPerfil();
        GestorProgreso progreso = GestorProgreso.getInstancia();

        try {
            avatarTexture = new Texture(Gdx.files.internal("avatares/" + datosPerfil.avatar));
        } catch (Exception e) {
            avatarTexture = new Texture(Gdx.files.internal("avatares/default_avatar.png"));
        }

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        //  Título Principal 
        Label titleLabel = new Label("MI PERFIL", skin, "title");
        titleLabel.setFontScale(0.3f);
        root.add(titleLabel).padTop(25).padBottom(10).row();

        // Tabla de Información Principal
        Table mainInfoTable = new Table();
        
        Image avatarImage = new Image(avatarTexture);
        mainInfoTable.add(avatarImage).size(80).padRight(20);

        Table personalDataTable = new Table();
        personalDataTable.left(); 
        
        Label userLabel = new Label("Usuario:", skin, "subtitle");
        userLabel.setFontScale(0.5f);
        Label userData = new Label(GestorUsuarios.usuarioActual.getUsername(), skin, "default");
        userData.setFontScale(0.5f);
        personalDataTable.add(userLabel).padRight(10);
        personalDataTable.add(userData).left().row();
        
        Label nameLabel = new Label("Nombre:", skin, "subtitle");
        nameLabel.setFontScale(0.5f);
        Label nameData = new Label(GestorUsuarios.usuarioActual.getNombreCompleto(), skin, "default");
        nameData.setFontScale(0.5f);
        personalDataTable.add(nameLabel).padRight(10).padTop(4);
        personalDataTable.add(nameData).left().padTop(4).row();

        Label regLabel = new Label("Registro:", skin, "subtitle");
        regLabel.setFontScale(0.5f);
        Label regData = new Label(dateFormat.format(GestorUsuarios.usuarioActual.getFechaRegistro().getTime()), skin, "default");
        regData.setFontScale(0.5f);
        personalDataTable.add(regLabel).padRight(10).padTop(4);
        personalDataTable.add(regData).left().padTop(4).row();
        
        Label sessionLabel = new Label("Ultima Sesion:", skin, "subtitle");
        sessionLabel.setFontScale(0.5f);
        Label sessionData = new Label(datosPerfil.ultimaSesion == 0 ? "Nunca" : dateFormat.format(new Date(datosPerfil.ultimaSesion)), skin, "default");
        sessionData.setFontScale(0.5f);
        personalDataTable.add(sessionLabel).padRight(10).padTop(4);
        personalDataTable.add(sessionData).left().padTop(4).row();

        mainInfoTable.add(personalDataTable);
        root.add(mainInfoTable).padBottom(15).row();

        //Tabla de Estadísticas
        Table statsTable = new Table();
        statsTable.defaults().pad(2).left();

        Label progressTitle = new Label("PROGRESO", skin, "title");
        progressTitle.setFontScale(0.3f);
        statsTable.add(progressTitle).colspan(2).padBottom(8).center().row();
        
        //Datos de Progreso 
        Label levelsLabel = new Label("Niveles Completados:", skin, "default");
        levelsLabel.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(levelsLabel);

        Label levelsData = new Label(progreso.getNivelesCompletados() + " / " + 7, skin, "default");
        levelsData.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(levelsData).padLeft(15).row();
        
        Label scoreLabel = new Label("Puntaje Total:", skin, "default");
        scoreLabel.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(scoreLabel);

        Label scoreData = new Label(String.valueOf(progreso.getPuntajeTotalAcumulado()), skin, "default");
        scoreData.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(scoreData).padLeft(15).row();
        
        Label timeLabel = new Label("Tiempo Jugado:", skin, "default");
        timeLabel.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(timeLabel);

        Label timeData = new Label(formatearTiempo(progreso.getTiempoTotalJugado()), skin, "default");
        timeData.setFontScale(0.5f); // <-- Ajuste de tamaño
        statsTable.add(timeData).padLeft(15).row();
        
        root.add(statsTable).padBottom(20).row();

        //  Tabla de Botones 
        Table buttonTable = new Table();
        TextButton cambiarAvatarBtn = new TextButton("Cambiar Avatar", skin);
        TextButton historialBtn = new TextButton("Historial", skin);
        TextButton regresarBtn = new TextButton("Regresar", skin);

        cambiarAvatarBtn.getLabel().setFontScale(0.3f);
        historialBtn.getLabel().setFontScale(0.3f);
        regresarBtn.getLabel().setFontScale(0.3f);

        buttonTable.add(cambiarAvatarBtn).width(160).height(40).pad(5);
        buttonTable.add(historialBtn).width(160).height(40).pad(5);
        
        root.add(buttonTable).row();
        root.add(regresarBtn).width(160).height(40).padTop(10);

        cambiarAvatarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
               game.setScreen(new CortinaTransicion(game, MiPerfilScreen.this, new AvatarSeleccionScreen(game)));
               
            }
        });
        
        historialBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
               game.setScreen(new CortinaTransicion(game, MiPerfilScreen.this, new HistorialPartidasScreen(game)));
                
            }
        });

        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new CortinaTransicion(game, MiPerfilScreen.this, new MenuScreen(game)));
            }
        });
    }

    private String formatearTiempo(long tiempoMs) {
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        minutos %= 60;
        return String.format("%02d h %02d min", horas, minutos);
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
        if (avatarTexture != null) avatarTexture.dispose();
    }
    
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}