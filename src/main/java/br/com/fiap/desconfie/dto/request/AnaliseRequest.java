package br.com.fiap.desconfie.dto.request;

import br.com.fiap.desconfie.enums.TipoEntrada;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pedido de análise. TEXTO e LINK usam "conteudo"; IMAGEM usa "imagemBase64".
 * A obrigatoriedade de cada campo conforme o tipo é conferida no AnaliseService (400).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseRequest {

    @NotNull(message = "O tipo é obrigatório (TEXTO, IMAGEM ou LINK)")
    private TipoEntrada tipo;

    @Size(max = 2000, message = "O conteúdo deve ter no máximo 2.000 caracteres")
    private String conteudo;

    // cerca de 2 MB de imagem viram 2,8 milhões de caracteres em Base64
    @Size(max = 2_800_000, message = "A imagem deve ter no máximo 2 MB")
    private String imagemBase64;
}
