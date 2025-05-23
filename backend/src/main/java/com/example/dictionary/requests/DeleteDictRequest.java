package com.example.dictionary.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteDictRequest {
    @NotNull
    private String dictId;
}
