package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Um campo inválido dentro do erro 400. Ex.: { "campo": "email", "mensagem": "E-mail inválido" }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampoErro {
    private String campo;
    private String mensagem;
}
