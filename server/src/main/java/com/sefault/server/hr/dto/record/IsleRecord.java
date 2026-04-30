package com.sefault.server.hr.dto.record;

import java.util.UUID;
import lombok.NonNull;

public record IsleRecord(UUID id, @NonNull UUID employeeId, String code) {}
