BEGIN;

CREATE TABLE ubicaciones (
                             id BIGSERIAL PRIMARY KEY,
                             direccion VARCHAR(255) NOT NULL,
                             ciudad VARCHAR(100) NOT NULL,
                             departamento VARCHAR(100) NOT NULL,

                             CONSTRAINT uk_ubicaciones_direccion_ciudad
                                 UNIQUE (direccion, ciudad)
);

CREATE INDEX idx_ubicaciones_ciudad
    ON ubicaciones(ciudad);

CREATE TABLE agencias (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(150) NOT NULL,
                          ubicacion_id BIGINT NOT NULL REFERENCES ubicaciones(id),
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_agencias_ubicacion
    ON agencias(ubicacion_id);

CREATE UNIQUE INDEX uq_agencias_nombre_lower
    ON agencias(LOWER(nombre));

CREATE UNIQUE INDEX uq_agencias_ubicacion_activa
    ON agencias(ubicacion_id)
    WHERE activo;

CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(150) NOT NULL,
                          correo VARCHAR(150) NOT NULL UNIQUE,
                          clave VARCHAR(255) NOT NULL,
                          telefono VARCHAR(20),
                          dni VARCHAR(8),
                          ruc VARCHAR(11),
                          rol VARCHAR(30) NOT NULL,
                          agencia_id BIGINT REFERENCES agencias(id),
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_registro TIMESTAMP NOT NULL DEFAULT now(),

                          CONSTRAINT chk_usuarios_rol CHECK (
                              rol IN (
                                      'CLIENTE',
                                      'COUNTER',
                                      'MOTORIZADO',
                                      'ENCARGADO_LOGISTICA',
                                      'ADMINISTRADOR'
                                  )
                              ),

                          CONSTRAINT chk_usuarios_documento CHECK (
                              (dni IS NULL AND ruc IS NULL)
                                  OR (
                                  dni IS NOT NULL
                                      AND ruc IS NULL
                                      AND dni ~ '^[0-9]{8}$'
                                  )
                                  OR (
                                  dni IS NULL
                                      AND ruc IS NOT NULL
                                      AND ruc ~ '^[0-9]{11}$'
                                  )
                              ),

                          CONSTRAINT chk_usuarios_cliente_documento CHECK (
                              rol <> 'CLIENTE'
                                  OR dni IS NOT NULL
                                  OR ruc IS NOT NULL
                              ),

                          CONSTRAINT chk_usuarios_agencia_requerida CHECK (
                              (
                                  rol IN ('CLIENTE', 'ADMINISTRADOR')
                                      AND agencia_id IS NULL
                                  )
                                  OR (
                                  rol IN (
                                          'COUNTER',
                                          'MOTORIZADO',
                                          'ENCARGADO_LOGISTICA'
                                      )
                                      AND agencia_id IS NOT NULL
                                  )
                              )
);


CREATE INDEX idx_usuarios_agencia
    ON usuarios(agencia_id);

CREATE UNIQUE INDEX uq_usuarios_dni
    ON usuarios(dni);

CREATE UNIQUE INDEX uq_usuarios_ruc
    ON usuarios(ruc);

CREATE INDEX idx_usuarios_agencia_rol
    ON usuarios(agencia_id, rol)
    WHERE activo;

CREATE TABLE camiones (
                          id BIGSERIAL PRIMARY KEY,
                          placa VARCHAR(15) NOT NULL UNIQUE,
                          peso_maximo_kg NUMERIC(10,2) NOT NULL,
                          volumen_maximo_m NUMERIC(10,2) NOT NULL,
                          agencia_base_id BIGINT NOT NULL REFERENCES agencias(id),
                          agencia_actual_id BIGINT NOT NULL REFERENCES agencias(id),
                          estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
                          activo BOOLEAN NOT NULL DEFAULT TRUE,

                          CONSTRAINT chk_camiones_estado CHECK (
                              estado IN (
                                         'DISPONIBLE',
                                         'EN_VIAJE',
                                         'EN_MANTENIMIENTO'
                                  )
                              ),

                          CONSTRAINT chk_camiones_capacidad_positiva CHECK (
                              peso_maximo_kg > 0
                                  AND volumen_maximo_m > 0
                              )
);

CREATE INDEX idx_camiones_agencia_actual
    ON camiones(agencia_actual_id, estado);

CREATE TABLE viajes (
                        id BIGSERIAL PRIMARY KEY,
                        camion_id BIGINT NOT NULL REFERENCES camiones(id),
                        agencia_origen_id BIGINT NOT NULL REFERENCES agencias(id),
                        agencia_destino_id BIGINT NOT NULL REFERENCES agencias(id),
                        estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
                        fecha_salida TIMESTAMP,
                        fecha_llegada TIMESTAMP,
                        encargado_salida_id BIGINT REFERENCES usuarios(id),
                        encargado_llegada_id BIGINT REFERENCES usuarios(id),
                        fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),

                        CONSTRAINT chk_viajes_estado CHECK (
                            estado IN (
                                       'PROGRAMADO',
                                       'EN_RUTA',
                                       'LLEGADO',
                                       'FINALIZADO'
                                )
                            ),

                        CONSTRAINT chk_viajes_origen_destino_distintos CHECK (
                            agencia_origen_id <> agencia_destino_id
                            ),

                        CONSTRAINT chk_viajes_fechas CHECK (
                            fecha_llegada IS NULL
                                OR fecha_salida IS NULL
                                OR fecha_llegada >= fecha_salida
                            )
);

CREATE INDEX idx_viajes_destino_estado
    ON viajes(agencia_destino_id, estado);

CREATE INDEX idx_viajes_origen_estado
    ON viajes(agencia_origen_id, estado);

CREATE INDEX idx_viajes_camion
    ON viajes(camion_id);

CREATE UNIQUE INDEX uq_viajes_camion_abierto
    ON viajes(camion_id)
    WHERE estado IN ('PROGRAMADO', 'EN_RUTA');

CREATE TABLE tarifas (
                         id BIGSERIAL PRIMARY KEY,
                         rango_peso_min_kg NUMERIC(10,2) NOT NULL,
                         rango_peso_max_kg NUMERIC(10,2) NOT NULL,
                         precio_base NUMERIC(10,2) NOT NULL,
                         recargo_domicilio NUMERIC(10,2) NOT NULL DEFAULT 0,
                         activo BOOLEAN NOT NULL DEFAULT TRUE,

                         CONSTRAINT chk_tarifas_rango_valido CHECK (
                             rango_peso_min_kg >= 0
                                 AND rango_peso_max_kg > rango_peso_min_kg
                             ),

                         CONSTRAINT chk_tarifas_precio_base CHECK (
                             precio_base > 0
                             ),

                         CONSTRAINT chk_tarifas_recargo CHECK (
                             recargo_domicilio >= 0
                             ),

                         CONSTRAINT ex_tarifas_rangos_sin_cruce
                             EXCLUDE USING gist (
            numrange(
                rango_peso_min_kg,
                rango_peso_max_kg,
                '(]'
            ) WITH &&
        )
        WHERE (activo)
);

CREATE INDEX idx_tarifas_activas_peso
    ON tarifas(rango_peso_min_kg, rango_peso_max_kg)
    WHERE activo;

CREATE TABLE envios (
                        id BIGSERIAL PRIMARY KEY,
                        codigo_envio VARCHAR(20) NOT NULL UNIQUE,
                        cliente_id BIGINT REFERENCES usuarios(id),
                        counter_recepcion_id BIGINT NOT NULL REFERENCES usuarios(id),

                        remitente_nombre VARCHAR(150) NOT NULL,
                        remitente_documento VARCHAR(11) NOT NULL,
                        remitente_telefono VARCHAR(20),

                        destinatario_nombre VARCHAR(150) NOT NULL,
                        destinatario_documento VARCHAR(11) NOT NULL,
                        destinatario_telefono VARCHAR(20),

                        modalidad VARCHAR(20) NOT NULL,
                        descripcion_paquete TEXT,
                        peso_kg NUMERIC(10,2) NOT NULL,
                        alto_cm NUMERIC(10,2),
                        ancho_cm NUMERIC(10,2),
                        largo_cm NUMERIC(10,2),
                        peso_volumetrico_kg NUMERIC(20,2) NOT NULL,
                        peso_facturable_kg NUMERIC(20,2) NOT NULL,

                        direccion_entrega VARCHAR(255),

                        agencia_origen_id BIGINT NOT NULL REFERENCES agencias(id),
                        agencia_destino_id BIGINT NOT NULL REFERENCES agencias(id),

                        costo_envio NUMERIC(10,2) NOT NULL,
                        estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',

                        viaje_id BIGINT REFERENCES viajes(id),
                        motorizado_id BIGINT REFERENCES usuarios(id),

                        fecha_registro TIMESTAMP NOT NULL DEFAULT now(),
                        fecha_entrega TIMESTAMP,

                        CONSTRAINT chk_envios_remitente_documento CHECK (
                            remitente_documento ~ '^([0-9]{8}|[0-9]{11})$'
                            ),

                        CONSTRAINT chk_envios_remitente_nombre CHECK (
                            remitente_nombre ~ '[^[:space:]]'
                            ),

                        CONSTRAINT chk_envios_modalidad CHECK (
                            modalidad IN ('AGENCIA', 'DOMICILIO')
                            ),

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

                        CONSTRAINT chk_envios_direccion_entrega CHECK (
                            (
                                modalidad = 'DOMICILIO'
                                    AND direccion_entrega IS NOT NULL
                                )
                                OR modalidad = 'AGENCIA'
                            ),

                        CONSTRAINT chk_envios_peso_positivo CHECK (
                            peso_kg > 0
                            ),

                        CONSTRAINT chk_envios_costo_positivo CHECK (
                            costo_envio > 0
                            ),

                        CONSTRAINT chk_envios_medidas_positivas CHECK (
                            (alto_cm IS NULL OR alto_cm > 0)
                                AND (ancho_cm IS NULL OR ancho_cm > 0)
                                AND (largo_cm IS NULL OR largo_cm > 0)
                            ),

                        CONSTRAINT chk_envios_origen_destino_distintos CHECK (
                            agencia_origen_id <> agencia_destino_id
                            ),

                        CONSTRAINT chk_envios_viaje_asignado CHECK (
                            estado = 'REGISTRADO'
                                OR viaje_id IS NOT NULL
                            ),

                        CONSTRAINT chk_envios_reparto_motorizado CHECK (
                            estado <> 'EN_REPARTO'
                                OR motorizado_id IS NOT NULL
                            ),

                        CONSTRAINT chk_envios_entrega_fecha CHECK (
                            estado <> 'ENTREGADO'
                                OR fecha_entrega IS NOT NULL
                            ),

                        CONSTRAINT chk_envios_estado_segun_modalidad CHECK (
                            NOT (
                                modalidad = 'AGENCIA'
                                    AND estado = 'EN_REPARTO'
                                )
                                AND NOT (
                                modalidad = 'DOMICILIO'
                                    AND estado = 'LISTO_PARA_RECOJO'
                                )
                            )
);

CREATE INDEX idx_envios_origen_estado
    ON envios(agencia_origen_id, estado);

CREATE INDEX idx_envios_destino_estado
    ON envios(agencia_destino_id, estado);

CREATE INDEX idx_envios_cliente
    ON envios(cliente_id);

CREATE INDEX idx_envios_remitente_documento
    ON envios(remitente_documento);

CREATE INDEX idx_envios_viaje
    ON envios(viaje_id);

CREATE INDEX idx_envios_pendientes_asignacion
    ON envios(agencia_origen_id, fecha_registro, id)
    WHERE estado = 'REGISTRADO' AND viaje_id IS NULL;

CREATE INDEX idx_envios_origen_fecha
    ON envios(agencia_origen_id, fecha_registro DESC);

CREATE INDEX idx_envios_motorizado_estado
    ON envios(motorizado_id, estado)
    WHERE motorizado_id IS NOT NULL;

CREATE TABLE historial_estados_envio (
                                         id BIGSERIAL PRIMARY KEY,
                                         envio_id BIGINT NOT NULL REFERENCES envios(id),
                                         estado VARCHAR(30) NOT NULL,
                                         fecha TIMESTAMP NOT NULL DEFAULT now(),
                                         usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
                                         comentario VARCHAR(255)
);

CREATE INDEX idx_historial_envio
    ON historial_estados_envio(envio_id, fecha);


COMMIT;
