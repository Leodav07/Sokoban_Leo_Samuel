#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_progress;
uniform vec2 u_resolution; // Resolución de la pantalla

void main() {
    vec2 uv = v_texCoords;
    
    // Convertir coordenadas UV a coordenadas de píxel
    vec2 pixelCoords = uv * u_resolution;
    
    // Crear el patrón de círculos concéntricos como en Mario Bros 3
    // El centro está en el medio de la pantalla
    vec2 center = u_resolution * 0.5;
    float distanceFromCenter = distance(pixelCoords, center);
    
    // Radio máximo desde el centro hasta las esquinas
    float maxRadius = length(u_resolution * 0.5) * 1.414; // sqrt(2) para cubrir las esquinas
    
    // INVERTIDO: El círculo se CIERRA (iris in) - empieza grande y se hace pequeño
    float cutoffRadius = (1.0 - u_progress) * maxRadius;
    
    // Crear el efecto de "iris in" (círculo que se cierra desde afuera hacia adentro)
    if (distanceFromCenter < cutoffRadius) {
        // Dentro del círculo que se achica = pantalla actual (sin blur)
        gl_FragColor = texture2D(u_texture, uv);
    } else {
        // Fuera del círculo = negro (pantalla siguiente)
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}