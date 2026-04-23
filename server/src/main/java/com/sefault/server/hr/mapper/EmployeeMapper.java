package com.sefault.server.hr.mapper;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeRecord entityToRecord(Employee Employee);

    EmployeeRecord projectionToRecord(EmployeeProjection projection);

    Employee toEntity(EmployeeRecord record);

    void updateEntityFromRecord(EmployeeRecord record, @MappingTarget Employee entityToUpdate);
}
