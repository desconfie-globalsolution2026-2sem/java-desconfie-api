-- =====================================================================
-- DESCONFIE · Global Solution 2026/2 · CitizenTech
-- Disciplina: Building Relational Database · FIAP · Turma 1TDSPJ
-- Integrantes: Juan Souza Marques (RM 573469), Lucas Leite Carlos (RM 571985),
--              Matheus Matsushita de Souza (RM 570017), Pedro Amaro Pires (RM 570636)
-- Data: 17/11/2026
-- Objetivo: script completo de entrega do banco do Desconfie, sistema que
--           avalia se mensagens, prints e links são golpe e avisa o
--           círculo de proteção da pessoa.
-- Ordem de execução (rodar inteiro com F5 no SQL Developer):
--   1. Limpeza e criação: remove tabelas e sequences antigas, se existirem, e cria sequences, 7 tabelas (PK, FK, NOT NULL, UNIQUE, CHECK) e comentários
--   2. Inclusão: 76 INSERTs e a contagem por tabela como evidência
--   3. Consultas: 10 consultas numeradas
--   4. Atualização e exclusão: um UPDATE e um DELETE, ambos com WHERE
-- =====================================================================


-- =====================================================================
-- 1. LIMPEZA E CRIAÇÃO DAS TABELAS
-- =====================================================================
-- ---------------------------------------------------------------------
-- LIMPEZA: apaga as tabelas e sequences antigas, se existirem, para o
-- script poder ser executado de novo sem erro (ordem não importa por
-- causa do CASCADE CONSTRAINTS)
-- ---------------------------------------------------------------------
BEGIN
    FOR t IN (SELECT table_name FROM user_tables
               WHERE table_name IN ('TB_DSF_ALERTA', 'TB_DSF_ANALISE_PADRAO', 'TB_DSF_ANALISE',
                                    'TB_DSF_PADRAO_GOLPE', 'TB_DSF_CATEGORIA_GOLPE',
                                    'TB_DSF_VINCULO_GUARDIAO', 'TB_DSF_USUARIO')) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;
    FOR s IN (SELECT sequence_name FROM user_sequences
               WHERE sequence_name LIKE 'SQ_TB_DSF_%') LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/

-- ---------------------------------------------------------------------
-- SEQUENCES (uma por tabela de entidade; associativas usam PK composta)
-- ---------------------------------------------------------------------
CREATE SEQUENCE sq_tb_dsf_usuario         START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_dsf_categoria_golpe START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_dsf_padrao_golpe    START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_dsf_analise         START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_dsf_alerta          START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- 1. USUÁRIO
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_usuario (
    cd_usuario     NUMBER(10)    NOT NULL,
    nm_usuario     VARCHAR2(80)  NOT NULL,
    ds_email       VARCHAR2(120) NOT NULL,
    ds_senha       VARCHAR2(60)  NOT NULL,
    nr_telefone    VARCHAR2(20),
    st_usuario     CHAR(1)       DEFAULT 'A' NOT NULL,
    dt_cadastro    DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_dsf_usuario        PRIMARY KEY (cd_usuario),
    CONSTRAINT uk_dsf_usuario_email  UNIQUE (ds_email),
    CONSTRAINT ck_dsf_usuario_status CHECK (st_usuario IN ('A', 'I'))
);

-- ---------------------------------------------------------------------
-- 2. VÍNCULO GUARDIÃO (autorrelacionamento usuário × usuário)
--    Cada dependente tem no máximo um guardião (UNIQUE em cd_dependente),
--    o que torna o vínculo um 1:N guardião → dependentes.
--    Regra "máximo 3 dependentes por guardião" é validada na API Java.
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_vinculo_guardiao (
    cd_guardiao    NUMBER(10) NOT NULL,
    cd_dependente  NUMBER(10) NOT NULL,
    dt_vinculo     DATE       DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_dsf_vinculo PRIMARY KEY (cd_guardiao, cd_dependente),
    CONSTRAINT uk_dsf_vinculo_dependente UNIQUE (cd_dependente),
    CONSTRAINT fk_dsf_vinculo_guardiao   FOREIGN KEY (cd_guardiao)
        REFERENCES tb_dsf_usuario (cd_usuario),
    CONSTRAINT fk_dsf_vinculo_dependente FOREIGN KEY (cd_dependente)
        REFERENCES tb_dsf_usuario (cd_usuario),
    CONSTRAINT ck_dsf_vinculo_proprio CHECK (cd_guardiao <> cd_dependente)
);

-- ---------------------------------------------------------------------
-- 3. CATEGORIA DE GOLPE
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_categoria_golpe (
    cd_categoria   NUMBER(10)    NOT NULL,
    nm_categoria   VARCHAR2(60)  NOT NULL,
    ds_categoria   VARCHAR2(300) NOT NULL,
    ds_orientacao  VARCHAR2(400) NOT NULL,
    CONSTRAINT pk_dsf_categoria      PRIMARY KEY (cd_categoria),
    CONSTRAINT uk_dsf_categoria_nome UNIQUE (nm_categoria)
);

-- ---------------------------------------------------------------------
-- 4. PADRÃO DE GOLPE (1:N categoria → padrão)
--    DOMINIO_OFICIAL não pertence a categoria de golpe (CHECK abaixo).
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_padrao_golpe (
    cd_padrao      NUMBER(10)    NOT NULL,
    cd_categoria   NUMBER(10),
    tp_padrao      VARCHAR2(20)  NOT NULL,
    ds_termo       VARCHAR2(120) NOT NULL,
    nr_peso        NUMBER(3)     NOT NULL,
    st_ativo       CHAR(1)       DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_dsf_padrao PRIMARY KEY (cd_padrao),
    CONSTRAINT fk_dsf_padrao_categoria FOREIGN KEY (cd_categoria)
        REFERENCES tb_dsf_categoria_golpe (cd_categoria),
    CONSTRAINT uk_dsf_padrao_termo UNIQUE (tp_padrao, ds_termo),
    CONSTRAINT ck_dsf_padrao_tipo
        CHECK (tp_padrao IN ('TEXTO', 'TLD', 'ENCURTADOR', 'MARCA', 'DOMINIO_OFICIAL')),
    CONSTRAINT ck_dsf_padrao_peso  CHECK (nr_peso BETWEEN 0 AND 50),
    CONSTRAINT ck_dsf_padrao_ativo CHECK (st_ativo IN ('S', 'N')),
    CONSTRAINT ck_dsf_padrao_categoria CHECK (
        (tp_padrao = 'DOMINIO_OFICIAL' AND cd_categoria IS NULL)
        OR (tp_padrao <> 'DOMINIO_OFICIAL' AND cd_categoria IS NOT NULL)
    )
);

-- ---------------------------------------------------------------------
-- 5. ANÁLISE (1:N usuário → análise)
--    nr_nota_ia nulo = IA indisponível no momento da análise.
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_analise (
    cd_analise      NUMBER(10)     NOT NULL,
    cd_usuario      NUMBER(10)     NOT NULL,
    tp_entrada      VARCHAR2(10)   NOT NULL,
    ds_conteudo     VARCHAR2(2000) NOT NULL,
    nr_nota_regras  NUMBER(5,2)    NOT NULL,
    nr_nota_ia      NUMBER(5,2),
    nr_nota_final   NUMBER(5,2)    NOT NULL,
    st_risco        VARCHAR2(10)   NOT NULL,
    st_feedback     VARCHAR2(15)   DEFAULT 'SEM_FEEDBACK' NOT NULL,
    dt_analise      TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_dsf_analise PRIMARY KEY (cd_analise),
    CONSTRAINT fk_dsf_analise_usuario FOREIGN KEY (cd_usuario)
        REFERENCES tb_dsf_usuario (cd_usuario),
    CONSTRAINT ck_dsf_analise_tipo  CHECK (tp_entrada IN ('TEXTO', 'IMAGEM', 'LINK')),
    CONSTRAINT ck_dsf_analise_regra CHECK (nr_nota_regras BETWEEN 0 AND 100),
    CONSTRAINT ck_dsf_analise_ia    CHECK (nr_nota_ia IS NULL OR nr_nota_ia BETWEEN 0 AND 100),
    CONSTRAINT ck_dsf_analise_final CHECK (nr_nota_final BETWEEN 0 AND 100),
    CONSTRAINT ck_dsf_analise_risco CHECK (st_risco IN ('VERDE', 'AMARELO', 'VERMELHO')),
    CONSTRAINT ck_dsf_analise_feedback
        CHECK (st_feedback IN ('SEM_FEEDBACK', 'ERA_GOLPE', 'FALSO_ALARME'))
);

-- ---------------------------------------------------------------------
-- 6. ANÁLISE × PADRÃO (N:N: quais padrões foram encontrados em cada análise)
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_analise_padrao (
    cd_analise      NUMBER(10) NOT NULL,
    cd_padrao       NUMBER(10) NOT NULL,
    nr_ocorrencias  NUMBER(3)  DEFAULT 1 NOT NULL,
    CONSTRAINT pk_dsf_analise_padrao PRIMARY KEY (cd_analise, cd_padrao),
    CONSTRAINT fk_dsf_ap_analise FOREIGN KEY (cd_analise)
        REFERENCES tb_dsf_analise (cd_analise) ON DELETE CASCADE,
    CONSTRAINT fk_dsf_ap_padrao  FOREIGN KEY (cd_padrao)
        REFERENCES tb_dsf_padrao_golpe (cd_padrao),
    CONSTRAINT ck_dsf_ap_ocorrencias CHECK (nr_ocorrencias >= 1)
);

-- ---------------------------------------------------------------------
-- 7. ALERTA (1:N análise → alerta e 1:N usuário destinatário → alerta)
--    Destinatário = qualquer outro membro do círculo de quem recebeu vermelho.
-- ---------------------------------------------------------------------
CREATE TABLE tb_dsf_alerta (
    cd_alerta        NUMBER(10) NOT NULL,
    cd_analise       NUMBER(10) NOT NULL,
    cd_destinatario  NUMBER(10) NOT NULL,
    dt_alerta        TIMESTAMP  DEFAULT SYSTIMESTAMP NOT NULL,
    st_lido          CHAR(1)    DEFAULT 'N' NOT NULL,
    CONSTRAINT pk_dsf_alerta PRIMARY KEY (cd_alerta),
    CONSTRAINT fk_dsf_alerta_analise      FOREIGN KEY (cd_analise)
        REFERENCES tb_dsf_analise (cd_analise) ON DELETE CASCADE,
    CONSTRAINT fk_dsf_alerta_destinatario FOREIGN KEY (cd_destinatario)
        REFERENCES tb_dsf_usuario (cd_usuario),
    CONSTRAINT uk_dsf_alerta_unico UNIQUE (cd_analise, cd_destinatario),
    CONSTRAINT ck_dsf_alerta_lido  CHECK (st_lido IN ('S', 'N'))
);

-- ---------------------------------------------------------------------
-- COMENTÁRIOS DAS TABELAS
-- ---------------------------------------------------------------------
COMMENT ON TABLE tb_dsf_usuario IS 'Pessoas cadastradas no Desconfie, que enviam conteúdos para análise e podem ser guardiões ou dependentes.';
COMMENT ON TABLE tb_dsf_vinculo_guardiao IS 'Autorrelacionamento entre usuários: cada guardião protege até 3 dependentes e cada dependente tem um único guardião; juntos formam o círculo que compartilha o histórico amarelo e vermelho.';
COMMENT ON TABLE tb_dsf_categoria_golpe IS 'Tipos de golpe (falso parente, falsa central, phishing...) com a orientação mostrada ao usuário.';
COMMENT ON TABLE tb_dsf_padrao_golpe IS 'Frases, termos e sinais de link com peso, usados pelo motor de regras para calcular metade da nota.';
COMMENT ON TABLE tb_dsf_analise IS 'Cada verificação feita por um usuário: conteúdo, notas de regras e de IA, nota final, risco e feedback.';
COMMENT ON TABLE tb_dsf_analise_padrao IS 'Associativa N:N: quais padrões de golpe foram encontrados em cada análise e quantas vezes.';
COMMENT ON TABLE tb_dsf_alerta IS 'Avisos gerados para os demais membros do círculo quando alguém recebe uma análise de risco vermelho.';

-- =====================================================================
-- 2. INCLUSÃO DOS REGISTROS
-- =====================================================================
-- Desliga a substituição de variáveis do SQL Developer (o & nos textos)
SET DEFINE OFF;

-- ---------------------------------------------------------------------
-- 1. USUÁRIOS (10): os 4 integrantes e 6 pessoas fictícias, entre elas
--    três idosos que são dependentes no círculo de proteção.
--    Senhas em texto puro (decisão do MVP), todas diferentes.
-- ---------------------------------------------------------------------
INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Lucas Leite', 'lucasleite.dsf@outlook.com', 'Semaforo#Verde26', '11987342210', 'A', TO_DATE('01/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Juan Marques', 'juan.marques.ads@gmail.com', 'flask#cafe2026', '11976510384', 'A', TO_DATE('01/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Matheus Matsushita', 'mateus.matsu@hotmail.com', 'Modelo_ML_92', '11991027745', 'A', TO_DATE('02/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Pedro Amaro', 'pedro.amaro.front@gmail.com', 'Css!Grid44', NULL, 'A', TO_DATE('03/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Maria Aparecida Souza', 'cida.souza1951@bol.com.br', 'netos2tres', '1139614472', 'A', TO_DATE('12/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'José Carlos Ribeiro', 'zecarlos.ribeiro@uol.com.br', 'palmeiras1951', '11988201937', 'A', TO_DATE('12/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Fernanda Lima Duarte', 'fe.limaduarte@yahoo.com.br', 'Jasmim.8811', '21994735568', 'A', TO_DATE('20/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Antônio Ferreira Gomes', 'antonio.fgomes@terra.com.br', 'horta.sitio47', '2127143390', 'A', TO_DATE('21/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Beatriz Nogueira Lopes', 'bia.nogueira05@gmail.com', 'kpop_bia2005', '11955408126', 'A', TO_DATE('25/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro)
VALUES (sq_tb_dsf_usuario.NEXTVAL, 'Rafael Tavares Mendes', 'rafa.tmendes@outlook.com', 'Corrida10k!', NULL, 'I', TO_DATE('02/11/2026', 'DD/MM/YYYY'));

-- ---------------------------------------------------------------------
-- 2. VÍNCULOS GUARDIÃO (6): Lucas protege 3 pessoas (o limite da regra),
--    os demais guardiões protegem 1. Nenhum dependente se repete.
--    Círculos: {Lucas, Maria, José, Beatriz}, {Fernanda, Antônio},
--              {Juan, Matheus}, {Pedro, Rafael}
-- ---------------------------------------------------------------------
INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (1, 5, TO_DATE('12/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (1, 6, TO_DATE('13/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (1, 9, TO_DATE('26/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (7, 8, TO_DATE('21/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (2, 3, TO_DATE('04/10/2026', 'DD/MM/YYYY'));

INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente, dt_vinculo)
VALUES (4, 10, TO_DATE('02/11/2026', 'DD/MM/YYYY'));

-- ---------------------------------------------------------------------
-- 3. CATEGORIAS DE GOLPE (6), com a orientação mostrada ao usuário
-- ---------------------------------------------------------------------
INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Falso parente',
        'Golpista se passa por filho, neto ou amigo que trocou de número e pede dinheiro.',
        'Não faça nenhum Pix. Ligue para o número antigo da pessoa ou confirme com outro familiar antes de qualquer coisa.');

INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Falsa central do banco',
        'Golpista finge ser do banco ou da central de segurança para pegar senhas e códigos.',
        'O banco nunca pede senha, código ou transferência por telefone ou mensagem. Desligue e ligue para o número do seu cartão.');

INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Phishing de link',
        'Link falso que imita sites de bancos e lojas para roubar dados.',
        'Não clique. Acesse o site digitando o endereço oficial no navegador ou pelo aplicativo oficial.');

INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Falso emprego',
        'Oferta de trabalho ou renda fácil que cobra taxa ou pede dados pessoais.',
        'Empresas sérias não cobram para contratar nem pagam por curtidas. Desconfie de ganhos fáceis.');

INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Pedido de dinheiro',
        'Mensagem que pede Pix, transferência ou pagamento.',
        'Confirme por ligação ou pessoalmente antes de transferir qualquer valor.');

INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao)
VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, 'Urgência e ameaça',
        'Mensagem que cria pressa ou medo para a vítima agir sem pensar.',
        'Golpistas criam pressa para você não pensar. Pare, respire e confirme por outro canal.');

-- ---------------------------------------------------------------------
-- 4. PADRÕES DE GOLPE (22): 12 TEXTO, 4 TLD, 2 ENCURTADOR, 2 MARCA e
--    2 DOMINIO_OFICIAL. Termos sem acento e em minúsculas, como o motor
--    de regras compara. DOMINIO_OFICIAL não tem categoria (CHECK).
-- ---------------------------------------------------------------------
-- Categoria 1: Falso parente
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 1, 'TEXTO', 'troquei de numero', 35, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 1, 'TEXTO', 'numero novo', 30, 'S');

-- Categoria 2: Falsa central do banco
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 2, 'TEXTO', 'central de seguranca', 35, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 2, 'TEXTO', 'codigo de verificacao', 35, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 2, 'TEXTO', 'compra nao reconhecida', 30, 'S');

-- Categoria 3: Phishing de link (texto)
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'TEXTO', 'clique no link', 30, 'S');

-- Categoria 4: Falso emprego
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 4, 'TEXTO', 'renda extra', 25, 'S');

-- Categoria 5: Pedido de dinheiro
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 5, 'TEXTO', 'preciso de', 10, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 5, 'TEXTO', 'pix', 15, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 5, 'TEXTO', 'transferencia', 15, 'S');

-- Categoria 6: Urgência e ameaça
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 6, 'TEXTO', 'urgente', 20, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 6, 'TEXTO', 'sua conta sera bloqueada', 30, 'S');

-- Sinais de link (categoria 3: Phishing de link)
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'TLD', '.xyz', 25, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'TLD', '.top', 20, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'TLD', '.click', 25, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'TLD', '.online', 15, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'ENCURTADOR', 'bit.ly', 20, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'ENCURTADOR', 'tinyurl.com', 20, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'MARCA', 'itau', 35, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, 3, 'MARCA', 'nubank', 35, 'S');

-- Sites verdadeiros: a marca dentro do domínio oficial não conta como imitação
INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, NULL, 'DOMINIO_OFICIAL', 'itau.com.br', 0, 'S');

INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo)
VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, NULL, 'DOMINIO_OFICIAL', 'nubank.com.br', 0, 'S');

-- ---------------------------------------------------------------------
-- 5. ANÁLISES (12): notas iguais às que o motor de regras calcularia.
--    Nota final = média de regras e IA; com IA nula (fora do ar), só regras.
--    Risco: abaixo de 30 VERDE, de 30 a 59 AMARELO, 60 ou mais VERMELHO.
-- ---------------------------------------------------------------------
-- 1) Maria recebe o golpe do falso filho: 35 + 10 + 15 + 20 = 80 nas regras
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 5, 'TEXTO',
        'Mãe, troquei de número. Preciso de um Pix urgente de R$ 800. Faz o pix nesse número que te mando a chave.',
        80, 92, 86, 'VERMELHO', 'ERA_GOLPE', TO_TIMESTAMP('03/11/2026 09:14:22', 'DD/MM/YYYY HH24:MI:SS'));

-- 2) Antônio recebe a falsa central do banco: 35 + 30 + 35 = 100
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 8, 'TEXTO',
        'Central de Segurança do Banco: identificamos uma compra não reconhecida de R$ 2.349,90. Para cancelar, informe o código de verificação que enviamos por SMS.',
        100, 95, 97.5, 'VERMELHO', 'ERA_GOLPE', TO_TIMESTAMP('05/11/2026 16:42:07', 'DD/MM/YYYY HH24:MI:SS'));

-- 3) José abre um link que imita o Itaú: marca 35 + .xyz 25 + sem HTTPS 10 = 70; a IA discordou
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 6, 'LINK',
        'http://itau-seguranca-app.xyz/atualizar',
        70, 40, 55, 'AMARELO', 'ERA_GOLPE', TO_TIMESTAMP('06/11/2026 10:03:51', 'DD/MM/YYYY HH24:MI:SS'));

-- 4) Lucas confere o site verdadeiro do Itaú
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 1, 'LINK',
        'https://www.itau.com.br/cartoes',
        0, 5, 2.5, 'VERDE', 'SEM_FEEDBACK', TO_TIMESTAMP('06/11/2026 21:30:12', 'DD/MM/YYYY HH24:MI:SS'));

-- 5) Beatriz recebe um aviso de bloqueio que era verdadeiro (falso alarme)
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 9, 'TEXTO',
        'Banco informa: sua conta será bloqueada hoje por falta de atualização cadastral. Procure sua agência.',
        30, 45, 37.5, 'AMARELO', 'FALSO_ALARME', TO_TIMESTAMP('07/11/2026 08:55:40', 'DD/MM/YYYY HH24:MI:SS'));

-- 6) Juan analisa uma mensagem comum
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 2, 'TEXTO',
        'Oi, a reunião do grupo ficou para quinta às 19h na biblioteca.',
        0, 3, 1.5, 'VERDE', 'SEM_FEEDBACK', TO_TIMESTAMP('08/11/2026 13:20:05', 'DD/MM/YYYY HH24:MI:SS'));

-- 7) Matheus recebe uma falsa vaga: 25 + 15 = 40
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 3, 'TEXTO',
        'Vaga de renda extra: ganhe R$ 300 por dia curtindo vídeos. Para liberar o cadastro, faça uma transferência de R$ 49,90.',
        40, 72, 56, 'AMARELO', 'ERA_GOLPE', TO_TIMESTAMP('09/11/2026 19:47:33', 'DD/MM/YYYY HH24:MI:SS'));

-- 8) Pedro abre um link encurtado com a IA fora do ar: só regras (20)
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 4, 'LINK',
        'https://bit.ly/3xPromoNatal',
        20, NULL, 20, 'VERDE', 'ERA_GOLPE', TO_TIMESTAMP('10/11/2026 11:08:19', 'DD/MM/YYYY HH24:MI:SS'));

-- 9) Rafael analisa um aviso de entrega com a IA fora do ar
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 10, 'TEXTO',
        'Bom dia! Seu pedido 48213 saiu para entrega e chega até sexta-feira.',
        0, NULL, 0, 'VERDE', 'SEM_FEEDBACK', TO_TIMESTAMP('10/11/2026 07:31:44', 'DD/MM/YYYY HH24:MI:SS'));

-- 10) Lucas envia um print; o texto abaixo foi lido da imagem pela IA (OCR)
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 1, 'IMAGEM',
        'PARABÉNS! Você foi sorteado no Natal Premiado. Clique no link e confirme seus dados para receber o prêmio.',
        30, 78, 54, 'AMARELO', 'SEM_FEEDBACK', TO_TIMESTAMP('12/11/2026 22:16:58', 'DD/MM/YYYY HH24:MI:SS'));

-- 11) Fernanda recebe mensagem de uma amiga que trocou mesmo de número: 30 + 10 = 40
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 7, 'TEXTO',
        'Amiga, esse é meu número novo. Salva aí! Preciso de um favor seu amanhã cedo.',
        40, 50, 45, 'AMARELO', 'FALSO_ALARME', TO_TIMESTAMP('14/11/2026 18:02:26', 'DD/MM/YYYY HH24:MI:SS'));

-- 12) Maria confere o site verdadeiro do Nubank
INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise)
VALUES (sq_tb_dsf_analise.NEXTVAL, 5, 'LINK',
        'https://nubank.com.br/app',
        0, 2, 1, 'VERDE', 'SEM_FEEDBACK', TO_TIMESTAMP('16/11/2026 15:39:10', 'DD/MM/YYYY HH24:MI:SS'));

-- ---------------------------------------------------------------------
-- 6. ANÁLISE × PADRÃO (16): padrões que o motor encontrou em cada análise.
--    A nota soma cada padrão uma vez, mas a ocorrência guarda quantas
--    vezes o termo apareceu (o "pix" aparece 2 vezes na análise 1).
-- ---------------------------------------------------------------------
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (1, 1, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (1, 8, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (1, 9, 2);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (1, 11, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (2, 3, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (2, 4, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (2, 5, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (3, 13, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (3, 19, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (5, 12, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (7, 7, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (7, 10, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (8, 17, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (10, 6, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (11, 2, 1);
INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (11, 8, 1);

-- ---------------------------------------------------------------------
-- 7. ALERTAS (4): análise vermelha avisa todos os outros membros do círculo.
--    Análise 1 (Maria) -> Lucas, José e Beatriz. Análise 2 (Antônio) -> Fernanda.
-- ---------------------------------------------------------------------
INSERT INTO tb_dsf_alerta (cd_alerta, cd_analise, cd_destinatario, dt_alerta, st_lido)
VALUES (sq_tb_dsf_alerta.NEXTVAL, 1, 1, TO_TIMESTAMP('03/11/2026 09:14:23', 'DD/MM/YYYY HH24:MI:SS'), 'S');

INSERT INTO tb_dsf_alerta (cd_alerta, cd_analise, cd_destinatario, dt_alerta, st_lido)
VALUES (sq_tb_dsf_alerta.NEXTVAL, 1, 6, TO_TIMESTAMP('03/11/2026 09:14:23', 'DD/MM/YYYY HH24:MI:SS'), 'N');

INSERT INTO tb_dsf_alerta (cd_alerta, cd_analise, cd_destinatario, dt_alerta, st_lido)
VALUES (sq_tb_dsf_alerta.NEXTVAL, 1, 9, TO_TIMESTAMP('03/11/2026 09:14:23', 'DD/MM/YYYY HH24:MI:SS'), 'N');

INSERT INTO tb_dsf_alerta (cd_alerta, cd_analise, cd_destinatario, dt_alerta, st_lido)
VALUES (sq_tb_dsf_alerta.NEXTVAL, 2, 7, TO_TIMESTAMP('05/11/2026 16:42:08', 'DD/MM/YYYY HH24:MI:SS'), 'N');

COMMIT;

-- ---------------------------------------------------------------------
-- EVIDÊNCIA: total de registros por tabela (76 no total)
-- ---------------------------------------------------------------------
SELECT 'tb_dsf_usuario' AS tabela, COUNT(*) AS registros FROM tb_dsf_usuario
UNION ALL
SELECT 'tb_dsf_vinculo_guardiao', COUNT(*) FROM tb_dsf_vinculo_guardiao
UNION ALL
SELECT 'tb_dsf_categoria_golpe', COUNT(*) FROM tb_dsf_categoria_golpe
UNION ALL
SELECT 'tb_dsf_padrao_golpe', COUNT(*) FROM tb_dsf_padrao_golpe
UNION ALL
SELECT 'tb_dsf_analise', COUNT(*) FROM tb_dsf_analise
UNION ALL
SELECT 'tb_dsf_analise_padrao', COUNT(*) FROM tb_dsf_analise_padrao
UNION ALL
SELECT 'tb_dsf_alerta', COUNT(*) FROM tb_dsf_alerta;

SELECT (SELECT COUNT(*) FROM tb_dsf_usuario)
     + (SELECT COUNT(*) FROM tb_dsf_vinculo_guardiao)
     + (SELECT COUNT(*) FROM tb_dsf_categoria_golpe)
     + (SELECT COUNT(*) FROM tb_dsf_padrao_golpe)
     + (SELECT COUNT(*) FROM tb_dsf_analise)
     + (SELECT COUNT(*) FROM tb_dsf_analise_padrao)
     + (SELECT COUNT(*) FROM tb_dsf_alerta) AS total_registros
FROM dual;

-- =====================================================================
-- 3 E 4. CONSULTAS, UPDATE E DELETE
-- =====================================================================
-- ---------------------------------------------------------------------
-- CONSULTA 1: Quais análises deram VERMELHO, da mais recente para a mais antiga?
-- Recursos: INNER JOIN, WHERE, ORDER BY
-- ---------------------------------------------------------------------
SELECT a.cd_analise,
       u.nm_usuario,
       a.tp_entrada,
       a.nr_nota_final,
       TO_CHAR(a.dt_analise, 'DD/MM/YYYY HH24:MI') AS data_analise
  FROM tb_dsf_analise a
 INNER JOIN tb_dsf_usuario u ON u.cd_usuario = a.cd_usuario
 WHERE a.st_risco = 'VERMELHO'
 ORDER BY a.dt_analise DESC;

-- ---------------------------------------------------------------------
-- CONSULTA 2: Qual a nota final média, máxima e mínima por tipo de entrada
--             (texto, imagem e link)?
-- Recursos: COUNT, AVG, MAX, MIN, GROUP BY, ORDER BY
-- ---------------------------------------------------------------------
SELECT tp_entrada,
       COUNT(*)                     AS qt_analises,
       ROUND(AVG(nr_nota_final), 2) AS nota_media,
       MAX(nr_nota_final)           AS nota_maxima,
       MIN(nr_nota_final)           AS nota_minima
  FROM tb_dsf_analise
 GROUP BY tp_entrada
 ORDER BY nota_media DESC;

-- ---------------------------------------------------------------------
-- CONSULTA 3: Quais categorias de golpe foram detectadas mais de 3 vezes?
-- Recursos: INNER JOIN (3 tabelas), COUNT, GROUP BY, HAVING, ORDER BY
-- ---------------------------------------------------------------------
SELECT c.nm_categoria,
       COUNT(*)                     AS qt_deteccoes,
       COUNT(DISTINCT ap.cd_analise) AS qt_analises
  FROM tb_dsf_analise_padrao ap
 INNER JOIN tb_dsf_padrao_golpe p    ON p.cd_padrao = ap.cd_padrao
 INNER JOIN tb_dsf_categoria_golpe c ON c.cd_categoria = p.cd_categoria
 GROUP BY c.nm_categoria
HAVING COUNT(*) > 3
 ORDER BY qt_deteccoes DESC;

-- ---------------------------------------------------------------------
-- CONSULTA 4: Quais usuários têm nota média acima da média geral de todas
--             as análises? (quem mais recebe mensagens perigosas)
-- Recursos: subconsulta, AVG, INNER JOIN, GROUP BY, HAVING, ORDER BY
-- ---------------------------------------------------------------------
SELECT u.nm_usuario,
       COUNT(a.cd_analise)            AS qt_analises,
       ROUND(AVG(a.nr_nota_final), 2) AS nota_media
  FROM tb_dsf_usuario u
 INNER JOIN tb_dsf_analise a ON a.cd_usuario = u.cd_usuario
 GROUP BY u.cd_usuario, u.nm_usuario
HAVING AVG(a.nr_nota_final) > (SELECT AVG(nr_nota_final) FROM tb_dsf_analise)
 ORDER BY nota_media DESC;

-- ---------------------------------------------------------------------
-- CONSULTA 5: Quais padrões somaram mais ocorrências no total?
--             (os termos que os golpistas mais usam)
-- Recursos: INNER JOIN, SUM, COUNT, GROUP BY, ORDER BY
-- ---------------------------------------------------------------------
SELECT p.ds_termo,
       p.tp_padrao,
       SUM(ap.nr_ocorrencias) AS total_ocorrencias,
       COUNT(ap.cd_analise)   AS qt_analises
  FROM tb_dsf_analise_padrao ap
 INNER JOIN tb_dsf_padrao_golpe p ON p.cd_padrao = ap.cd_padrao
 GROUP BY p.ds_termo, p.tp_padrao
 ORDER BY total_ocorrencias DESC, p.ds_termo;

-- ---------------------------------------------------------------------
-- CONSULTA 6: Todos os usuários e quantos alertas cada um recebeu,
--             inclusive quem nunca recebeu nenhum.
-- Recursos: LEFT JOIN, COUNT, SUM com CASE (cálculo), GROUP BY, ORDER BY
-- ---------------------------------------------------------------------
SELECT u.nm_usuario,
       COUNT(al.cd_alerta)                                AS qt_alertas,
       SUM(CASE WHEN al.st_lido = 'N' THEN 1 ELSE 0 END)  AS qt_nao_lidos
  FROM tb_dsf_usuario u
  LEFT JOIN tb_dsf_alerta al ON al.cd_destinatario = u.cd_usuario
 GROUP BY u.cd_usuario, u.nm_usuario
 ORDER BY qt_alertas DESC, u.nm_usuario;

-- ---------------------------------------------------------------------
-- CONSULTA 7: Todos os padrões cadastrados e quantas vezes cada um foi
--             encontrado, inclusive os que nunca apareceram em uma análise.
-- Recursos: RIGHT JOIN, LEFT JOIN, COUNT, NVL, GROUP BY, ORDER BY
-- ---------------------------------------------------------------------
SELECT p.ds_termo,
       p.tp_padrao,
       NVL(c.nm_categoria, '(site oficial)') AS categoria,
       COUNT(ap.cd_analise)                  AS qt_encontrado
  FROM tb_dsf_analise_padrao ap
 RIGHT JOIN tb_dsf_padrao_golpe p    ON p.cd_padrao = ap.cd_padrao
  LEFT JOIN tb_dsf_categoria_golpe c ON c.cd_categoria = p.cd_categoria
 GROUP BY p.ds_termo, p.tp_padrao, c.nm_categoria
 ORDER BY qt_encontrado DESC, p.tp_padrao, p.ds_termo;

-- ---------------------------------------------------------------------
-- CONSULTA 8: Quem protege quem? Guardiões e seus dependentes no círculo.
-- Recursos: INNER JOIN da tabela de usuário duas vezes (autorrelacionamento),
--           ORDER BY
-- ---------------------------------------------------------------------
SELECT g.nm_usuario                       AS guardiao,
       d.nm_usuario                       AS dependente,
       TO_CHAR(v.dt_vinculo, 'DD/MM/YYYY') AS desde
  FROM tb_dsf_vinculo_guardiao v
 INNER JOIN tb_dsf_usuario g ON g.cd_usuario = v.cd_guardiao
 INNER JOIN tb_dsf_usuario d ON d.cd_usuario = v.cd_dependente
 ORDER BY g.nm_usuario, d.nm_usuario;

-- ---------------------------------------------------------------------
-- CONSULTA 9: Em quais análises as regras e a IA mais discordaram?
--             (casos para revisar os pesos dos padrões ou treinar o modelo)
-- Recursos: cálculo no SELECT (ABS e CASE), WHERE com IS NOT NULL, ORDER BY
-- ---------------------------------------------------------------------
SELECT cd_analise,
       tp_entrada,
       nr_nota_regras,
       nr_nota_ia,
       ABS(nr_nota_regras - nr_nota_ia) AS diferenca,
       CASE
           WHEN nr_nota_regras > nr_nota_ia THEN 'Regras mais rigorosas'
           WHEN nr_nota_regras < nr_nota_ia THEN 'IA mais rigorosa'
           ELSE 'Concordaram'
       END AS quem_pesou_mais
  FROM tb_dsf_analise
 WHERE nr_nota_ia IS NOT NULL
 ORDER BY diferenca DESC;

-- ---------------------------------------------------------------------
-- CONSULTA 10: Qual o percentual de alarmes falsos entre as análises que
--              receberam feedback, por risco?
-- Recursos: WHERE, COUNT, SUM com CASE, cálculo de percentual, GROUP BY, ORDER BY
-- ---------------------------------------------------------------------
SELECT st_risco,
       COUNT(*)                                                        AS qt_com_feedback,
       SUM(CASE WHEN st_feedback = 'FALSO_ALARME' THEN 1 ELSE 0 END)   AS qt_falso_alarme,
       ROUND(100 * SUM(CASE WHEN st_feedback = 'FALSO_ALARME' THEN 1 ELSE 0 END) / COUNT(*), 1)
                                                                       AS pct_falso_alarme
  FROM tb_dsf_analise
 WHERE st_feedback <> 'SEM_FEEDBACK'
 GROUP BY st_risco
 ORDER BY pct_falso_alarme DESC;

-- =====================================================================
-- UPDATE com WHERE: José abriu o aplicativo e viu os alertas dele.
-- Marca como lidos apenas os alertas não lidos do usuário 6.
-- =====================================================================
SELECT cd_alerta, cd_analise, cd_destinatario, st_lido
  FROM tb_dsf_alerta
 WHERE cd_destinatario = 6;

UPDATE tb_dsf_alerta
   SET st_lido = 'S'
 WHERE cd_destinatario = 6
   AND st_lido = 'N';

SELECT cd_alerta, cd_analise, cd_destinatario, st_lido
  FROM tb_dsf_alerta
 WHERE cd_destinatario = 6;

COMMIT;

-- =====================================================================
-- DELETE com WHERE: Fernanda pediu para tirar do histórico a análise 11,
-- que foi um falso alarme. O ON DELETE CASCADE da tb_dsf_analise_padrao
-- apaga junto os padrões encontrados nela.
-- =====================================================================
SELECT COUNT(*) AS padroes_da_analise_11
  FROM tb_dsf_analise_padrao
 WHERE cd_analise = 11;

DELETE FROM tb_dsf_analise
 WHERE cd_analise = 11
   AND cd_usuario = 7;

SELECT COUNT(*) AS analise_11_restante
  FROM tb_dsf_analise
 WHERE cd_analise = 11;

SELECT COUNT(*) AS padroes_da_analise_11
  FROM tb_dsf_analise_padrao
 WHERE cd_analise = 11;

COMMIT;
