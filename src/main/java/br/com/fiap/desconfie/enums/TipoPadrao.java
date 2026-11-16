package br.com.fiap.desconfie.enums;

/**
 * Tipos de padrão usados pelo motor de regras.
 * TEXTO: frase ou palavra procurada em mensagens.
 * TLD: final de domínio de risco (ex.: .xyz). ENCURTADOR: encurtador de link (ex.: bit.ly).
 * MARCA: nome de marca imitado em links falsos. DOMINIO_OFICIAL: site verdadeiro de uma empresa.
 */
public enum TipoPadrao {
    TEXTO, TLD, ENCURTADOR, MARCA, DOMINIO_OFICIAL
}
