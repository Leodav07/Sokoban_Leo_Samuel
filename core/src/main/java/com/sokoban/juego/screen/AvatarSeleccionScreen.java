package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.SoundManager;
import java.util.ArrayList;
import java.util.List;

public class AvatarSeleccionScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private SpriteBatch backgroundBatch;
    private OrthographicCamera backgroundCamera;
    private FitViewport backgroundViewport;
    private final List<Texture> avatarTextures = new ArrayList<>();

    // Sistema de diálogo personalizado
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private BitmapFont dialogFont;
    private String mensajeDialog = "";
    private boolean mostrarDialog = false;
    private final GlyphLayout layout = new GlyphLayout();
    private float dialogAlpha = 0f;
    private float dialogScale = 0.5f;
    private final OrthographicCamera dialogCamera;
    private final ScreenViewport dialogViewport;
    
    // Variables para manejar la confirmación
    private boolean esperandoConfirmacion = false;
    private String avatarSeleccionado;
    private Table tablaConfirmacion;

    private final String[] avatares = {
        "default_avatar.png", "avatarejemplo.png", "mario.png", "luigi.png", 
        "peach.png", "toad.png", "yoshi.png", "bowser.png", "wario.png", "goomba.png"
    };

    public AvatarSeleccionScreen(Main game) {
        this.game = game;
        this.dialogCamera = new OrthographicCamera();
        this.dialogViewport = new ScreenViewport(dialogCamera);
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        
        try {
            // CORREGIDO: Cargar skin con atlas como en las otras pantallas
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
            
            dialogBatch = new SpriteBatch();
            cuadroTexture = new Texture("skin/cuadro.png");
            dialogFont = skin.getFont("default-font");
            
        } catch (Exception e) {
            Gdx.app.error("AvatarScreen", "Error cargando assets", e);
            skin = new Skin();
        }

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titulo = new Label("SELECCIONA UN AVATAR", skin, "title");
        titulo.setFontScale(0.5f);
        root.add(titulo).padBottom(20).row();
        
        Table avatarGrid = new Table();
        int colCount = 0;
        for (final String avatarName : avatares) {
            try {
                Texture avatarTex = new Texture(Gdx.files.internal("avatares/" + avatarName));
                // Aplicar filtrado nearest neighbor a los avatares también
                avatarTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                avatarTextures.add(avatarTex);
                
                Image avatarImage = new Image(avatarTex);
                avatarImage.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (!esperandoConfirmacion) {
                            confirmarSeleccion(avatarName);
                        }
                    }
                });
                avatarGrid.add(avatarImage).size(96).pad(10);
                colCount++;
                if (colCount >= 4) {
                    avatarGrid.row();
                    colCount = 0;
                }
            } catch (Exception e) {
                // Avatar no encontrado, continuar con el siguiente
            }
        }

        root.add(avatarGrid).expand().row();
        
        TextButton regresarBtn = new TextButton("Regresar", skin);
        regresarBtn.getLabel().setFontScale(0.7f);
        regresarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.setScreen(new CortinaTransicion(game, AvatarSeleccionScreen.this, new MiPerfilScreen(game)));
            }
        });
        root.add(regresarBtn).padTop(20);
    }
    
    private void confirmarSeleccion(final String avatarName) {
        esperandoConfirmacion = true;
        avatarSeleccionado = avatarName;
        
        tablaConfirmacion = new Table();
        tablaConfirmacion.setFillParent(true);
        
        TextButton siButton = new TextButton("Si", skin);
        TextButton noButton = new TextButton("No", skin);
        
        // Aplicar efectos hover como en otras pantallas
        addButtonEffects(siButton, Color.GREEN);
        addButtonEffects(noButton, Color.RED);
        
        Table buttonTable = new Table();
        buttonTable.add(siButton).width(100).height(40).pad(20);
        buttonTable.add(noButton).width(100).height(40).pad(20);

        tablaConfirmacion.add(buttonTable).expand().bottom().padBottom(50);
        stage.addActor(tablaConfirmacion);
        
        siButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                GestorDatosPerfil.getInstancia().guardarAvatar(avatarSeleccionado);
                game.setScreen(new CortinaTransicion(game, AvatarSeleccionScreen.this, new MiPerfilScreen(game)));
                
            }
        });

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play(SoundManager.SoundEffect.SELECCION_MENU);
                esperandoConfirmacion = false;
                tablaConfirmacion.remove(); 
                ocultarDialog(); 
            }
        });

        mostrarDialog("¿Quieres seleccionar este avatar?");
    }
    
    // Método para efectos de botones (copiado de MenuScreen)
    private void addButtonEffects(TextButton button, Color hoverColor) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.pow2Out));
                button.setColor(hoverColor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                button.setColor(Color.WHITE);
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Dibujar fondo
        if (backgroundTexture != null && backgroundBatch != null) {
            backgroundViewport.apply();
            backgroundCamera.update();
            backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
            backgroundBatch.begin();
            backgroundBatch.draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
            backgroundBatch.end();
        }
        
        // Dibujar UI
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();

        // Dibujar diálogo si es necesario
        if (mostrarDialog) {
            dialogViewport.apply();
            renderDialog();
        }
    }
    
    private void renderDialog() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        dialogFont.getData().setScale(dialogScale * 0.8f);
        layout.setText(dialogFont, mensajeDialog);
        
        float dialogWidth = layout.width + 80 * dialogScale;
        float dialogHeight = layout.height + 60 * dialogScale;
        if (esperandoConfirmacion) {
            dialogHeight += 50; 
        }

        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;

        dialogBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        dialogBatch.begin();
        dialogBatch.setColor(1, 1, 1, dialogAlpha);
        dialogBatch.draw(cuadroTexture, dialogX, dialogY, dialogWidth, dialogHeight);

        float textX = dialogX + (dialogWidth - layout.width) / 2;
        float textY = dialogY + (dialogHeight + layout.height) / 2 + (esperandoConfirmacion ? 20 : 0);
        
        dialogFont.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, dialogAlpha);
        dialogFont.draw(dialogBatch, layout, textX, textY);
        
        dialogBatch.setColor(Color.WHITE);
        dialogBatch.end();
    }
    
    private void mostrarDialog(String mensaje) {
        this.mensajeDialog = mensaje;
        this.mostrarDialog = true;
        this.dialogAlpha = 0f;
        this.dialogScale = 0.5f;
        animateDialog(true);
    }

    private void ocultarDialog() {
        animateDialog(false);
    }

    private void animateDialog(boolean show) {
        if (show) {
            Timer.schedule(new Timer.Task() {
                float time = 0f;
                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.3f) {
                        dialogAlpha = 1f;
                        dialogScale = 1f;
                        this.cancel();
                        return;
                    }
                    dialogAlpha = Interpolation.pow2Out.apply(time / 0.3f);
                    dialogScale = 0.5f + (0.5f * Interpolation.bounceOut.apply(time / 0.3f));
                }
            }, 0f, 0.02f);
        } else {
            Timer.schedule(new Timer.Task() {
                float time = 0f;
                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.2f) {
                        mostrarDialog = false;
                        mensajeDialog = "";
                        esperandoConfirmacion = false;
                        if (tablaConfirmacion != null) tablaConfirmacion.remove();
                        this.cancel();
                        return;
                    }
                    dialogAlpha = 1f - (time / 0.2f);
                    dialogScale = 1f - (0.3f * (time / 0.2f));
                }
            }, 0f, 0.02f);
        }
    }
    
    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
        if (backgroundViewport != null) backgroundViewport.update(width, height, true);
        if (dialogViewport != null) dialogViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backgroundBatch != null) backgroundBatch.dispose();
        if (dialogBatch != null) dialogBatch.dispose();
        if (cuadroTexture != null) cuadroTexture.dispose();
        for (Texture tex : avatarTextures) {
            if (tex != null) tex.dispose();
        }
    }
    
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}