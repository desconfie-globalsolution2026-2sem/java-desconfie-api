-- =====================================================================
-- DESCONFIE · Global Solution 2026/2 · CitizenTech
-- Disciplina: Building Relational Database · FIAP · Turma 1TDSPJ
-- Integrantes: Juan Souza Marques (RM 573469), Lucas Leite Carlos (RM 571985),
--              Matheus Matsushita de Souza (RM 570017), Pedro Amaro Pires (RM 570636)
-- Data: 17/11/2026
-- Objetivo: 10 consultas numeradas que respondem perguntas da solução,
--           seguidas de um UPDATE e de um DELETE, ambos com WHERE.
-- Ordem de execução: 01_ddl.sql -> 02_inserts.sql -> 03_consultas.sql
-- Recursos: WHERE, ORDER BY, cálculo no SELECT, COUNT, SUM, AVG, MAX, MIN,
--           GROUP BY, HAVING, subconsulta, INNER JOIN, LEFT JOIN, RIGHT JOIN.
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
