package com.sefault.server.user.service;

import java.util.Map;

public interface PdfGenerationService {
    String processTemplate(String templateName, Map<String, Object> data);
}
