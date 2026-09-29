package com.deepblue.deepblue_rescue.mapper;

import com.deepblue.deepblue_rescue.domain.Animal;
import com.deepblue.deepblue_rescue.dto.response.AnimalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnimalMapper {

    @Mapping(target = "caseCode", source = "rescueCase.caseCode")
    @Mapping(target = "rescueStatus", source = "rescueCase.status")
    AnimalResponse toResponse(Animal animal);
}
