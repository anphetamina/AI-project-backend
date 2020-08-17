package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

@Data
@Builder
@Relation(itemRelation = "resourcesResponse")
public class ResourcesResponse {
    Long teamId;
    @Schema(description = "active cpu cores number sum for all virtual machines") int activeNumVcpu;
    @Schema(description = "active disk space amount (MB) sum for all virtual machines") int activeDiskSpace;
    @Schema(description = "active ram amount (GB) sum for all virtual machines") int activeRam;
    @Schema(description = "active virtual machines number") int activeVMs;
    @Schema(description = "inactive/active virtual machines total") int tot;
    @Schema(description = "team configuration max cpu cores number") int maxVcpu;
    @Schema(description = "team configuration max disk space amount (MB)") int maxDiskSpace;
    @Schema(description = "team configuration max ram amount (GB)") int maxRam;
    @Schema(description = "team configuration max number of active virtual machines") int maxOn;
    @Schema(description = "team configuration max number of virtual machines both active and inactive") int maxTot;
}
