package com.ticketsystem.msroute.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ticketsystem.msroute.client.model.RouteDTODto;
import java.math.BigDecimal;
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
 * ScheduleDTODto
 */

@JsonTypeName("ScheduleDTO")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.12.0")
public class ScheduleDTODto {

  private UUID id;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime departureTime;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime arrivalTime;

  private Integer totalSeats;

  private Integer availableSeats;

  private BigDecimal basePrice;

  private Boolean isActive;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  private RouteDTODto route;

  public ScheduleDTODto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ScheduleDTODto(UUID id, OffsetDateTime departureTime, OffsetDateTime arrivalTime, Integer totalSeats, Integer availableSeats, BigDecimal basePrice, Boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt, RouteDTODto route) {
    this.id = id;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.totalSeats = totalSeats;
    this.availableSeats = availableSeats;
    this.basePrice = basePrice;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.route = route;
  }

  public ScheduleDTODto id(UUID id) {
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

  public ScheduleDTODto departureTime(OffsetDateTime departureTime) {
    this.departureTime = departureTime;
    return this;
  }

  /**
   * Get departureTime
   * @return departureTime
   */
  @NotNull @Valid 
  @Schema(name = "departureTime", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("departureTime")
  public OffsetDateTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(OffsetDateTime departureTime) {
    this.departureTime = departureTime;
  }

  public ScheduleDTODto arrivalTime(OffsetDateTime arrivalTime) {
    this.arrivalTime = arrivalTime;
    return this;
  }

  /**
   * Get arrivalTime
   * @return arrivalTime
   */
  @NotNull @Valid 
  @Schema(name = "arrivalTime", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("arrivalTime")
  public OffsetDateTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(OffsetDateTime arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public ScheduleDTODto totalSeats(Integer totalSeats) {
    this.totalSeats = totalSeats;
    return this;
  }

  /**
   * Get totalSeats
   * @return totalSeats
   */
  @NotNull 
  @Schema(name = "totalSeats", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalSeats")
  public Integer getTotalSeats() {
    return totalSeats;
  }

  public void setTotalSeats(Integer totalSeats) {
    this.totalSeats = totalSeats;
  }

  public ScheduleDTODto availableSeats(Integer availableSeats) {
    this.availableSeats = availableSeats;
    return this;
  }

  /**
   * Get availableSeats
   * @return availableSeats
   */
  @NotNull 
  @Schema(name = "availableSeats", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("availableSeats")
  public Integer getAvailableSeats() {
    return availableSeats;
  }

  public void setAvailableSeats(Integer availableSeats) {
    this.availableSeats = availableSeats;
  }

  public ScheduleDTODto basePrice(BigDecimal basePrice) {
    this.basePrice = basePrice;
    return this;
  }

  /**
   * Get basePrice
   * @return basePrice
   */
  @NotNull @Valid 
  @Schema(name = "basePrice", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("basePrice")
  public BigDecimal getBasePrice() {
    return basePrice;
  }

  public void setBasePrice(BigDecimal basePrice) {
    this.basePrice = basePrice;
  }

  public ScheduleDTODto isActive(Boolean isActive) {
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

  public ScheduleDTODto createdAt(OffsetDateTime createdAt) {
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

  public ScheduleDTODto updatedAt(OffsetDateTime updatedAt) {
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

  public ScheduleDTODto route(RouteDTODto route) {
    this.route = route;
    return this;
  }

  /**
   * Get route
   * @return route
   */
  @NotNull @Valid 
  @Schema(name = "route", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("route")
  public RouteDTODto getRoute() {
    return route;
  }

  public void setRoute(RouteDTODto route) {
    this.route = route;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScheduleDTODto scheduleDTO = (ScheduleDTODto) o;
    return Objects.equals(this.id, scheduleDTO.id) &&
        Objects.equals(this.departureTime, scheduleDTO.departureTime) &&
        Objects.equals(this.arrivalTime, scheduleDTO.arrivalTime) &&
        Objects.equals(this.totalSeats, scheduleDTO.totalSeats) &&
        Objects.equals(this.availableSeats, scheduleDTO.availableSeats) &&
        Objects.equals(this.basePrice, scheduleDTO.basePrice) &&
        Objects.equals(this.isActive, scheduleDTO.isActive) &&
        Objects.equals(this.createdAt, scheduleDTO.createdAt) &&
        Objects.equals(this.updatedAt, scheduleDTO.updatedAt) &&
        Objects.equals(this.route, scheduleDTO.route);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, departureTime, arrivalTime, totalSeats, availableSeats, basePrice, isActive, createdAt, updatedAt, route);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScheduleDTODto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    departureTime: ").append(toIndentedString(departureTime)).append("\n");
    sb.append("    arrivalTime: ").append(toIndentedString(arrivalTime)).append("\n");
    sb.append("    totalSeats: ").append(toIndentedString(totalSeats)).append("\n");
    sb.append("    availableSeats: ").append(toIndentedString(availableSeats)).append("\n");
    sb.append("    basePrice: ").append(toIndentedString(basePrice)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    route: ").append(toIndentedString(route)).append("\n");
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

