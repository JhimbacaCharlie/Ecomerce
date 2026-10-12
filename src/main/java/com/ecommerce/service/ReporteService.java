package com.ecommerce.service;

import com.ecommerce.dto.DetallePedidoResponse;
import com.ecommerce.dto.SalesReportResponse;
import com.ecommerce.dto.VentaDetalladaResponse;
import com.ecommerce.entity.DetallePedido;
import com.ecommerce.entity.Pedido;
import com.ecommerce.enums.EstadoPago;
import com.ecommerce.enums.EstadoPedido;
import com.ecommerce.repository.PedidoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReporteService {

    private final PedidoRepository pedidos;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReporteService(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public SalesReportResponse resumen() {
        List<Pedido> all = pedidos.findAll();
        long total = all.size();
        long paid = 0;
        long cancel = 0;
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal costo = BigDecimal.ZERO;
        long unitsSold = 0;

        for (Pedido p : all) {
            if (p.getEstado() == EstadoPedido.CANCELADO) {
                cancel++;
            }
            if (p.getEstadoPago() == EstadoPago.CONFIRMADO) {
                paid++;
                ingresos = ingresos.add(p.getTotal());
                for (DetallePedido d : p.getDetalles()) {
                    unitsSold += d.getCantidad();
                    BigDecimal cu = (d.getProducto() != null && d.getProducto().getCostoUnitario() != null)
                            ? d.getProducto().getCostoUnitario()
                            : BigDecimal.ZERO;
                    costo = costo.add(cu.multiply(BigDecimal.valueOf(d.getCantidad())));
                }
            }
        }

        BigDecimal utilidad = ingresos.subtract(costo);
        BigDecimal ticket = paid == 0 ? BigDecimal.ZERO : ingresos.divide(BigDecimal.valueOf(paid), 2, RoundingMode.HALF_UP);

        return new SalesReportResponse(total, paid, unitsSold, ingresos, costo, utilidad, ticket, cancel, unitsSold);
    }

    @Transactional(readOnly = true)
    public List<VentaDetalladaResponse> listarVentas() {
        List<Pedido> list = pedidos.findAllByOrderByFechaPedidoDesc();
        List<VentaDetalladaResponse> result = new ArrayList<>();

        for (Pedido p : list) {
            BigDecimal costoVenta = BigDecimal.ZERO;
            int cantidadTotal = 0;
            StringBuilder sbProd = new StringBuilder();
            List<DetallePedidoResponse> detallesResp = new ArrayList<>();

            for (DetallePedido d : p.getDetalles()) {
                cantidadTotal += d.getCantidad();
                BigDecimal cu = (d.getProducto() != null && d.getProducto().getCostoUnitario() != null)
                        ? d.getProducto().getCostoUnitario()
                        : BigDecimal.ZERO;
                costoVenta = costoVenta.add(cu.multiply(BigDecimal.valueOf(d.getCantidad())));

                String prodNom = d.getProducto() != null ? d.getProducto().getNombre() : "Producto";
                if (sbProd.length() > 0) sbProd.append(", ");
                sbProd.append(d.getCantidad()).append("x ").append(prodNom);

                DetallePedidoResponse dr = new DetallePedidoResponse();
                dr.setId(d.getId());
                dr.setProductoNombre(prodNom);
                dr.setCantidad(d.getCantidad());
                dr.setPrecioUnitario(d.getPrecioUnitario());
                dr.setSubtotal(d.getSubtotal());
                detallesResp.add(dr);
            }

            BigDecimal totalVenta = p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO;
            BigDecimal margen = totalVenta.subtract(costoVenta);

            result.add(new VentaDetalladaResponse(
                    p.getId(),
                    p.getFechaPedido(),
                    p.getUsuario() != null ? p.getUsuario().getNombre() : "Cliente",
                    p.getUsuario() != null ? p.getUsuario().getEmail() : "-",
                    p.getTelefonoContacto(),
                    p.getDireccionEnvio(),
                    sbProd.toString(),
                    cantidadTotal,
                    costoVenta,
                    totalVenta,
                    margen,
                    p.getMetodoPago() != null ? p.getMetodoPago().name() : "NO SELECCIONADO",
                    p.getReferenciaPago() != null ? p.getReferenciaPago() : "-",
                    p.getEstadoPago().name(),
                    p.getEstado().name(),
                    detallesResp
            ));
        }

        return result;
    }

    @Async("reportExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<String> exportCsv() {
        StringBuilder b = new StringBuilder();
        // BOM UTF-8 para apertura correcta en Microsoft Excel
        b.append('\uFEFF');
        b.append("ID,FECHA,CLIENTE,EMAIL,TELEFONO,DIRECCION,PRODUCTOS,UNIDADES,COSTO_TOTAL,TOTAL_VENTA,UTILIDAD,METODO_PAGO,REFERENCIA_PAGO,ESTADO_PAGO,ESTADO_PEDIDO\n");

        for (VentaDetalladaResponse v : listarVentas()) {
            b.append(v.getId()).append(',')
             .append(v.getFecha() != null ? v.getFecha().format(DATE_FMT) : "").append(',')
             .append(csv(v.getClienteNombre())).append(',')
             .append(csv(v.getClienteEmail())).append(',')
             .append(csv(v.getClienteTelefono())).append(',')
             .append(csv(v.getDireccionEnvio())).append(',')
             .append(csv(v.getProductosResumen())).append(',')
             .append(v.getCantidadTotal()).append(',')
             .append(v.getCostoTotal()).append(',')
             .append(v.getTotalVenta()).append(',')
             .append(v.getUtilidad()).append(',')
             .append(csv(v.getMetodoPago())).append(',')
             .append(csv(v.getReferenciaPago())).append(',')
             .append(csv(v.getEstadoPago())).append(',')
             .append(csv(v.getEstadoPedido())).append('\n');
        }

        return CompletableFuture.completedFuture(b.toString());
    }

    @Async("reportExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<String> exportXls() {
        StringBuilder b = new StringBuilder();
        b.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">");
        b.append("<head><meta charset=\"UTF-8\"><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>Historial de Ventas</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head>");
        b.append("<body>");
        b.append("<h2>TechStore - Reporte de Ventas e Ingresos</h2>");
        b.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" style=\"border-collapse:collapse; font-family:Arial, sans-serif; font-size:12px;\">");
        b.append("<tr style=\"background-color:#1e3a5f; color:#ffffff; font-weight:bold;\">");
        b.append("<th>ID</th><th>Fecha</th><th>Cliente</th><th>Email</th><th>Teléfono</th><th>Dirección</th><th>Productos</th><th>Uds</th><th>Costo (S/.)</th><th>Total (S/.)</th><th>Utilidad (S/.)</th><th>Método Pago</th><th>Referencia</th><th>Estado Pago</th><th>Estado Pedido</th>");
        b.append("</tr>");

        for (VentaDetalladaResponse v : listarVentas()) {
            b.append("<tr>");
            b.append("<td>").append(v.getId()).append("</td>");
            b.append("<td>").append(v.getFecha() != null ? v.getFecha().format(DATE_FMT) : "").append("</td>");
            b.append("<td>").append(escHtml(v.getClienteNombre())).append("</td>");
            b.append("<td>").append(escHtml(v.getClienteEmail())).append("</td>");
            b.append("<td>").append(escHtml(v.getClienteTelefono())).append("</td>");
            b.append("<td>").append(escHtml(v.getDireccionEnvio())).append("</td>");
            b.append("<td>").append(escHtml(v.getProductosResumen())).append("</td>");
            b.append("<td align=\"center\">").append(v.getCantidadTotal()).append("</td>");
            b.append("<td align=\"right\">").append(v.getCostoTotal()).append("</td>");
            b.append("<td align=\"right\" style=\"font-weight:bold;\">").append(v.getTotalVenta()).append("</td>");
            b.append("<td align=\"right\" style=\"color:#007b48; font-weight:bold;\">").append(v.getUtilidad()).append("</td>");
            b.append("<td>").append(escHtml(v.getMetodoPago())).append("</td>");
            b.append("<td>").append(escHtml(v.getReferenciaPago())).append("</td>");
            b.append("<td align=\"center\">").append(escHtml(v.getEstadoPago())).append("</td>");
            b.append("<td align=\"center\">").append(escHtml(v.getEstadoPedido())).append("</td>");
            b.append("</tr>");
        }

        b.append("</table></body></html>");
        return CompletableFuture.completedFuture(b.toString());
    }

    @Async("reportExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<String> exportJson() {
        StringBuilder b = new StringBuilder("[\n");
        List<VentaDetalladaResponse> list = listarVentas();
        for (int i = 0; i < list.size(); i++) {
            VentaDetalladaResponse v = list.get(i);
            b.append("  {\n")
             .append("    \"id\": ").append(v.getId()).append(",\n")
             .append("    \"fecha\": \"").append(v.getFecha() != null ? v.getFecha().format(DATE_FMT) : "").append("\",\n")
             .append("    \"cliente\": \"").append(json(v.getClienteNombre())).append("\",\n")
             .append("    \"email\": \"").append(json(v.getClienteEmail())).append("\",\n")
             .append("    \"telefono\": \"").append(json(v.getClienteTelefono())).append("\",\n")
             .append("    \"direccion\": \"").append(json(v.getDireccionEnvio())).append("\",\n")
             .append("    \"productos\": \"").append(json(v.getProductosResumen())).append("\",\n")
             .append("    \"unidades\": ").append(v.getCantidadTotal()).append(",\n")
             .append("    \"costoTotal\": ").append(v.getCostoTotal()).append(",\n")
             .append("    \"totalVenta\": ").append(v.getTotalVenta()).append(",\n")
             .append("    \"utilidad\": ").append(v.getUtilidad()).append(",\n")
             .append("    \"metodoPago\": \"").append(json(v.getMetodoPago())).append("\",\n")
             .append("    \"referenciaPago\": \"").append(json(v.getReferenciaPago())).append("\",\n")
             .append("    \"estadoPago\": \"").append(json(v.getEstadoPago())).append("\",\n")
             .append("    \"estadoPedido\": \"").append(json(v.getEstadoPedido())).append("\"\n")
             .append("  }");
            if (i < list.size() - 1) b.append(',');
            b.append('\n');
        }
        b.append(']');
        return CompletableFuture.completedFuture(b.toString());
    }

    private String csv(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private String json(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
