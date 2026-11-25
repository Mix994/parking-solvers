-- Espelha o esquema Room do app (datas em timestamptz, UTC).

CREATE TABLE servidor (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    prontuario VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL,
    telefone   VARCHAR(50)  NOT NULL,
    setor      VARCHAR(255) NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    login       VARCHAR(100) NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL CHECK (perfil IN ('ADMIN', 'PORTARIA', 'SERVIDOR')),
    servidor_id BIGINT REFERENCES servidor (id) ON DELETE SET NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_usuario_servidor ON usuario (servidor_id);

CREATE TABLE veiculo (
    id              BIGSERIAL PRIMARY KEY,
    servidor_id     BIGINT       NOT NULL REFERENCES servidor (id) ON DELETE RESTRICT,
    placa           VARCHAR(10)  NOT NULL UNIQUE,
    modelo          VARCHAR(255) NOT NULL,
    cor             VARCHAR(50)  NOT NULL,
    tipo_cadastro   VARCHAR(20)  NOT NULL DEFAULT 'PERMANENTE'
                    CHECK (tipo_cadastro IN ('PERMANENTE', 'TEMPORARIO')),
    validade_inicio TIMESTAMPTZ,
    validade_fim    TIMESTAMPTZ,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_veiculo_servidor ON veiculo (servidor_id);

CREATE TABLE registro_movimentacao (
    id              BIGSERIAL PRIMARY KEY,
    veiculo_id      BIGINT REFERENCES veiculo (id) ON DELETE SET NULL,
    placa_registrada VARCHAR(10) NOT NULL,
    autorizado      BOOLEAN      NOT NULL,
    data_hora_entrada TIMESTAMPTZ NOT NULL,
    data_hora_saida   TIMESTAMPTZ,
    turno           VARCHAR(10)  NOT NULL CHECK (turno IN ('MANHA', 'TARDE', 'NOITE')),
    tipo_vaga       VARCHAR(30)  NOT NULL DEFAULT 'COMUM'
                    CHECK (tipo_vaga IN ('COMUM', 'CARREGAMENTO_ELETRICO', 'EXCLUSIVA_PCD',
                                         'EXCLUSIVA_IDOSO', 'EXCLUSIVA_OFICIAL')),
    observacao      TEXT         NOT NULL DEFAULT ''
);
CREATE INDEX idx_mov_veiculo ON registro_movimentacao (veiculo_id);
CREATE INDEX idx_mov_placa ON registro_movimentacao (placa_registrada);
CREATE INDEX idx_mov_saida ON registro_movimentacao (data_hora_saida);

CREATE TABLE visitante (
    id                 BIGSERIAL PRIMARY KEY,
    nome               VARCHAR(255) NOT NULL,
    documento          VARCHAR(50)  NOT NULL,
    instituicao_origem VARCHAR(255) NOT NULL,
    telefone           VARCHAR(50)  NOT NULL,
    placa              VARCHAR(10)  NOT NULL,
    modelo_veiculo     VARCHAR(255) NOT NULL,
    cor_veiculo        VARCHAR(50)  NOT NULL,
    motivo_visita      VARCHAR(500) NOT NULL,
    setor_destino      VARCHAR(255) NOT NULL,
    tipo_vaga          VARCHAR(30)  NOT NULL DEFAULT 'COMUM'
                       CHECK (tipo_vaga IN ('COMUM', 'CARREGAMENTO_ELETRICO', 'EXCLUSIVA_PCD',
                                            'EXCLUSIVA_IDOSO', 'EXCLUSIVA_OFICIAL')),
    turno              VARCHAR(10)  NOT NULL CHECK (turno IN ('MANHA', 'TARDE', 'NOITE')),
    registrado_por     VARCHAR(255) NOT NULL,
    data_hora_entrada  TIMESTAMPTZ  NOT NULL,
    data_hora_saida    TIMESTAMPTZ,
    observacao         TEXT         NOT NULL DEFAULT ''
);
CREATE INDEX idx_visitante_placa ON visitante (placa);
CREATE INDEX idx_visitante_saida ON visitante (data_hora_saida);

CREATE TABLE configuracao_estacionamento (
    id                               INT PRIMARY KEY,
    capacidade_total                 INT NOT NULL DEFAULT 60,
    capacidade_exclusivas            INT NOT NULL DEFAULT 10,
    capacidade_carregamento_eletrico INT NOT NULL DEFAULT 2,
    CONSTRAINT configuracao_unica CHECK (id = 1)
);
INSERT INTO configuracao_estacionamento (id) VALUES (1);
