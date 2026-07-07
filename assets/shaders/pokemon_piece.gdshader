// pokemon_piece.gdshader
// Shader 2.5D para peças Pokémon no tabuleiro 3D.
// Y-Billboard: sprite mantém-se vertical e vira para a câmera (estilo clássico 2.5D).
// Filtro nearest para sprites pixel-crisp; suporte a outline de seleção.

shader_type spatial;
render_mode unshaded, depth_draw_opaque, cull_disabled, depth_prepass_alpha;

// Textura do sprite (filtro nearest para visual retro-crisp)
uniform sampler2D albedo_tex : source_color, filter_nearest;
// Limiar de alpha — pixels abaixo deste valor são descartados (recorte)
uniform float alpha_threshold : hint_range(0.0, 1.0) = 0.08;
// Cor de tint (branco = sem tint)
uniform vec4 tint_color : source_color = vec4(1.0, 1.0, 1.0, 1.0);
// Ativo quando a figura está selecionada
uniform bool selected = false;
// Cor do outline de seleção
uniform vec4 select_color : source_color = vec4(1.0, 0.9, 0.0, 1.0);
// Espessura do outline em coordenadas UV (0.02–0.06 é um bom range)
uniform float outline_size : hint_range(0.01, 0.08) = 0.03;

void vertex() {
    // Y-Billboard: projeta o sprite no plano frontal à câmera, mantendo eixo Y vertical.
    // Equivalente ao BILLBOARD_FIXED_Y do Sprite3D, mas em ShaderMaterial —
    // permite que apliquemos outline de seleção e alpha-cutout juntos.
    MODELVIEW_MATRIX = VIEW_MATRIX * mat4(
        vec4(normalize(cross(vec3(0.0, 1.0, 0.0), INV_VIEW_MATRIX[2].xyz)), 0.0),
        vec4(0.0, 1.0, 0.0, 0.0),
        vec4(normalize(cross(INV_VIEW_MATRIX[0].xyz, vec3(0.0, 1.0, 0.0))), 0.0),
        MODEL_MATRIX[3]
    );
    MODELVIEW_NORMAL_MATRIX = mat3(MODELVIEW_MATRIX);
}

void fragment() {
    vec4 tex = texture(albedo_tex, UV);

    if (tex.a < alpha_threshold) {
        // Outline de seleção: se o pixel é transparente mas vizinho a um pixel opaco,
        // pinta com a cor de seleção (amarelo).
        if (selected) {
            float n = texture(albedo_tex, UV + vec2(0.0,  outline_size)).a;
            float s = texture(albedo_tex, UV + vec2(0.0, -outline_size)).a;
            float e = texture(albedo_tex, UV + vec2( outline_size, 0.0)).a;
            float w = texture(albedo_tex, UV + vec2(-outline_size, 0.0)).a;
            if (max(max(n, s), max(e, w)) >= alpha_threshold) {
                ALBEDO = select_color.rgb;
                ALPHA  = 1.0;
                return;
            }
        }
        discard;
    }

    ALBEDO = tex.rgb * tint_color.rgb;
    ALPHA  = tex.a;
}
