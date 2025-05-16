package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ContratoPdfService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generarContratoPdf(
            ClienteRespuestaDTO cliente,
            Seguro seguro,
            AgenteDTO agente,
            java.util.Date fechaInicio,
            java.util.Date fechaFin,
            List<Beneficiario> beneficiarios,
            List<CondicionMedica> condicionesMedicas
    ) throws Exception {
        // 1) Preparar el contexto Thymeleaf
        Context ctx = new Context();
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("seguro", seguro);
        ctx.setVariable("agente", agente);
        ctx.setVariable("fechaInicio", fechaInicio);
        ctx.setVariable("fechaFin", fechaFin);
        ctx.setVariable("beneficiarios", beneficiarios);
        ctx.setVariable("condicionesMedicas", condicionesMedicas);

        // 2) Procesar la plantilla a HTML
        String html = templateEngine.process("ContratoPlantilla", ctx);

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
