package com.sokoban.juego.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorConfiguracion;
import com.sokoban.juego.logica.GestorUsuarios;

import java.io.IOException;
import java.util.Locale;

public class ConfiguracionScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin; // <<--- Usaremos la skin estándar

    // UI Elements
    private Slider volumenSlider;
    private SelectBox<String> idiomaSelectBox;

    public ConfiguracionScreen(Main game) {
        this.game = game;
        // Comprobación de seguridad
        if (GestorUsuarios.usuarioActual == null) {
            Gdx.app.error("ConfiguracionScreen", "Usuario actual es null. Redirigiendo a LoginScreen.");
            // No podemos cambiar de pantalla en el constructor, pero prevenimos la ejecución.
        }
    }

    @Override
    public void show() {
        // Redirección si el usuario no está logueado
        if (GestorUsuarios.usuarioActual == null) {
            game.setScreen(new LoginScreen(game));
            return;
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // <<--- CAMBIO PRINCIPAL: Usamos la skin por defecto 'uiskin.json' --->>
        try {
            skin = new Skin(Gdx.files.internal("uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionScreen", "No se encontró 'uiskin.json'. Asegúrate de que esté en tu carpeta 'assets'.", e);
            // Si incluso la skin por defecto falla, creamos una vacía para evitar crasheo total.
            skin = new Skin();
        }
        
        createUI();
    }
    
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        // La lógica de cargar la configuración no cambia
        Object[] config = GestorConfiguracion.getInstancia().cargarConfiguracion();
        float volumen = (Float) config[0];
        String idioma = (String) config[1];

        // Usamos los estilos por defecto de la skin
        Label titleLabel = new Label("CONFIGURACION", skin, "title-font", "white");
        
        volumenSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumenSlider.setValue(volumen);

        idiomaSelectBox = new SelectBox<>(skin);
        idiomaSelectBox.setItems("es", "en", "fr");
        idiomaSelectBox.setSelected(idioma);

        TextButton guardarButton = new TextButton("GUARDAR", skin);
        TextButton volverButton = new TextButton("VOLVER", skin);

        table.add(titleLabel).colspan(2).padBottom(40);
        table.row();
        table.add(new Label("Volumen", skin)).pad(10);
        table.add(volumenSlider).width(300).height(30).pad(10);
        table.row();
        table.add(new Label("Idioma", skin)).pad(10);
        table.add(idiomaSelectBox).width(300).height(50).pad(10);
        table.row().padTop(30);
        table.add(guardarButton).width(200).height(50).pad(10);
        table.add(volverButton).width(200).height(50).pad(10);

        guardarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleGuardar();
            }
        });

        volverButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Volvemos al menú principal (podemos usar la cortina si quieres, pero por simplicidad la quitamos)
                game.setScreen(new MenuScreen(game));
                dispose(); // Liberamos los recursos de esta pantalla
            }
        });
    }
    
    private void handleGuardar() {
        // <<--- CAMBIO: Usamos el diálogo estándar de la skin --->>
        mostrarDialogoSimple("Guardando", "Guardando cambios...");
        
        new Thread(() -> {
            try {
                // La lógica de guardado no cambia
                GestorConfiguracion.getInstancia().guardarConfiguracion(
                        volumenSlider.getValue(),
                        idiomaSelectBox.getSelected()
                );

                Locale locale = new Locale(idiomaSelectBox.getSelected());
                game.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
                game.setVolumen(volumenSlider.getValue());
                
                Gdx.app.postRunnable(() -> mostrarDialogoSimple("Éxito", "¡Cambios guardados!"));

            } catch (IOException e) {
                Gdx.app.postRunnable(() -> mostrarDialogoSimple("Error", "No se pudieron guardar los cambios."));
            }
        }).start();
    }
    
    // <<--- NUEVO MÉTODO: Un diálogo simple usando la skin estándar --->>
    private void mostrarDialogoSimple(String titulo, String mensaje) {
        Dialog dialog = new Dialog(titulo, skin);
        dialog.text(mensaje);
        dialog.button("Aceptar");
        dialog.show(stage);
    }

    @Override
    public void render(float delta) {
        // <<--- SIMPLIFICADO: Solo limpiamos la pantalla y dibujamos el stage --->>
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f); // Un fondo gris oscuro
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
    }

    // Métodos no utilizados
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
}