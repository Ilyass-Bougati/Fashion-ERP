package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.Report;
import java.util.Set;
import java.util.UUID;

public record ReportCategoryRecord(UUID id, String name, Set<Report> reports, String description) {}
