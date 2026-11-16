package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sinal de risco de um link que vem de uma regra fixa do código (IP, @, sem HTTPS...),
 * e não de um padrão cadastrado no banco. Não é gravado: é recalculado a partir da URL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SinalLink {
    private String descricao;
    private int peso;
}
