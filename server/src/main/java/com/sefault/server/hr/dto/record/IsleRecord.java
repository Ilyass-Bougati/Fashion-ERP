package com.sefault.server.hr.dto.record;

import com.sefault.server.hr.entity.Employee;
import java.util.UUID;

public record IsleRecord(UUID id, Employee employee, String code) {}
