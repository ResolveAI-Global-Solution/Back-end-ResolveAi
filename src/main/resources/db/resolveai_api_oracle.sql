-- =====================================================================
-- ResolveAI | Global Solution 2026/2 | Domain Driven Design Using Java
-- Turma 1TDSPW | Banco: Oracle (SQL Developer)
-- Integrantes: Andrei Oliveira de Sousa (RM 573373), Felipe Coelho da Fonseca (RM 572133),
--              Henrique Soares Pereira (RM 569898), Luiz Felipe Lourenco da Silva (RM 572576)
-- Script sem acentos. Execute com F5 (Run Script).
-- Este script usa o padrao da disciplina de Java: tb_, cd_, nm_, ds_, sq_tb_
-- (mesmo modelo logico da entrega de Banco de Dados, renomeado para o padrao DDD)
-- =====================================================================

-- Limpeza idempotente: apaga SOMENTE as tabelas e sequences deste script
BEGIN
  FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN
    ('TB_DENUNCIA','TB_HISTORICO_STATUS','TB_APOIO','TB_OCORRENCIA',
     'TB_USUARIO','TB_CATEGORIA','TB_BAIRRO','TB_MUNICIPIO')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name IN
    ('SQ_TB_DENUNCIA','SQ_TB_HISTORICO_STATUS','SQ_TB_OCORRENCIA',
     'SQ_TB_USUARIO','SQ_TB_CATEGORIA','SQ_TB_BAIRRO','SQ_TB_MUNICIPIO')) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/

-- =====================================================================
-- CREATE TABLE (na ordem das chaves estrangeiras)
-- =====================================================================

CREATE TABLE tb_municipio (
  cd_municipio   NUMBER(10)   CONSTRAINT pk_tb_municipio PRIMARY KEY,
  nm_municipio   VARCHAR2(80) NOT NULL CONSTRAINT uk_tb_municipio_nome UNIQUE,
  sg_uf          CHAR(2)      DEFAULT 'SP' NOT NULL,
  qt_habitantes  NUMBER(9)    NOT NULL CONSTRAINT ck_tb_municipio_hab CHECK (qt_habitantes > 0)
);

COMMENT ON TABLE  tb_municipio              IS 'Municipios paulistas atendidos pela ResolveAI';
COMMENT ON COLUMN tb_municipio.cd_municipio IS 'Identificador do municipio';
COMMENT ON COLUMN tb_municipio.nm_municipio IS 'Nome do municipio';
COMMENT ON COLUMN tb_municipio.sg_uf        IS 'Sigla da unidade da federacao';
COMMENT ON COLUMN tb_municipio.qt_habitantes IS 'Populacao do municipio (Censo 2022, valor aproximado)';

CREATE TABLE tb_bairro (
  cd_bairro      NUMBER(10)   CONSTRAINT pk_tb_bairro PRIMARY KEY,
  cd_municipio   NUMBER(10)   NOT NULL CONSTRAINT fk_tb_bairro_municipio REFERENCES tb_municipio(cd_municipio),
  nm_bairro      VARCHAR2(80) NOT NULL,
  ic_comunidade  CHAR(1)      DEFAULT 'N' NOT NULL CONSTRAINT ck_tb_bairro_comunidade CHECK (ic_comunidade IN ('S','N')),
  CONSTRAINT uk_tb_bairro_mun_nome UNIQUE (cd_municipio, nm_bairro)
);

COMMENT ON TABLE  tb_bairro               IS 'Bairros e comunidades dos municipios atendidos';
COMMENT ON COLUMN tb_bairro.cd_bairro     IS 'Identificador do bairro';
COMMENT ON COLUMN tb_bairro.cd_municipio  IS 'Municipio ao qual o bairro pertence (1:N)';
COMMENT ON COLUMN tb_bairro.nm_bairro     IS 'Nome do bairro ou comunidade';
COMMENT ON COLUMN tb_bairro.ic_comunidade IS 'Indica se e favela ou comunidade urbana (S/N)';

CREATE TABLE tb_categoria (
  cd_categoria   NUMBER(10)    CONSTRAINT pk_tb_categoria PRIMARY KEY,
  nm_categoria   VARCHAR2(60)  NOT NULL CONSTRAINT uk_tb_categoria_nome UNIQUE,
  ds_categoria   VARCHAR2(200)
);

COMMENT ON TABLE  tb_categoria              IS 'Tipos de problema urbano que o morador pode registrar';
COMMENT ON COLUMN tb_categoria.cd_categoria IS 'Identificador da categoria';
COMMENT ON COLUMN tb_categoria.nm_categoria IS 'Nome da categoria do problema';
COMMENT ON COLUMN tb_categoria.ds_categoria IS 'Descricao do que a categoria abrange';

CREATE TABLE tb_usuario (
  cd_usuario     NUMBER(10)    CONSTRAINT pk_tb_usuario PRIMARY KEY,
  cd_bairro      NUMBER(10)    NOT NULL CONSTRAINT fk_tb_usuario_bairro REFERENCES tb_bairro(cd_bairro),
  nm_usuario     VARCHAR2(100) NOT NULL,
  ds_email       VARCHAR2(120) NOT NULL CONSTRAINT uk_tb_usuario_email UNIQUE,
  ds_senha_hash  VARCHAR2(100) NOT NULL,
  tp_plano       VARCHAR2(10)  DEFAULT 'GRATUITO' NOT NULL CONSTRAINT ck_tb_usuario_plano CHECK (tp_plano IN ('GRATUITO','PREMIUM')),
  dt_cadastro    DATE          DEFAULT SYSDATE NOT NULL
);

COMMENT ON TABLE  tb_usuario               IS 'Moradores cadastrados no aplicativo ResolveAI';
COMMENT ON COLUMN tb_usuario.cd_usuario    IS 'Identificador do usuario';
COMMENT ON COLUMN tb_usuario.cd_bairro     IS 'Bairro onde o usuario mora (1:N)';
COMMENT ON COLUMN tb_usuario.nm_usuario    IS 'Nome do usuario';
COMMENT ON COLUMN tb_usuario.ds_email      IS 'E-mail de acesso, unico por usuario';
COMMENT ON COLUMN tb_usuario.ds_senha_hash IS 'Hash BCrypt da senha (nunca texto puro)';
COMMENT ON COLUMN tb_usuario.tp_plano      IS 'Plano do usuario: gratuito (com anuncios) ou premium';
COMMENT ON COLUMN tb_usuario.dt_cadastro   IS 'Data de cadastro no aplicativo';

CREATE TABLE tb_ocorrencia (
  cd_ocorrencia  NUMBER(10)    CONSTRAINT pk_tb_ocorrencia PRIMARY KEY,
  cd_usuario     NUMBER(10)    NOT NULL CONSTRAINT fk_tb_ocorrencia_usuario   REFERENCES tb_usuario(cd_usuario),
  cd_categoria   NUMBER(10)    NOT NULL CONSTRAINT fk_tb_ocorrencia_categoria REFERENCES tb_categoria(cd_categoria),
  cd_bairro      NUMBER(10)    NOT NULL CONSTRAINT fk_tb_ocorrencia_bairro    REFERENCES tb_bairro(cd_bairro),
  ds_titulo      VARCHAR2(120) NOT NULL,
  ds_descricao   VARCHAR2(500),
  ds_endereco    VARCHAR2(150),
  nr_latitude    NUMBER(9,6)   NOT NULL,
  nr_longitude   NUMBER(9,6)   NOT NULL,
  st_ocorrencia  VARCHAR2(15)  DEFAULT 'ABERTA' NOT NULL,
  dt_registro    DATE          DEFAULT SYSDATE NOT NULL,
  dt_resolucao   DATE,
  ic_oculta      CHAR(1)       DEFAULT 'N' NOT NULL CONSTRAINT ck_tb_ocorrencia_oculta CHECK (ic_oculta IN ('S','N')),
  CONSTRAINT ck_tb_ocorrencia_status CHECK (st_ocorrencia IN
    ('ABERTA','CONFIRMADA','EM_ANALISE','ENCAMINHADA','RESOLVIDA','REABERTA','REJEITADA')),
  CONSTRAINT ck_tb_ocorrencia_datas CHECK (dt_resolucao IS NULL OR dt_resolucao >= dt_registro)
);

COMMENT ON TABLE  tb_ocorrencia               IS 'Problemas urbanos registrados pelos moradores';
COMMENT ON COLUMN tb_ocorrencia.cd_ocorrencia IS 'Identificador da ocorrencia';
COMMENT ON COLUMN tb_ocorrencia.cd_usuario    IS 'Usuario que registrou a ocorrencia (1:N)';
COMMENT ON COLUMN tb_ocorrencia.cd_categoria  IS 'Categoria do problema (1:N)';
COMMENT ON COLUMN tb_ocorrencia.cd_bairro     IS 'Bairro onde o problema acontece (1:N)';
COMMENT ON COLUMN tb_ocorrencia.ds_titulo     IS 'Titulo curto da ocorrencia';
COMMENT ON COLUMN tb_ocorrencia.ds_descricao  IS 'Descricao detalhada do problema (opcional)';
COMMENT ON COLUMN tb_ocorrencia.ds_endereco   IS 'Endereco ou referencia do local';
COMMENT ON COLUMN tb_ocorrencia.nr_latitude   IS 'Latitude do local fotografado (posicao do aparelho no momento da foto)';
COMMENT ON COLUMN tb_ocorrencia.nr_longitude  IS 'Longitude do local fotografado';
COMMENT ON COLUMN tb_ocorrencia.st_ocorrencia IS 'Situacao atual da ocorrencia';
COMMENT ON COLUMN tb_ocorrencia.dt_registro   IS 'Data em que a ocorrencia foi registrada';
COMMENT ON COLUMN tb_ocorrencia.dt_resolucao  IS 'Data em que o problema foi resolvido; nula ate a resolucao';
COMMENT ON COLUMN tb_ocorrencia.ic_oculta     IS 'Ocorrencia oculta apos 3 denuncias pendentes (S/N)';

CREATE TABLE tb_apoio (
  cd_usuario     NUMBER(10) NOT NULL CONSTRAINT fk_tb_apoio_usuario    REFERENCES tb_usuario(cd_usuario),
  cd_ocorrencia  NUMBER(10) NOT NULL CONSTRAINT fk_tb_apoio_ocorrencia REFERENCES tb_ocorrencia(cd_ocorrencia),
  dt_apoio       DATE       DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_tb_apoio PRIMARY KEY (cd_usuario, cd_ocorrencia)
);

COMMENT ON TABLE  tb_apoio              IS 'Tabela associativa N:N entre usuario e ocorrencia: apoio de vizinhos';
COMMENT ON COLUMN tb_apoio.cd_usuario   IS 'Usuario que apoiou a ocorrencia';
COMMENT ON COLUMN tb_apoio.cd_ocorrencia IS 'Ocorrencia apoiada';
COMMENT ON COLUMN tb_apoio.dt_apoio     IS 'Data em que o apoio foi registrado';

CREATE TABLE tb_historico_status (
  cd_historico   NUMBER(10)    CONSTRAINT pk_tb_historico_status PRIMARY KEY,
  cd_ocorrencia  NUMBER(10)    NOT NULL CONSTRAINT fk_tb_historico_ocorrencia REFERENCES tb_ocorrencia(cd_ocorrencia),
  st_status      VARCHAR2(15)  NOT NULL CONSTRAINT ck_tb_historico_status CHECK (st_status IN
    ('ABERTA','CONFIRMADA','EM_ANALISE','ENCAMINHADA','RESOLVIDA','REABERTA','REJEITADA')),
  dt_status      DATE          DEFAULT SYSDATE NOT NULL,
  ds_observacao  VARCHAR2(200)
);

COMMENT ON TABLE  tb_historico_status              IS 'Historico de mudancas de status de cada ocorrencia';
COMMENT ON COLUMN tb_historico_status.cd_historico IS 'Identificador do registro de historico';
COMMENT ON COLUMN tb_historico_status.cd_ocorrencia IS 'Ocorrencia a que a mudanca se refere (1:N)';
COMMENT ON COLUMN tb_historico_status.st_status    IS 'Status assumido pela ocorrencia neste registro';
COMMENT ON COLUMN tb_historico_status.dt_status    IS 'Data da mudanca de status';
COMMENT ON COLUMN tb_historico_status.ds_observacao IS 'Observacao sobre a mudanca de status';

CREATE TABLE tb_denuncia (
  cd_denuncia    NUMBER(10)    CONSTRAINT pk_tb_denuncia PRIMARY KEY,
  cd_ocorrencia  NUMBER(10)    NOT NULL CONSTRAINT fk_tb_denuncia_ocorrencia REFERENCES tb_ocorrencia(cd_ocorrencia),
  cd_usuario     NUMBER(10)    NOT NULL CONSTRAINT fk_tb_denuncia_usuario    REFERENCES tb_usuario(cd_usuario),
  tp_motivo      VARCHAR2(25)  NOT NULL CONSTRAINT ck_tb_denuncia_motivo CHECK (tp_motivo IN
    ('FOTO_FALSA','MOSTRA_PESSOA_OU_PLACA','CONTEUDO_OFENSIVO','NAO_E_ESPACO_PUBLICO','OUTRO')),
  ds_comentario  VARCHAR2(200),
  dt_denuncia    DATE          DEFAULT SYSDATE NOT NULL,
  st_denuncia    VARCHAR2(10)  DEFAULT 'PENDENTE' NOT NULL CONSTRAINT ck_tb_denuncia_status CHECK (st_denuncia IN ('PENDENTE','MANTIDA','REMOVIDA')),
  nm_moderador   VARCHAR2(100),
  ds_decisao     VARCHAR2(200),
  dt_decisao     DATE,
  CONSTRAINT uk_tb_denuncia_usuario_ocor UNIQUE (cd_usuario, cd_ocorrencia)
);

COMMENT ON TABLE  tb_denuncia              IS 'Denuncias de publicacoes feitas por moradores (moderacao)';
COMMENT ON COLUMN tb_denuncia.cd_denuncia  IS 'Identificador da denuncia';
COMMENT ON COLUMN tb_denuncia.cd_ocorrencia IS 'Ocorrencia denunciada (1:N)';
COMMENT ON COLUMN tb_denuncia.cd_usuario   IS 'Usuario que realizou a denuncia';
COMMENT ON COLUMN tb_denuncia.tp_motivo    IS 'Motivo da denuncia';
COMMENT ON COLUMN tb_denuncia.ds_comentario IS 'Comentario adicional do denunciante';
COMMENT ON COLUMN tb_denuncia.dt_denuncia  IS 'Data em que a denuncia foi feita';
COMMENT ON COLUMN tb_denuncia.st_denuncia  IS 'Estado da denuncia: PENDENTE, MANTIDA ou REMOVIDA';
COMMENT ON COLUMN tb_denuncia.nm_moderador IS 'Nome do moderador que tomou a decisao';
COMMENT ON COLUMN tb_denuncia.ds_decisao   IS 'Justificativa da decisao do moderador';
COMMENT ON COLUMN tb_denuncia.dt_decisao   IS 'Data em que a decisao foi registrada';

-- =====================================================================
-- SEQUENCES
-- =====================================================================

CREATE SEQUENCE sq_tb_municipio        START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_bairro           START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_categoria        START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_usuario          START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_ocorrencia       START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_historico_status START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_denuncia         START WITH 1 INCREMENT BY 1 NOCACHE;

-- =====================================================================
-- INSERTS (portados do script de Banco de Dados, adaptados para o padrao DDD)
-- Hash BCrypt de 'Senha@123' gerado com BCryptPasswordEncoder
-- =====================================================================

-- Municipios (6 registros do script de BD)
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Sao Paulo',    'SP', 11451245);
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Guarulhos',    'SP',  1291771);
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Campinas',     'SP',  1139047);
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Santo Andre',  'SP',   748919);
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Osasco',       'SP',   728615);
INSERT INTO tb_municipio VALUES (sq_tb_municipio.NEXTVAL, 'Santos',       'SP',   418608);

-- Bairros (8 do script de BD + Vila Mariana adicionada para o mapa do Figma)
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 1, 'Capao Redondo',      'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 1, 'Paraisopolis',       'S');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 1, 'Itaquera',           'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 1, 'Heliopolis',         'S');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 2, 'Pimentas',           'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 3, 'Barao Geraldo',      'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 4, 'Parque Capuava',     'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 5, 'Jardim Piratininga', 'N');
INSERT INTO tb_bairro VALUES (sq_tb_bairro.NEXTVAL, 1, 'Vila Mariana',       'N');

-- Categorias (5 do script de BD)
INSERT INTO tb_categoria VALUES (sq_tb_categoria.NEXTVAL, 'Buraco na via',       'Buracos em ruas, avenidas e vielas');
INSERT INTO tb_categoria VALUES (sq_tb_categoria.NEXTVAL, 'Calcada danificada',  'Calcadas quebradas ou sem acessibilidade');
INSERT INTO tb_categoria VALUES (sq_tb_categoria.NEXTVAL, 'Iluminacao publica',  'Postes apagados e falta de iluminacao');
INSERT INTO tb_categoria VALUES (sq_tb_categoria.NEXTVAL, 'Drenagem e bueiro',   'Bueiros entupidos, alagamentos e corregos');
INSERT INTO tb_categoria VALUES (sq_tb_categoria.NEXTVAL, 'Mato alto e limpeza', 'Mato alto, entulho e falta de limpeza');

-- Usuarios (8 do script de BD, com hash BCrypt de 'Senha@123')
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 1, 'Ana Souza',      'ana.souza@exemplo.com.br',      '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'PREMIUM',  TO_DATE('2026-06-10','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 2, 'Bruno Lima',     'bruno.lima@exemplo.com.br',     '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'GRATUITO', TO_DATE('2026-06-15','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 3, 'Carla Mendes',   'carla.mendes@exemplo.com.br',   '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'GRATUITO', TO_DATE('2026-07-01','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 4, 'Diego Alves',    'diego.alves@exemplo.com.br',    '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'PREMIUM',  TO_DATE('2026-07-12','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 5, 'Elisa Rocha',    'elisa.rocha@exemplo.com.br',    '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'GRATUITO', TO_DATE('2026-08-03','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 6, 'Fabio Nunes',    'fabio.nunes@exemplo.com.br',    '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'GRATUITO', TO_DATE('2026-08-20','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 7, 'Gabriela Dias',  'gabriela.dias@exemplo.com.br',  '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'PREMIUM',  TO_DATE('2026-09-02','YYYY-MM-DD'));
INSERT INTO tb_usuario VALUES (sq_tb_usuario.NEXTVAL, 8, 'Hugo Ferreira',  'hugo.ferreira@exemplo.com.br',  '$2a$10$ffSVQwL94HdPu9kUyloZ6OxQVewad5wmkAhA7xySvIJIMqh6421Iy', 'GRATUITO', TO_DATE('2026-09-18','YYYY-MM-DD'));

-- Ocorrencias (12 do script de BD + 3 em Vila Mariana para o mapa do Figma)
-- Coordenadas aproximadas por bairro
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 1, 1, 1, 'Buraco em frente a escola', 'Buraco grande na rua, perto da entrada da escola.', 'Rua Exemplo, 120', -23.6680, -46.7340, 'RESOLVIDA', TO_DATE('2026-09-02','YYYY-MM-DD'), TO_DATE('2026-09-20','YYYY-MM-DD'), 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 2, 4, 2, 'Buraco que da no corrego', 'Buraco em frente a casa que chega ate o corrego. Ninguem veio ate hoje.', 'Rua da Comunidade, 45', -23.7290, -46.7220, 'ENCAMINHADA', TO_DATE('2026-08-15','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 3, 2, 3, 'Calcada quebrada na avenida', 'Calcada com pedras soltas, risco de queda para idosos.', 'Avenida Exemplo, 800', -23.5450, -46.4610, 'EM_ANALISE', TO_DATE('2026-11-02','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 4, 3, 4, 'Poste apagado na rua', 'Poste sem luz ha varias noites.', 'Rua das Flores, 300', -23.6220, -46.5500, 'RESOLVIDA', TO_DATE('2026-09-10','YYYY-MM-DD'), TO_DATE('2026-09-18','YYYY-MM-DD'), 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 5, 1, 5, 'Buraco na avenida principal', 'Buraco no meio da faixa, carros desviam.', 'Avenida Central, 1500', -23.4610, -46.5420, 'ENCAMINHADA', TO_DATE('2026-10-01','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 6, 5, 6, 'Mato alto em terreno', 'Terreno com mato alto perto das casas.', 'Rua do Campo, 90', -22.8240, -47.0790, 'RESOLVIDA', TO_DATE('2026-09-25','YYYY-MM-DD'), TO_DATE('2026-10-12','YYYY-MM-DD'), 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 7, 4, 7, 'Bueiro entupido', 'Bueiro entupido causa alagamento quando chove.', 'Rua do Parque, 210', -23.6660, -46.5200, 'ENCAMINHADA', TO_DATE('2026-10-20','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 8, 2, 8, 'Calcada sem rampa', 'Calcada sem rampa de acesso para cadeirantes.', 'Rua Nova, 55', -23.5240, -46.7900, 'ABERTA', TO_DATE('2026-11-05','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 1, 3, 1, 'Falta de iluminacao na viela', 'Viela escura, moradores com medo de passar a noite.', 'Viela 3, 12', -23.6680, -46.7340, 'ENCAMINHADA', TO_DATE('2026-10-28','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 2, 1, 2, 'Buraco na rua principal da comunidade', 'Buraco na rua principal dificulta a passagem.', 'Rua Principal, 10', -23.7290, -46.7220, 'RESOLVIDA', TO_DATE('2026-08-30','YYYY-MM-DD'), TO_DATE('2026-09-15','YYYY-MM-DD'), 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 3, 4, 3, 'Relato com identificacao de pessoa', 'Relato nao relacionado a servico publico.', 'Nao informado', -23.5450, -46.4610, 'REJEITADA', TO_DATE('2026-10-05','YYYY-MM-DD'), NULL, 'S');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 4, 5, 4, 'Mato alto na calcada da praca', 'Mato cobre a calcada da praca.', 'Praca Central, s/n', -23.6220, -46.5500, 'EM_ANALISE', TO_DATE('2026-11-08','YYYY-MM-DD'), NULL, 'N');

-- 3 ocorrencias em Vila Mariana (cd_bairro = 9) para o mapa do Figma
-- Coordenadas perto de Rua Vergueiro (-23.5880, -46.6360)
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 1, 2, 9, 'Calcada quebrada em frente a escola', 'Placa de concreto solta e buraco no meio da calcada. Criancas desviam pela rua.', 'Rua Vergueiro, 1180', -23.5881, -46.6362, 'CONFIRMADA', TO_DATE('2026-11-10','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 3, 1, 9, 'Buraco na entrada do parque', 'Buraco fundo na entrada do parque. Risco para ciclistas.', 'Rua Vergueiro, 1500', -23.5900, -46.6370, 'ABERTA', TO_DATE('2026-11-14','YYYY-MM-DD'), NULL, 'N');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia, dt_registro, dt_resolucao, ic_oculta)
  VALUES (sq_tb_ocorrencia.NEXTVAL, 5, 3, 9, 'Poste sem luz no cruzamento', 'Poste apagado no cruzamento, ponto perigoso a noite.', 'Rua Domingos de Morais, 200', -23.5870, -46.6350, 'REABERTA', TO_DATE('2026-11-01','YYYY-MM-DD'), NULL, 'N');

-- Apoios (portados do script de BD + 2 novos em Vila Mariana)
INSERT INTO tb_apoio VALUES (2, 1,  TO_DATE('2026-09-03','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (3, 1,  TO_DATE('2026-09-04','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (4, 2,  TO_DATE('2026-08-16','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (5, 2,  TO_DATE('2026-08-17','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (6, 2,  TO_DATE('2026-08-18','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (7, 5,  TO_DATE('2026-10-02','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (8, 5,  TO_DATE('2026-10-03','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (1, 10, TO_DATE('2026-09-01','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (3, 10, TO_DATE('2026-09-02','YYYY-MM-DD'));
-- 2 apoios na ocorrencia 13 (Vila Mariana - CONFIRMADA)
INSERT INTO tb_apoio VALUES (2, 13, TO_DATE('2026-11-11','YYYY-MM-DD'));
INSERT INTO tb_apoio VALUES (4, 13, TO_DATE('2026-11-12','YYYY-MM-DD'));

-- Historico de status (portado do script de BD + historico das novas ocorrencias)
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 1,  'ABERTA',      TO_DATE('2026-09-02','YYYY-MM-DD'), 'Ocorrencia registrada com foto no local.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 1,  'ENCAMINHADA', TO_DATE('2026-09-05','YYYY-MM-DD'), 'Encaminhada a Zeladoria Urbana.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 1,  'RESOLVIDA',   TO_DATE('2026-09-20','YYYY-MM-DD'), 'Buraco tapado pela equipe da prefeitura.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 2,  'ABERTA',      TO_DATE('2026-08-15','YYYY-MM-DD'), 'Ocorrencia registrada com foto no local.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 2,  'ENCAMINHADA', TO_DATE('2026-08-20','YYYY-MM-DD'), 'Encaminhada a Zeladoria Urbana.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 11, 'REJEITADA',   TO_DATE('2026-10-06','YYYY-MM-DD'), 'Rejeitada pela moderacao: identificava uma pessoa.');
-- Historico das ocorrencias de Vila Mariana
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 13, 'ABERTA',      TO_DATE('2026-11-10','YYYY-MM-DD'), 'Ocorrencia registrada com foto no local.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 13, 'CONFIRMADA',  TO_DATE('2026-11-12','YYYY-MM-DD'), 'Dois vizinhos confirmaram o problema.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 14, 'ABERTA',      TO_DATE('2026-11-14','YYYY-MM-DD'), 'Ocorrencia registrada com foto no local.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 15, 'ABERTA',      TO_DATE('2026-11-01','YYYY-MM-DD'), 'Ocorrencia registrada com foto no local.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 15, 'RESOLVIDA',   TO_DATE('2026-11-05','YYYY-MM-DD'), 'Poste consertado pela concessionaria.');
INSERT INTO tb_historico_status VALUES (sq_tb_historico_status.NEXTVAL, 15, 'REABERTA',    TO_DATE('2026-11-10','YYYY-MM-DD'), 'Poste apagou novamente. Contestado com foto.');

-- 3 denuncias de exemplo na ocorrencia 11 (REJEITADA/oculta): 1 REMOVIDA, 1 MANTIDA, 1 PENDENTE
INSERT INTO tb_denuncia (cd_denuncia, cd_ocorrencia, cd_usuario, tp_motivo, ds_comentario, dt_denuncia, st_denuncia, nm_moderador, ds_decisao, dt_decisao)
  VALUES (sq_tb_denuncia.NEXTVAL, 11, 5, 'MOSTRA_PESSOA_OU_PLACA', 'Da para ver o rosto de uma crianca na foto.', TO_DATE('2026-10-05','YYYY-MM-DD'), 'REMOVIDA', 'Equipe HalfTech', 'Foto expoe identificacao de menor. Ocorrencia removida.', TO_DATE('2026-10-06','YYYY-MM-DD'));
INSERT INTO tb_denuncia (cd_denuncia, cd_ocorrencia, cd_usuario, tp_motivo, ds_comentario, dt_denuncia, st_denuncia, nm_moderador, ds_decisao, dt_decisao)
  VALUES (sq_tb_denuncia.NEXTVAL, 11, 6, 'CONTEUDO_OFENSIVO', 'Descricao ofensiva para moradores do bairro.', TO_DATE('2026-10-05','YYYY-MM-DD'), 'MANTIDA', 'Equipe HalfTech', 'Denuncia sem fundamento apos revisao.', TO_DATE('2026-10-06','YYYY-MM-DD'));
INSERT INTO tb_denuncia (cd_denuncia, cd_ocorrencia, cd_usuario, tp_motivo, ds_comentario, dt_denuncia, st_denuncia, nm_moderador, ds_decisao, dt_decisao)
  VALUES (sq_tb_denuncia.NEXTVAL, 11, 7, 'FOTO_FALSA', 'A foto parece ser de outro local.', TO_DATE('2026-10-07','YYYY-MM-DD'), 'PENDENTE', NULL, NULL, NULL);

COMMIT;
