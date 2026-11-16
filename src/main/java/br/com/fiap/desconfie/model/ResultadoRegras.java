package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado do motor de regras: a nota (0 a 100) e o que foi encontrado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoRegras {
    private int nota;
    private List<PadraoEncontrado> padroes = new ArrayList<>();
    private List<SinalLink> sinais = new ArrayList<>();
    private boolean dominioOficial;   // link de um site oficial conhecido (nota final limitada a 29)
}
