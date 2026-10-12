package com.ecommerce.dto;
import java.math.BigDecimal;
public record SalesReportResponse(long pedidosTotales,long pedidosPagados,long productosVendidos,BigDecimal ingresos,BigDecimal costoMercaderia,BigDecimal utilidadEstimada,BigDecimal ticketPromedio,long pedidosCancelados,long unidadesSalidas) {}
