package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Pessoa cadastrada no Desconfie (tabela tb_dsf_usuario).
 * Um usuário faz análises (1:N) e pode ser guardião de até 3 dependentes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long id;               // cd_usuario (gerado pela sequence sq_tb_dsf_usuario)
    private String nome;           // nm_usuario
    private String email;          // ds_email (único)
    private String senha;          // ds_senha (nunca devolvida pela API)
    private String telefone;       // nr_telefone (opcional)
    private String status;         // st_usuario: A = ativo, I = inativo
    private LocalDate dataCadastro; // dt_cadastro
}
