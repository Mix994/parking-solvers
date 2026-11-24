-- =============================================================================
-- SPARK - Massa de Testes (SQLite / Room v3) | Script 03 - Conferência da base
-- =============================================================================

-- 1. Quantidade de registros esperados por tabela após DadosIniciais
SELECT tabela, esperado, encontrado,
       CASE WHEN esperado = encontrado THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
FROM (
    SELECT 'usuario'               AS tabela, 5 AS esperado, (SELECT COUNT(*) FROM usuario)               AS encontrado UNION ALL
    SELECT 'servidor',                        3,             (SELECT COUNT(*) FROM servidor)                            UNION ALL
    SELECT 'veiculo',                         4,             (SELECT COUNT(*) FROM veiculo)                             UNION ALL
    SELECT 'registro_movimentacao',           5,             (SELECT COUNT(*) FROM registro_movimentacao)               UNION ALL
    SELECT 'visitante',                       3,             (SELECT COUNT(*) FROM visitante)
) t
ORDER BY tabela;

-- 2. Verificações-chave usadas pelo roteiro de testes
SELECT 'Servidores no pátio agora = 2' AS verificacao,
       CASE WHEN (SELECT COUNT(*) FROM registro_movimentacao WHERE data_hora_saida_millis IS NULL) = 2
            THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
UNION ALL
SELECT 'Visitantes externos no campus agora = 2',
       CASE WHEN (SELECT COUNT(*) FROM visitante WHERE data_hora_saida_millis IS NULL) = 2
            THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Veículo EXP9988 cadastrado como TEMPORARIO',
       CASE WHEN (SELECT tipo_cadastro FROM veiculo WHERE placa = 'EXP9988') = 'TEMPORARIO'
            THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Contas iniciais cobrem os 3 níveis (ADMIN, PORTARIA, SERVIDOR)',
       CASE WHEN (SELECT COUNT(DISTINCT perfil) FROM usuario WHERE ativo = 1) = 3
            THEN 'OK' ELSE 'DIVERGENTE' END;
