-- =============================================================================
-- SPARK - Massa de Testes (SQLite / Room v3) | Script 01 - Limpeza da base
-- =============================================================================

PRAGMA foreign_keys = OFF;

BEGIN TRANSACTION;

DELETE FROM registro_movimentacao;
DELETE FROM veiculo;
DELETE FROM usuario;
DELETE FROM visitante;
DELETE FROM servidor;
DELETE FROM sqlite_sequence WHERE name IN (
    'registro_movimentacao',
    'veiculo',
    'usuario',
    'visitante',
    'servidor'
);

COMMIT;

PRAGMA foreign_keys = ON;
