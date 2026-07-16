package com.abcbank.shortcode.shortcode.services;

import java.io.ByteArrayInputStream;


/**
 * Service responsible for exporting shortcode registry reports.
 */


public interface ExportService {

    ByteArrayInputStream exportRegistryToExcel(
        String status,String startDate,String endDate);

}