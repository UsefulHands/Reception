package com.github.UsefulHands.reception.features.audit;

import com.github.UsefulHands.reception.features.audit.dtos.AuditLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    AuditLogDto toDto(AuditLogEntity entity);

    List<AuditLogDto> toDtoList(List<AuditLogEntity> entities);
}
