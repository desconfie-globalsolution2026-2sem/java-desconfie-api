package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.enums.TipoPadrao;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import br.com.fiap.desconfie.model.PadraoGolpe;
import br.com.fiap.desconfie.model.ResultadoRegras;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários do motor de regras, com os mesmos padrões do catálogo do banco.
 * Não precisam do Oracle nem do Spring: o motor recebe a lista de padrões pronta.
 */
class MotorRegrasServiceTest {

    private final MotorRegrasService motor = new MotorRegrasService();

    private static final CategoriaGolpe FALSO_PARENTE = new CategoriaGolpe(1L, "Falso parente", "", "");
    private static final CategoriaGolpe PEDIDO_DINHEIRO = new CategoriaGolpe(2L, "Pedido de dinheiro", "", "");
    private static final CategoriaGolpe URGENCIA = new CategoriaGolpe(3L, "Urgência e ameaça", "", "");
    private static final CategoriaGolpe PHISHING = new CategoriaGolpe(4L, "Phishing de link", "", "");

    private static final List<PadraoGolpe> PADROES = List.of(
            padrao(1, FALSO_PARENTE, TipoPadrao.TEXTO, "troquei de numero", 35),
            padrao(2, PEDIDO_DINHEIRO, TipoPadrao.TEXTO, "preciso de", 10),
            padrao(3, PEDIDO_DINHEIRO, TipoPadrao.TEXTO, "pix", 15),
            padrao(4, URGENCIA, TipoPadrao.TEXTO, "urgente", 20),
            padrao(5, PHISHING, TipoPadrao.TLD, ".xyz", 25),
            padrao(6, PHISHING, TipoPadrao.ENCURTADOR, "bit.ly", 20),
            padrao(7, PHISHING, TipoPadrao.MARCA, "itau", 35),
            padrao(8, null, TipoPadrao.DOMINIO_OFICIAL, "itau.com.br", 0));

    private static PadraoGolpe padrao(long id, CategoriaGolpe categoria, TipoPadrao tipo, String termo, int peso) {
        return new PadraoGolpe(id, categoria, tipo, termo, peso, true);
    }

    @Test
    void exemploFalsoParenteSoma80() {
        ResultadoRegras r = motor.analisarTexto("Mãe, troquei de número. Preciso de um Pix urgente de R$ 800.", PADROES);
        // troquei de numero (35) + preciso de (10) + pix (15) + urgente (20)
        assertEquals(80, r.getNota());
        assertEquals(4, r.getPadroes().size());
    }

    @Test
    void padraoRepetidoSomaUmaVezSo() {
        ResultadoRegras r = motor.analisarTexto("urgente urgente URGENTE!!!", PADROES);
        assertEquals(20, r.getNota());
        assertEquals(3, r.getPadroes().get(0).getOcorrencias());
    }

    @Test
    void palavraDentroDeOutraNaoConta() {
        // "pix" não deve ser encontrado dentro de "pixel"
        assertEquals(0, motor.analisarTexto("Comprei uma câmera de 12 megapixel", PADROES).getNota());
    }

    @Test
    void mensagemNormalFicaVerde() {
        assertEquals(0, motor.analisarTexto("Oi! Chego em casa às 19h, quer que eu compre pão?", PADROES).getNota());
    }

    @Test
    void linkOficialNaoPontuaEMarcaComoOficial() {
        ResultadoRegras r = motor.analisarLink("https://www.itau.com.br/cartoes", PADROES);
        assertTrue(r.isDominioOficial());
        assertEquals(0, r.getNota());
    }

    @Test
    void linkEncurtado() {
        ResultadoRegras r = motor.analisarLink("https://bit.ly/3abcXYZ", PADROES);
        assertEquals(20, r.getNota());
        assertFalse(r.isDominioOficial());
    }

    @Test
    void marcaImitadaComTldDeRiscoESemHttps() {
        ResultadoRegras r = motor.analisarLink("http://itau-seguranca-app.xyz/atualizar", PADROES);
        // marca itau (35) + TLD .xyz (25) + sem HTTPS (10)
        assertEquals(70, r.getNota());
        assertEquals(2, r.getPadroes().size());
        assertEquals(1, r.getSinais().size());
    }

    @Test
    void linkComIpEArroba() {
        ResultadoRegras r = motor.analisarLink("http://www.itau.com.br@192.168.10.5/login", PADROES);
        // @ (25) + sem HTTPS (10) + IP (30); o host real é o IP, não o site do banco
        assertEquals(65, r.getNota());
        assertFalse(r.isDominioOficial());
    }

    @Test
    void linkInvalidoGeraErro() {
        assertThrows(IllegalArgumentException.class, () -> motor.analisarLink("http://", PADROES));
    }
}
