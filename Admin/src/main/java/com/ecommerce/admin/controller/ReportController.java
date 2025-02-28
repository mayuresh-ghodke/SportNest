package com.ecommerce.admin.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Principal;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.library.dto.ProductDto;
import com.ecommerce.library.service.ProductService;

import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@RestController
@AllArgsConstructor
public class ReportController {

    private ProductService productService;

    @GetMapping("/products/exportReport")
    public String exportReport(@RequestParam(required = false) String reportFormat, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (reportFormat == null || reportFormat.isEmpty()) {
            return "Report format not specified";
        }

        List<ProductDto> products = productService.allProduct();
        String path = "C:/reports"; // Ensure this path exists on your system

        // Check if the directory exists, if not, create it
        File reportDirectory = new File(path);
        if (!reportDirectory.exists()) {
            boolean dirCreated = reportDirectory.mkdirs();
            if (!dirCreated) {
                return "Failed to create directory for reports.";
            }
        }

        try {
            // Load the JRXML report template file from resources
            File file = ResourceUtils.getFile("classpath:reports/ProductsReport.jrxml");

            // Compile the JasperReport from the JRXML file
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            // Data source for the report (list of products)
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(products);

            // Parameters for the report (can be empty if not needed)
            Map<String, Object> parameters = new HashMap<>();

            // Fill the report with data and parameters
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export the report based on the format (csv or PDF)
            if (reportFormat.equalsIgnoreCase("csv")) {
                JasperExportManager.exportReportToHtmlFile(jasperPrint, path + "/products_report.csv");
            } else if (reportFormat.equalsIgnoreCase("pdf")) {
                JasperExportManager.exportReportToPdfFile(jasperPrint, path + "/products_report.pdf");
            } else {
                return "Invalid report format specified";
            }

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("Exception: " + fileNotFoundException);
        } catch (JRException jrException) {
            jrException.printStackTrace();
        }

        return "Report generated successfully";
    }

}
