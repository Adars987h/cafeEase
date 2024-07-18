package com.inn.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.InputStreamResource;

import java.io.File;

@Data
@AllArgsConstructor
public class BillResponse {

    InputStreamResource inputStreamResource;
    File file;
}
