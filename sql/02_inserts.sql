-- =====================================================================
-- DESCONFIE · Global Solution 2026/2 · CitizenTech
-- Disciplina: Building Relational Database · FIAP · Turma 1TDSPJ
-- Integrantes: Juan Souza Marques (RM 573469), Lucas Leite Carlos (RM 571985),
--              Matheus Matsushita de Souza (RM 570017), Pedro Amaro Pires (RM 570636)
-- Data: 17/11/2026
-- Objetivo: inserir 76 registros espalhados pelas 7 tabelas. Categorias e
--           padrões são o catálogo que a API Java usa de verdade no motor
--           de regras; usuários, análises e alertas são dados fictícios.
-- Ordem de execução: 01_ddl.sql -> 02_inserts.sql -> 03_consultas.sql
-- ATENÇÃO: rodar uma vez só, logo depois do 01_ddl.sql (que recria tudo vazio).
--          Para refazer tudo, use o desconfie_bd_entrega.sql, que limpa antes.
-- Observação: os códigos usados nas chaves estrangeiras (1, 2, 3...) valem
--             porque o 01_ddl.sql cria as sequences começando em 1.
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
