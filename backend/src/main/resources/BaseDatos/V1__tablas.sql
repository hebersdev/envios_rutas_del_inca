CREATE TABLE ubicaciones (
                             id              BIGSERIAL PRIMARY KEY,
                             direccion       VARCHAR(255) NOT NULL,
                             ciudad          VARCHAR(100) NOT NULL,
                             departamento    VARCHAR(100) NOT NULL,
                             CONSTRAINT uk_ubicaciones_direccion_ciudad UNIQUE (direccion, ciudad)
);

CREATE INDEX idx_ubicaciones_ciudad ON ubicaciones(ciudad);

CREATE TABLE agencias (
                          id              BIGSERIAL PRIMARY KEY,
                          nombre          VARCHAR(150) NOT NULL,
                          ubicacion_id    BIGINT NOT NULL REFERENCES ubicaciones(id),
                          activo          BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_creacion  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_agencias_ubicacion ON agencias(ubicacion_id);
CREATE UNIQUE INDEX uq_agencias_nombre_lower ON agencias (LOWER(nombre));
CREATE UNIQUE INDEX uq_agencias_ubicacion_activa ON agencias (ubicacion_id) WHERE activo;


CREATE TABLE usuarios (
                          id              BIGSERIAL PRIMARY KEY,
                          nombre          VARCHAR(150) NOT NULL,
                          correo          VARCHAR(150) NOT NULL UNIQUE,
                          clave           VARCHAR(255) NOT NULL,
                          telefono        VARCHAR(20),
                          dni             VARCHAR(8),
                          ruc             VARCHAR(11),
                          rol             VARCHAR(30) NOT NULL,
                          agencia_id      BIGINT REFERENCES agencias(id),
                          activo          BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_registro  TIMESTAMP NOT NULL DEFAULT now(),

                          CONSTRAINT chk_usuarios_rol CHECK (
                              rol IN ('CLIENTE', 'COUNTER', 'MOTORIZADO', 'ENCARGADO_LOGISTICA', 'ADMINISTRADOR')
                              ),
                          CONSTRAINT chk_usuarios_documento CHECK (
                              (dni IS NULL AND ruc IS NULL)
                                  OR (dni IS NOT NULL AND ruc IS NULL AND dni ~ '^[0-9]{8}$')
                                  OR (dni IS NULL AND ruc IS NOT NULL AND ruc ~ '^[0-9]{11}$')
                              ),
                          CONSTRAINT chk_usuarios_agencia_requerida CHECK (
                              (rol IN ('CLIENTE', 'ADMINISTRADOR') AND agencia_id IS NULL)
                                  OR
                              (rol IN ('COUNTER', 'MOTORIZADO', 'ENCARGADO_LOGISTICA') AND agencia_id IS NOT NULL)
                              )
);

CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_agencia ON usuarios(agencia_id);
CREATE UNIQUE INDEX uq_usuarios_dni ON usuarios(dni);
CREATE UNIQUE INDEX uq_usuarios_ruc ON usuarios(ruc);

CREATE TABLE camiones (
                          id                  BIGSERIAL PRIMARY KEY,
                          placa               VARCHAR(15) NOT NULL UNIQUE,
                          peso_maximo_kg      NUMERIC(10,2) NOT NULL,
                          volumen_maximo_m3   NUMERIC(10,2) NOT NULL,
                          agencia_base_id     BIGINT NOT NULL REFERENCES agencias(id),
                          agencia_actual_id   BIGINT NOT NULL REFERENCES agencias(id),
                          estado              VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
                          activo              BOOLEAN NOT NULL DEFAULT TRUE,

                          CONSTRAINT chk_camiones_estado CHECK (
                              estado IN ('DISPONIBLE', 'EN_VIAJE', 'EN_MANTENIMIENTO')
                              )
);

CREATE INDEX idx_camiones_agencia_actual ON camiones(agencia_actual_id, estado);


CREATE TABLE viajes (
                        id                      BIGSERIAL PRIMARY KEY,
                        camion_id               BIGINT NOT NULL REFERENCES camiones(id),
                        agencia_origen_id       BIGINT NOT NULL REFERENCES agencias(id),
                        agencia_destino_id      BIGINT NOT NULL REFERENCES agencias(id),
                        estado                  VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
                        fecha_salida            TIMESTAMP,
                        fecha_llegada           TIMESTAMP,
                        encargado_salida_id     BIGINT REFERENCES usuarios(id),
                        encargado_llegada_id    BIGINT REFERENCES usuarios(id),
                        fecha_creacion          TIMESTAMP NOT NULL DEFAULT now(),

                        CONSTRAINT chk_viajes_estado CHECK (
                            estado IN ('PROGRAMADO', 'EN_RUTA', 'LLEGADO', 'FINALIZADO')
                            ),
                        CONSTRAINT chk_viajes_origen_destino_distintos CHECK (
                            agencia_origen_id <> agencia_destino_id
                            )
);

CREATE INDEX idx_viajes_destino_estado ON viajes(agencia_destino_id, estado);
CREATE INDEX idx_viajes_origen_estado ON viajes(agencia_origen_id, estado);
CREATE INDEX idx_viajes_camion ON viajes(camion_id);

CREATE TABLE tarifas (
    id                  BIGSERIAL PRIMARY KEY,
    rango_peso_min_kg   NUMERIC(10,2) NOT NULL,
    rango_peso_max_kg   NUMERIC(10,2) NOT NULL,
    precio_base         NUMERIC(10,2) NOT NULL,
    recargo_domicilio   NUMERIC(10,2) NOT NULL DEFAULT 0,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_tarifas_rango_valido
        CHECK (rango_peso_min_kg >= 0 AND rango_peso_max_kg > rango_peso_min_kg),
    CONSTRAINT chk_tarifas_precio_base
        CHECK (precio_base > 0),
    CONSTRAINT chk_tarifas_recargo
        CHECK (recargo_domicilio >= 0)
);

CREATE INDEX idx_tarifas_activas_peso
    ON tarifas(rango_peso_min_kg, rango_peso_max_kg)
    WHERE activo;

CREATE TABLE envios (
                        id                      BIGSERIAL PRIMARY KEY,
                        codigo_envio            VARCHAR(20) NOT NULL UNIQUE,
                        cliente_id              BIGINT NOT NULL REFERENCES usuarios(id),
                        counter_recepcion_id    BIGINT NOT NULL REFERENCES usuarios(id),
                        modalidad               VARCHAR(20) NOT NULL,
                        descripcion_paquete     TEXT,
                        peso_kg                 NUMERIC(10,2) NOT NULL,
                        alto_cm                 NUMERIC(10,2),
                        ancho_cm                NUMERIC(10,2),
                        largo_cm                NUMERIC(10,2),
                        direccion_recojo        VARCHAR(255),
                        direccion_entrega       VARCHAR(255),
                        agencia_origen_id       BIGINT NOT NULL REFERENCES agencias(id),
                        agencia_destino_id      BIGINT NOT NULL REFERENCES agencias(id),
                        costo_envio             NUMERIC(10,2) NOT NULL,
                        estado                  VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',
                        viaje_id                BIGINT REFERENCES viajes(id),
                        motorizado_id           BIGINT REFERENCES usuarios(id),
                            fecha_registro          TIMESTAMP NOT NULL DEFAULT now(),
                        fecha_entrega           TIMESTAMP,

                        CONSTRAINT chk_envios_modalidad CHECK (modalidad IN ('AGENCIA', 'DOMICILIO')),
                        CONSTRAINT chk_envios_estado CHECK (
                            estado IN (
                                       'REGISTRADO',
                                       'ASIGNADO_A_VIAJE',
                                       'EN_RUTA',
                                       'LLEGO_A_AGENCIA_DESTINO',
                                       'LISTO_PARA_RECOJO',
                                       'EN_REPARTO',
                                       'ENTREGADO'
                                )
                            ),
                        CONSTRAINT chk_envios_direccion_recojo CHECK (
                            (modalidad = 'DOMICILIO' AND direccion_recojo IS NOT NULL)
                                OR modalidad = 'AGENCIA'
                            )
);

CREATE INDEX idx_envios_codigo ON envios(codigo_envio);
CREATE INDEX idx_envios_origen_estado ON envios(agencia_origen_id, estado);
CREATE INDEX idx_envios_destino_estado ON envios(agencia_destino_id, estado);
CREATE INDEX idx_envios_cliente ON envios(cliente_id);
CREATE INDEX idx_envios_viaje ON envios(viaje_id);

CREATE TABLE historial_estados_envio (
                                         id              BIGSERIAL PRIMARY KEY,
                                         envio_id        BIGINT NOT NULL REFERENCES envios(id),
                                         estado          VARCHAR(30) NOT NULL,
                                         fecha           TIMESTAMP NOT NULL DEFAULT now(),
                                         usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
                                         comentario      VARCHAR(255)
);

CREATE INDEX idx_historial_envio ON historial_estados_envio(envio_id, fecha);