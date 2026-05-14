package com.sefault.server.user.service;

import java.io.IOException;
import java.util.Map;

public interface PdfGenerationService {
    String processTemplate(String templateName, Map<String, Object> data);

    byte[] generateDocument(String templateName, Map<String, Object> data) throws IOException;
}
