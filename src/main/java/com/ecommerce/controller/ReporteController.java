package com.ecommerce.controller;

import com.ecommerce.dto.SalesReportResponse;
import com.ecommerce.dto.VentaDetalladaResponse;
import com.ecommerce.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<SalesReportResponse> resumen() {
        return ResponseEntity.ok(reporteService.resumen());
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<VentaDetalladaResponse>> listarVentas() {
        return ResponseEntity.ok(reporteService.listarVentas());
    }

    @GetMapping("/ventas.csv")
    public CompletableFuture<ResponseEntity<byte[]>> csv() {
        return reporteService.exportCsv().thenApply(csvContent ->
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas_techstore.csv")
                        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                        .body(csvContent.getBytes(StandardCharsets.UTF_8))
        );
    }

    @GetMapping("/ventas.xls")
    public CompletableFuture<ResponseEntity<byte[]>> xls() {
        return reporteService.exportXls().thenApply(xlsContent ->
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas_techstore.xls")
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
                        .body(xlsContent.getBytes(StandardCharsets.UTF_8))
        );
    }

    @GetMapping("/ventas.json")
    public CompletableFuture<ResponseEntity<byte[]>> json() {
        return reporteService.exportJson().thenApply(jsonContent ->
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas_techstore.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonContent.getBytes(StandardCharsets.UTF_8))
        );
    }
}
