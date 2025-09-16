// Este código se ejecuta en la tarjeta gráfica (GPU)
#ifdef GL_ES
precision mediump float;
#endif

// 'varying' son variables que vienen del procesador de vértices
varying vec4 v_color;
varying vec2 v_texCoords;

// 'uniform' son variables que le pasamos desde nuestro código Java
uniform sampler2D u_texture; // La imagen original de la pantalla del juego
uniform float u_progress;    // El progreso de la transición (un número de 0.0 a 1.0)

void main() {
    // Las coordenadas de la textura van de 0.0 a 1.0 en X e Y.
    // La suma de X + Y crea un valor que es pequeño en la esquina inferior-izquierda
    // y grande en la superior-derecha, formando una diagonal.
    float diagonalValue = v_texCoords.x + v_texCoords.y;

    // El umbral del barrido se mueve a lo largo de esa diagonal.
    // Multiplicamos por 2.0 para que el barrido cubra toda la pantalla cuando el progreso sea 1.0.
    float threshold = u_progress * 2.0;

    // Si el valor diagonal de este píxel es menor que el umbral del barrido...
    if (diagonalValue < threshold) {
        // ...significa que el barrido ya pasó por aquí. Píntalo de negro.
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        // ...de lo contrario, dibuja el píxel original de la pantalla del juego.
        gl_FragColor = texture2D(u_texture, v_texCoords);
    }
}