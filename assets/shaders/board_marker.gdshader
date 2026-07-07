// board_marker.gdshader
// Shader para os marcadores do tabuleiro (anéis dos nós e linhas das arestas).
// Renderiza sem iluminação (unshaded) com emissão para efeito de brilho.

shader_type spatial;
render_mode unshaded, depth_draw_opaque, cull_disabled;

// Cor principal do marcador
uniform vec4 albedo_color : source_color = vec4(1.0, 1.0, 1.0, 1.0);
// Intensidade do brilho/emissão (1.0 = sem brilho extra, 2.0+ = brilha)
uniform float emission_strength : hint_range(0.5, 4.0) = 1.5;
// Transparência global
uniform float alpha : hint_range(0.0, 1.0) = 1.0;

void fragment() {
    ALBEDO   = albedo_color.rgb * emission_strength;
    EMISSION = albedo_color.rgb * (emission_strength - 1.0);
    ALPHA    = albedo_color.a * alpha;
}
