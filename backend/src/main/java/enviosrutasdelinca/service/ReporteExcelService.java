package enviosrutasdelinca.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import enviosrutasdelinca.model.Usuario;
import enviosrutasdelinca.repository.ReporteRepository;

@Service
public class ReporteExcelService {

    private final ReporteRepository reportes;

    public ReporteExcelService(ReporteRepository reportes) {
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public byte[] generar(Usuario usuario, LocalDate desde, LocalDate hasta) {
        if (!"ENCARGADO_LOGISTICA".equals(usuario.rol())
                || usuario.agenciaId() == null || !Boolean.TRUE.equals(usuario.activo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para descargar reportes.");
        }
        if (desde == null || hasta == null || hasta.isBefore(desde)
                || ChronoUnit.DAYS.between(desde, hasta) > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecciona un período de hasta 366 días con fechas válidas.");
        }

        long sedeId = usuario.agenciaId();
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();
        String sede = reportes.nombreSede(sedeId);
        List<Tabla> tablas = List.of(
                new Tabla("Envíos por período", "Envíos registrados en la sede de origen",
                        List.of("Código de envío", "Fecha de registro", "Estado", "Modalidad",
                                "Destino"),
                        reportes.envios(sedeId, inicio, fin)),
                new Tabla("Ingresos", "Costo registrado por envío; no acredita pagos recibidos",
                        List.of("Código de envío", "Fecha de registro", "Mes", "Modalidad",
                                "Destino", "Costo registrado (S/)"),
                        reportes.ingresos(sedeId, inicio, fin)),
                new Tabla("Viajes y camiones", "Envíos incluidos en viajes de la sede",
                        List.of("Código de envío", "Viaje", "Camión", "Origen", "Destino",
                                "Estado del viaje", "Salida", "Llegada", "Peso del envío (kg)",
                                "Capacidad del camión (kg)", "Volumen del envío (m³)",
                                "Capacidad del camión (m³)"), reportes.viajes(sedeId, inicio, fin)),
                new Tabla("Entregas", "Envíos con destino en la sede",
                        List.of("Código de envío", "Fecha de registro", "Fecha de entrega",
                                "Estado", "Modalidad", "Origen", "Registro a entrega (horas)"),
                        reportes.entregas(sedeId, inicio, fin)),
                new Tabla("Motorizados", "Entregas a domicilio completadas en la sede",
                        List.of("Código de envío", "ID motorizado", "Motorizado",
                                "Fecha de entrega"),
                        reportes.motorizados(sedeId, inicio, fin)));

        SXSSFWorkbook libro = new SXSSFWorkbook(100);
        libro.setCompressTempFiles(true);
        try (libro; ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = crearEstilos(libro);
            for (Tabla tabla : tablas) {
                escribirTabla(libro, tabla, sede, desde, hasta, estilos);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo generar el archivo Excel.", error);
        } finally {
            libro.dispose();
        }
    }

    private Estilos crearEstilos(Workbook libro) {
        CellStyle cabecera = libro.createCellStyle();
        cabecera.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        cabecera.setFont(fuente);

        CellStyle fecha = libro.createCellStyle();
        fecha.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd"));
        CellStyle fechaHora = libro.createCellStyle();
        fechaHora.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
        CellStyle numero = libro.createCellStyle();
        numero.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));
        return new Estilos(cabecera, fecha, fechaHora, numero);
    }

    private void escribirTabla(Workbook libro, Tabla tabla, String sede,
            LocalDate desde, LocalDate hasta, Estilos estilos) {
        Sheet hoja = libro.createSheet(tabla.nombre());
        hoja.createRow(0).createCell(0).setCellValue("Rutas del Inca · " + tabla.nombre());
        hoja.createRow(1).createCell(0).setCellValue(
                "Sede: " + sede + " · Período: " + desde + " al " + hasta);
        hoja.createRow(2).createCell(0).setCellValue(tabla.criterio());
        Row cabecera = hoja.createRow(3);
        for (int columna = 0; columna < tabla.encabezados().size(); columna++) {
            Cell celda = cabecera.createCell(columna);
            celda.setCellValue(tabla.encabezados().get(columna));
            celda.setCellStyle(estilos.cabecera());
            hoja.setColumnWidth(columna, 24 * 256);
        }
        hoja.createFreezePane(0, 4);
        int fila = 4;
        for (Object[] datos : tabla.filas()) {
            if (datos.length != tabla.encabezados().size()) {
                throw new IllegalStateException("Las columnas del reporte " + tabla.nombre() + " no coinciden.");
            }
            Row renglon = hoja.createRow(fila++);
            for (int columna = 0; columna < datos.length; columna++) {
                escribirCelda(renglon.createCell(columna), datos[columna], estilos);
            }
        }
        if (tabla.filas().isEmpty()) {
            hoja.createRow(fila++).createCell(0).setCellValue("Sin datos para el período seleccionado");
        }
        hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(3, fila - 1, 0,
                tabla.encabezados().size() - 1));
    }

    private void escribirCelda(Cell celda, Object valor, Estilos estilos) {
        if (valor instanceof LocalDate fecha) {
            celda.setCellValue(fecha);
            celda.setCellStyle(estilos.fecha());
        } else if (valor instanceof LocalDateTime fechaHora) {
            celda.setCellValue(fechaHora);
            celda.setCellStyle(estilos.fechaHora());
        } else if (valor instanceof BigDecimal numero) {
            celda.setCellValue(numero.doubleValue());
            celda.setCellStyle(estilos.numero());
        } else if (valor instanceof Number numero) {
            celda.setCellValue(numero.doubleValue());
        } else if (valor != null) {
            celda.setCellValue(valor.toString());
        }
    }

    private record Tabla(String nombre, String criterio, List<String> encabezados, List<Object[]> filas) {
    }

    private record Estilos(CellStyle cabecera, CellStyle fecha, CellStyle fechaHora, CellStyle numero) {
    }
}
