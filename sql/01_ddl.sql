-- =====================================================================
-- DESCONFIE · Global Solution 2026/2 · CitizenTech
-- Disciplina: Building Relational Database · FIAP · Turma 1TDSPJ
-- Integrantes: Juan Souza Marques (RM 573469), Lucas Leite Carlos (RM 571985),
--              Matheus Matsushita de Souza (RM 570017), Pedro Amaro Pires (RM 570636)
-- Data: 17/11/2026
-- Objetivo: criar as sequences e as 7 tabelas com PK, FK, NOT NULL,
--           UNIQUE e CHECK, e um comentário em cada tabela. O mesmo
--           schema atende Building Relational Database e a API Java.
-- Ordem de execução: 01_ddl.sql -> 02_inserts.sql -> 03_consultas.sql
--           (dentro deste arquivo: sequences, tabelas na ordem das
--           chaves estrangeiras, comentários)
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
