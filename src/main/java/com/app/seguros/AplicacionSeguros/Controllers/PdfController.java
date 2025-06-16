package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import com.app.seguros.AplicacionSeguros.Services.ReportesPdfService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    @Autowired
    private ContratoPdfService pdfService;
    @Autowired
    private ReportesPdfService reportesPdfService;
    @Autowired
    private ContratoService contratoService;
    @Value("${documentos.directorio}")
    private String directorioBase;

    @GetMapping("/contrato/{id}")
    public ResponseEntity<?> descargarPdf(@PathVariable Integer id) {
        try {
            // 1) Carga datos del contrato
            ContratoRespuestaDTO contrato = contratoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato no encontrado con ID: " + id));

            // 2) Construye la ruta del PDF
            String cedula = contrato.getCliente().getCedula();
            String nombreCompleto = contrato.getCliente().getNombre().replace(" ", "_") + "_" + contrato.getCliente().getApellido().replace(" ", "_");
            String nombreCarpetaCliente = cedula + "_" + nombreCompleto;
            String nombreCarpetaContrato = contrato.getSeguro().getNombre().replace(" ", "_") + "_" + contrato.getId();
            Path directorioContrato = Paths.get(directorioBase, nombreCarpetaCliente, nombreCarpetaContrato);
            String nombreArchivo = "contrato-" + id + ".pdf";
            Path rutaCompleta = directorioContrato.resolve(nombreArchivo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(nombreArchivo).build());

            // 3) Verifica el estado del contrato
            if (EstadoContrato.ACTIVO.equals(contrato.getEstado())) {
                // Si el contrato está firmado, intenta leer el PDF guardado
                if (Files.exists(rutaCompleta) && Files.isReadable(rutaCompleta)) {
                    // Leer el PDF guardado
                    byte[] pdfBytes = Files.readAllBytes(rutaCompleta);
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                } else {
                    // Si el contrato está firmado pero el PDF no existe, devuelve un error
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("El PDF no está disponible. El contrato está firmado, pero el archivo no se generó correctamente.");
                }
            } else {
                // Si el contrato no está firmado, genera el PDF dinámicamente sin guardarlo
                byte[] pdf = pdfService.generarContratoPdf(
                        contrato.getCliente(),
                        contrato.getEstado(),
                        contrato.getSeguro(),
                        contrato.getAgente(),
                        contrato.getFechaInicio(),
                        contrato.getFechaFin(),
                        contrato.getBeneficiarios(),
                        contrato.getCliente().getCondicionesMedicas()
                );
                return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar el PDF: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el PDF: " + e.getMessage());
        }
    }

    @GetMapping("/Reportes")
    public ResponseEntity<?> ReportesPdf() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("Reportes").build());

        byte[] pdf = reportesPdfService.generarContratoPdf(
                        contratoService.listarImpagos(),
                        contratoService.listarTodos(),
                        contratoService.listarVencidos()
                );
                return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
