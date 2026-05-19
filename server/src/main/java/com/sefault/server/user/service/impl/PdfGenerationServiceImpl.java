package com.sefault.server.user.service.impl;

import com.sefault.server.user.service.PdfGenerationService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {
    private final SpringTemplateEngine templateEngine;

    public String processTemplate(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process(templateName, context);
    }

    public byte[] generateDocument(String templateName, Map<String, Object> data) throws IOException {
        byte[] pdfBytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processTemplate(templateName, data));
            renderer.layout();
            renderer.createPDF(out);
            pdfBytes = out.toByteArray();
        }

        return pdfBytes;
    }
}
