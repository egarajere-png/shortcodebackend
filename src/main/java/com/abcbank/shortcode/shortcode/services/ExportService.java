package com.abcbank.shortcode.shortcode.services;

import java.io.ByteArrayInputStream;

public interface ExportService {

    ByteArrayInputStream exportRegistryToExcel(String status);

}