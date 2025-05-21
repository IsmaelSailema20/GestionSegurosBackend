package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    @Autowired
    private ContratoPdfService pdfService;
    @Autowired
    private ContratoService contratoService;

    @GetMapping("/contrato/{id}")
    public ResponseEntity<?> descargarPdf(@PathVariable Integer id) {
        try {
            // 1) Carga datos desde tu servicio / repositorios
            ContratoRespuestaDTO c = contratoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

            byte[] pdf = pdfService.generarContratoPdf(
                    c.getCliente(),
                    c.getEstado(),
                    c.getSeguro(),
                    c.getAgente(),
                    c.getFechaInicio(),
                    c.getFechaFin(),
                    c.getBeneficiarios(),
                    c.getCliente().getCondicionesMedicas()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.inline().filename("contrato-"+id+".pdf").build()
            );
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
