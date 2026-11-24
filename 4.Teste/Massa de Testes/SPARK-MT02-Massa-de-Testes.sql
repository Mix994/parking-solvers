-- =============================================================================
-- SPARK - Massa de Testes (SQLite / Room v3) | Script 02 - Carga de referência
-- Nota: o aplicativo também popula automaticamente estes registros via
--       DadosIniciais.kt na primeira execução (incluindo hashes SHA-256 + salt).
-- =============================================================================

BEGIN TRANSACTION;

INSERT INTO servidor (id, nome, prontuario, email, telefone, setor, ativo) VALUES
    (1, 'Prof.ª Ana Beatriz Souza',  'BP102401', 'ana.souza@campus.edu.br',         '(11) 98765-1101', 'Coordenação BCC',            1),
    (2, 'Prof. Carlos Eduardo Lima', 'BP102402', 'carlos.lima@campus.edu.br',       '(11) 98765-2202', 'Departamento de Computação', 1),
    (3, 'Fernanda Oliveira',         'BP102403', 'fernanda.oliveira@campus.edu.br', '(11) 98765-3303', 'Secretaria Acadêmica',       1);

INSERT INTO veiculo (id, servidor_id, placa, modelo, cor, tipo_cadastro, validade_inicio_millis, validade_fim_millis, ativo) VALUES
    (1, 1, 'ABC1D23', 'Honda Civic',           'Prata',    'PERMANENTE', NULL, NULL, 1),
    (2, 2, 'BRA2E19', 'Toyota Corolla',        'Branco',   'PERMANENTE', NULL, NULL, 1),
    (3, 1, 'TMP4F56', 'Fiat Argo (Locadora)',  'Vermelho', 'TEMPORARIO', 1763856000000, 1764201600000, 1),
    (4, 3, 'EXP9988', 'VW Gol (Familiar)',     'Cinza',    'TEMPORARIO', 1763424000000, 1763769600000, 1);

INSERT INTO registro_movimentacao (id, veiculo_id, placa_registrada, autorizado, data_hora_entrada_millis, data_hora_saida_millis, turno, tipo_vaga, observacao) VALUES
    (1, 1, 'ABC1D23', 1, 1763906400000, NULL,          'MANHA', 'COMUM',         'Entrada regular docente'),
    (2, 2, 'BRA2E19', 1, 1763910000000, NULL,          'TARDE', 'EXCLUSIVA_PCD', 'Vaga exclusiva bloco A'),
    (3, 3, 'TMP4F56', 1, 1763899200000, 1763902800000, 'MANHA', 'COMUM',         'Troca temporária validada'),
    (4, 1, 'ABC1D23', 1, 1763733600000, 1763744400000, 'TARDE', 'COMUM',         'Aula período vespertino'),
    (5, 2, 'BRA2E19', 1, 1763568000000, 1763571600000, 'NOITE', 'EXCLUSIVA_PCD', 'Reunião colegiado noturno');

INSERT INTO visitante (id, nome, documento, instituicao_origem, telefone, placa, modelo_veiculo, cor_veiculo, motivo_visita, setor_destino, tipo_vaga, turno, registrado_por, data_hora_entrada_millis, data_hora_saida_millis, observacao) VALUES
    (1, 'Prof. Ricardo Mendes', '321.654.987-00', 'IFSP Campus Guarulhos',          '(11) 97412-8890', 'IFG8B24', 'Hyundai Creta', 'Azul',   'Banca avaliadora de TCC',    'Auditório Bloco B', 'COMUM',             'TARDE', 'Guarita Principal', 1763913600000, NULL,          'Convidado pela Coordenação BCC'),
    (2, 'Mariana Ferreira',     '45.890.123-4',   'TechRede Telecom',               '(11) 96321-4400', 'FBR3C12', 'Fiat Fiorino',  'Branco', 'Manutenção de link óptico',  'CTI / Datacenter',  'EXCLUSIVA_OFICIAL', 'MANHA', 'Guarita Principal', 1763910000000, NULL,          'Autorizado pela Diretoria Administrativa'),
    (3, 'Roberto Alves',        '112.445.778-90', 'Prefeitura de Bragança Paulista','(11) 95510-2030', 'PBP2026', 'Renault Logan', 'Prata',  'Reunião convênio extensão',  'Direção Geral',     'COMUM',             'MANHA', 'Guarita Principal', 1763820000000, 1763827200000, 'Visita encerrada normalmente');

COMMIT;
