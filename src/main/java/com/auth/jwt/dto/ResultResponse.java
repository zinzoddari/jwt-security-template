package com.auth.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Builder
@AllArgsConstructor
public class ResultResponse<T> {

    @JsonProperty
    private int status;

    @JsonProperty
    private String message;

    @JsonProperty
    private T data;

    public void setData(T data) {
        this.data = data;
    }
}
