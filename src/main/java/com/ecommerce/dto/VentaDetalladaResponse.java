package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaDetalladaResponse {
    private Long id;
    private LocalDateTime fecha;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String direccionEnvio;
    private String productosResumen;
    private int cantidadTotal;
    private BigDecimal costoTotal;
    private BigDecimal totalVenta;
    private BigDecimal utilidad;
    private String metodoPago;
    private String referenciaPago;
    private String estadoPago;
    private String estadoPedido;
    private List<DetallePedidoResponse> detalles;

    public VentaDetalladaResponse() {}

    public VentaDetalladaResponse(Long id, LocalDateTime fecha, String clienteNombre,
                                  String clienteEmail, String clienteTelefono,
                                  String direccionEnvio, String productosResumen,
                                  int cantidadTotal, BigDecimal costoTotal,
                                  BigDecimal totalVenta, BigDecimal utilidad,
                                  String metodoPago, String referenciaPago,
                                  String estadoPago, String estadoPedido,
                                  List<DetallePedidoResponse> detalles) {
        this.id = id;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.clienteTelefono = clienteTelefono;
        this.direccionEnvio = direccionEnvio;
        this.productosResumen = productosResumen;
        this.cantidadTotal = cantidadTotal;
        this.costoTotal = costoTotal;
        this.totalVenta = totalVenta;
        this.utilidad = utilidad;
        this.metodoPago = metodoPago;
        this.referenciaPago = referenciaPago;
        this.estadoPago = estadoPago;
        this.estadoPedido = estadoPedido;
        this.detalles = detalles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteEmail() { return clienteEmail; }
    public void setEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }

    public String getProductosResumen() { return productosResumen; }
    public void setProductosResumen(String productosResumen) { this.productosResumen = productosResumen; }

    public int getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(int cantidadTotal) { this.cantidadTotal = cantidadTotal; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public BigDecimal getTotalVenta() { return totalVenta; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }

    public BigDecimal getUtilidad() { return utilidad; }
    public void setUtilidad(BigDecimal utilidad) { this.utilidad = utilidad; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }

    public List<DetallePedidoResponse> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoResponse> detalles) { this.detalles = detalles; }
}
