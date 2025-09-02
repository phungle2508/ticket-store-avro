package com.ticketsystem.msroute.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * RouteDTODto
 */

@JsonTypeName("RouteDTO")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.12.0")
public class RouteDTODto {

  private UUID id;

  private String routeName;

  private String origin;

  private String destination;

  private Double distance;

  private Integer estimatedDuration;

  /**
   * Gets or Sets transportType
   */
  public enum TransportTypeEnum {
    TRAIN("TRAIN"),
    
    BUS("BUS");

    private String value;

    TransportTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TransportTypeEnum fromValue(String value) {
      for (TransportTypeEnum b : TransportTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private TransportTypeEnum transportType;

  private Boolean isActive;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public RouteDTODto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RouteDTODto(UUID id, String routeName, String origin, String destination, Double distance, Integer estimatedDuration, TransportTypeEnum transportType, Boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    this.id = id;
    this.routeName = routeName;
    this.origin = origin;
    this.destination = destination;
    this.distance = distance;
    this.estimatedDuration = estimatedDuration;
    this.transportType = transportType;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public RouteDTODto id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @NotNull @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public RouteDTODto routeName(String routeName) {
    this.routeName = routeName;
    return this;
  }

  /**
   * Get routeName
   * @return routeName
   */
  @NotNull 
  @Schema(name = "routeName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("routeName")
  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public RouteDTODto origin(String origin) {
    this.origin = origin;
    return this;
  }

  /**
   * Get origin
   * @return origin
   */
  @NotNull 
  @Schema(name = "origin", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("origin")
  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public RouteDTODto destination(String destination) {
    this.destination = destination;
    return this;
  }

  /**
   * Get destination
   * @return destination
   */
  @NotNull 
  @Schema(name = "destination", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("destination")
  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public RouteDTODto distance(Double distance) {
    this.distance = distance;
    return this;
  }

  /**
   * Get distance
   * @return distance
   */
  @NotNull 
  @Schema(name = "distance", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("distance")
  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public RouteDTODto estimatedDuration(Integer estimatedDuration) {
    this.estimatedDuration = estimatedDuration;
    return this;
  }

  /**
   * Get estimatedDuration
   * @return estimatedDuration
   */
  @NotNull 
  @Schema(name = "estimatedDuration", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("estimatedDuration")
  public Integer getEstimatedDuration() {
    return estimatedDuration;
  }

  public void setEstimatedDuration(Integer estimatedDuration) {
    this.estimatedDuration = estimatedDuration;
  }

  public RouteDTODto transportType(TransportTypeEnum transportType) {
    this.transportType = transportType;
    return this;
  }

  /**
   * Get transportType
   * @return transportType
   */
  @NotNull 
  @Schema(name = "transportType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transportType")
  public TransportTypeEnum getTransportType() {
    return transportType;
  }

  public void setTransportType(TransportTypeEnum transportType) {
    this.transportType = transportType;
  }

  public RouteDTODto isActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * Get isActive
   * @return isActive
   */
  @NotNull 
  @Schema(name = "isActive", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isActive")
  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public RouteDTODto createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @NotNull @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public RouteDTODto updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @NotNull @Valid 
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteDTODto routeDTO = (RouteDTODto) o;
    return Objects.equals(this.id, routeDTO.id) &&
        Objects.equals(this.routeName, routeDTO.routeName) &&
        Objects.equals(this.origin, routeDTO.origin) &&
        Objects.equals(this.destination, routeDTO.destination) &&
        Objects.equals(this.distance, routeDTO.distance) &&
        Objects.equals(this.estimatedDuration, routeDTO.estimatedDuration) &&
        Objects.equals(this.transportType, routeDTO.transportType) &&
        Objects.equals(this.isActive, routeDTO.isActive) &&
        Objects.equals(this.createdAt, routeDTO.createdAt) &&
        Objects.equals(this.updatedAt, routeDTO.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, routeName, origin, destination, distance, estimatedDuration, transportType, isActive, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RouteDTODto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    routeName: ").append(toIndentedString(routeName)).append("\n");
    sb.append("    origin: ").append(toIndentedString(origin)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    estimatedDuration: ").append(toIndentedString(estimatedDuration)).append("\n");
    sb.append("    transportType: ").append(toIndentedString(transportType)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

