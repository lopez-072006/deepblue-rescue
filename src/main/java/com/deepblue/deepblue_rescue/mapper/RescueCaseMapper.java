package com.deepblue.deepblue_rescue.mapper;

import com.deepblue.deepblue_rescue.domain.RescueCase;
import com.deepblue.deepblue_rescue.dto.response.RescueCaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RescueCaseMapper {

    @Mapping(target = "centerCode", source = "rescueCenter.code")
    @Mapping(target = "animalCode", source = "animal.animalCode")
    RescueCaseResponse toResponse(RescueCase rescueCase);
}
