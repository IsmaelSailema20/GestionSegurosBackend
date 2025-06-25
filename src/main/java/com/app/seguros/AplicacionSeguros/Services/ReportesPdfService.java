package com.app.seguros.AplicacionSeguros.Services;


import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportesPdfService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generarContratoPdf(
            List<ContratoRespuestaDTO> listaImpagos,
            List<ContratoRespuestaDTO> listaPorCliente,
            List<ContratoRespuestaDTO> listaVencidos,
            List<ContratoRespuestaDTO> listaPorVencer
    ) throws Exception {
        // 1) Preparar el contexto Thymeleaf
        Context ctx = new Context();
        ctx.setVariable("contratosPorVencer", listaPorVencer);
        ctx.setVariable("contratosImpagos", listaImpagos);
        ctx.setVariable("contratosPorCliente", listaPorCliente);
        ctx.setVariable("contratosVencidos", listaVencidos);


        // 2) Procesar la plantilla a HTML
        String html = templateEngine.process("ReportesPlantilla", ctx);

        // 3) Renderizar HTML a PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generarReporteDocumentosPdf(
            List<Documento> listaDocumentos
    ) throws Exception {
        // 1) Preparar el contexto Thymeleaf
        Context ctx = new Context();

        Map<TipoDocumento, List<Documento>> documentosPorTipo = listaDocumentos.stream()
                .collect(Collectors.groupingBy(Documento::getTipo));

        ctx.setVariable("documentosPorTipo",documentosPorTipo);


        // 2) Procesar la plantilla a HTML
        String html = templateEngine.process("ReportesDocumentosPlantilla", ctx);

        // 3) Renderizar HTML a PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }
}
