package co.edu.uniquindio.poo.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonederoVirtual {

    private String nombre;

    private final List<Cliente> listaClientes = new ArrayList<>();

    private final List<TransaccionProgramada> listaProgramadas =
            new ArrayList<>();

    private final List<String> listaNotificaciones = new ArrayList<>();

    private int siguienteCodigo = 1;
    private int siguienteProgramada = 1;

    public MonederoVirtual(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Cliente> getListaClientes() {
        return Collections.unmodifiableList(listaClientes);
    }

    public List<TransaccionProgramada> getListaProgramadas() {
        return Collections.unmodifiableList(listaProgramadas);
    }

    public List<String> getListaNotificaciones() {
        return Collections.unmodifiableList(listaNotificaciones);
    }

    public Cliente registrarCliente(String documento, String nombre) {
        if (documento == null || documento.trim().isEmpty()
                || nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingresa el documento y el nombre."
            );
        }

        if (buscarCliente(documento.trim()) != null) {
            throw new IllegalArgumentException(
                    "El cliente ya existe."
            );
        }

        Cliente nuevo = new Cliente(
                documento.trim(), nombre.trim()
        );

        listaClientes.add(nuevo);
        return nuevo;
    }

    public Cliente buscarCliente(String documento) {
        return listaClientes.stream()
                .filter(c -> c.getDocumento().equals(documento))
                .findFirst()
                .orElse(null);
    }

    public void eliminarCliente(String documento) {
        Cliente cliente = buscarCliente(documento);

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "No se encontró el cliente."
            );
        }

        if (!cliente.getHistorial().isEmpty()) {
            throw new IllegalArgumentException(
                    "No se elimina un cliente con historial financiero."
            );
        }

        for (Monedero monedero : cliente.getMonederos()) {
            if (monedero.getSaldo() > 0) {
                throw new IllegalArgumentException(
                        "Primero retira o transfiere todos los saldos."
                );
            }
        }

        for (TransaccionProgramada programada : listaProgramadas) {
            if (programada.getOrigen() == cliente
                    || programada.getDestino() == cliente) {
                throw new IllegalArgumentException(
                        "Primero cancela las operaciones programadas "
                                + "de este cliente."
                );
            }
        }

        listaClientes.remove(cliente);
    }

    public void editarNombre(Cliente cliente, String nombre) {
        verificarCliente(cliente);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre está vacío."
            );
        }

        cliente.setNombre(nombre.trim());
    }

    public Monedero registrarMonedero(Cliente cliente, String nombre) {
        verificarCliente(cliente);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingresa un nombre de monedero."
            );
        }

        for (Monedero monedero : cliente.getMonederos()) {
            if (monedero.getNombre().equalsIgnoreCase(nombre.trim())) {
                throw new IllegalArgumentException(
                        "Ya existe ese monedero."
                );
            }
        }

        Monedero nuevo = new Monedero(nombre.trim());
        cliente.agregarMonedero(nuevo);

        return nuevo;
    }

    private void verificarCliente(Cliente cliente) {
        if (cliente == null || !listaClientes.contains(cliente)) {
            throw new IllegalArgumentException(
                    "Selecciona un cliente válido."
            );
        }
    }

    private void verificarMonedero(Cliente cliente, Monedero monedero) {
        verificarCliente(cliente);

        if (monedero == null
                || !cliente.getMonederos().contains(monedero)) {
            throw new IllegalArgumentException(
                    "Selecciona un monedero de ese cliente."
            );
        }
    }

    private void verificarMonto(double monto) {
        if (!Double.isFinite(monto)
                || monto < 0.01
                || monto > 1000000000) {
            throw new IllegalArgumentException(
                    "Ingresa un monto desde un centavo y hasta mil millones."
            );
        }

        if (Math.abs(monto * 100 - Math.rint(monto * 100)) > 0.00001) {
            throw new IllegalArgumentException(
                    "El monto admite como máximo dos decimales."
            );
        }
    }

    private boolean esAtipica(Cliente cliente, double monto) {
        double suma = 0;
        int cantidad = 0;

        List<Transaccion> historial = cliente.getHistorial();

        for (Transaccion transaccion : historial) {
            if (!transaccion.isRevertida()) {
                suma += transaccion.getMonto();
                cantidad++;
            }
        }

        if (cantidad < 2) {
            return false;
        }

        double promedio = suma / cantidad;
        double sumaCuadrados = 0;

        for (Transaccion transaccion : historial) {
            if (!transaccion.isRevertida()) {
                sumaCuadrados +=
                        (transaccion.getMonto() - promedio)
                                * (transaccion.getMonto() - promedio);
            }
        }

        double desviacion = Math.sqrt(sumaCuadrados / cantidad);

        return monto > promedio + 2 * desviacion;
    }

    private void registrar(Transaccion transaccion) {
        Cliente cliente = transaccion.getCliente();

        cliente.agregarTransaccion(transaccion);

        cliente.agregarPuntos(
                transaccion.calcularPuntos(),
                "Transacción #" + transaccion.getCodigo()
        );

        if (transaccion.isSospechosa()) {
            listaNotificaciones.add(
                    "ALERTA para " + cliente.getNombre()
                            + ": operación atípica #"
                            + transaccion.getCodigo()
            );
        }
    }

    public Transaccion depositar(Cliente cliente,
                                 Monedero monedero,
                                 double monto) {
        verificarMonedero(cliente, monedero);
        verificarMonto(monto);

        boolean alerta = esAtipica(cliente, monto);

        monedero.depositar(monto);

        Transaccion transaccion = new Deposito(
                siguienteCodigo++,
                cliente,
                monedero,
                monto,
                alerta
        );

        registrar(transaccion);
        return transaccion;
    }

    public Transaccion retirar(Cliente cliente,
                               Monedero monedero,
                               double monto) {
        verificarMonedero(cliente, monedero);
        verificarMonto(monto);

        double comision = cliente.tieneRetirosGratis(LocalDate.now())
                ? 0 : monto * 0.01;

        comision = Math.round(comision * 100.0) / 100.0;

        boolean alerta = esAtipica(cliente, monto);

        monedero.retirar(monto + comision);

        Transaccion transaccion = new Retiro(
                siguienteCodigo++,
                cliente,
                monedero,
                monto,
                comision,
                alerta
        );

        registrar(transaccion);
        avisarSaldoBajo(cliente, monedero);

        return transaccion;
    }

    public Transaccion transferir(Cliente cliente,
                                  Monedero desde,
                                  Cliente destinatario,
                                  Monedero hacia,
                                  double monto) {
        verificarMonedero(cliente, desde);
        verificarMonedero(destinatario, hacia);
        verificarMonto(monto);

        if (desde == hacia) {
            throw new IllegalArgumentException(
                    "Elige otro monedero de destino."
            );
        }

        double comision = monto * 0.01
                * (1 - cliente.getDescuentoPorRango());

        if (cliente.tieneDescuentoTransferencia()) {
            comision = comision * 0.90;
        }

        comision = Math.round(comision * 100.0) / 100.0;

        boolean alerta = esAtipica(cliente, monto);

        desde.retirar(monto + comision);
        hacia.depositar(monto);

        Transaccion transaccion = new Transferencia(
                siguienteCodigo++,
                cliente,
                desde,
                destinatario,
                hacia,
                monto,
                comision,
                alerta
        );

        registrar(transaccion);

        destinatario.agregarTransaccion(
                new TransferenciaRecibida(transaccion)
        );

        listaNotificaciones.add(
                "Transferencia recibida: "
                        + destinatario.getNombre() + " / $" + monto
        );

        avisarSaldoBajo(cliente, desde);

        return transaccion;
    }

    private void avisarSaldoBajo(Cliente cliente, Monedero monedero) {
        if (monedero.getSaldo() < 100) {
            listaNotificaciones.add(
                    "Saldo bajo: " + cliente.getNombre()
                            + " / " + monedero.getNombre()
            );
        }
    }

    public void revertir(Cliente cliente, int codigo) {
        verificarCliente(cliente);

        for (Transaccion transaccion : cliente.getHistorial()) {
            if (transaccion.getCodigo() == codigo) {

                if (transaccion instanceof TransferenciaRecibida) {
                    throw new IllegalArgumentException(
                            "La transferencia debe revertirla el emisor."
                    );
                }

                if (cliente.getPuntos()
                        < transaccion.calcularPuntos()) {
                    throw new IllegalArgumentException(
                            "No se puede revertir: los puntos de esta "
                                    + "operación ya fueron canjeados."
                    );
                }

                transaccion.revertir();

                cliente.agregarPuntos(
                        -transaccion.calcularPuntos(),
                        "Reversión #" + codigo
                );

                return;
            }
        }

        throw new IllegalArgumentException(
                "No existe esa transacción en el historial del cliente."
        );
    }

    public void canjear(Cliente cliente,
                        Monedero monedero,
                        int puntos) {
        verificarMonedero(cliente, monedero);

        if (puntos != 100 && puntos != 500 && puntos != 1000) {
            throw new IllegalArgumentException(
                    "Elige un beneficio válido."
            );
        }

        if (cliente.getPuntos() < puntos) {
            throw new IllegalArgumentException(
                    "No tienes suficientes puntos."
            );
        }

        if (puntos == 100 && cliente.tieneDescuentoTransferencia()) {
            throw new IllegalArgumentException(
                    "Ya tienes activo el descuento permanente."
            );
        }

        if (puntos == 100) {
            cliente.setDescuentoTransferencia(true);
        }

        if (puntos == 500) {
            cliente.extenderRetirosGratis(LocalDate.now());
        }

        if (puntos == 1000) {
            monedero.depositar(50);
        }

        cliente.agregarPuntos(-puntos, "Canje de beneficio");
    }

    public TransaccionProgramada programar(
            String tipo,
            Cliente cliente,
            Monedero desde,
            Cliente destino,
            Monedero hacia,
            double monto,
            LocalDate fecha,
            String frecuencia) {

        verificarMonedero(cliente, desde);
        verificarMonto(monto);

        if (!"Depósito".equals(tipo)
                && !"Transferencia".equals(tipo)) {
            throw new IllegalArgumentException(
                    "Elige depósito o transferencia."
            );
        }

        if (tipo.equals("Transferencia")) {
            verificarMonedero(destino, hacia);

            if (desde == hacia) {
                throw new IllegalArgumentException(
                        "El origen y el destino son iguales."
                );
            }
        }

        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Elige una fecha actual o futura."
            );
        }

        if (!"Única".equals(frecuencia)
                && !"Semanal".equals(frecuencia)
                && !"Mensual".equals(frecuencia)) {
            throw new IllegalArgumentException(
                    "Frecuencia inválida."
            );
        }

        TransaccionProgramada programada =
                new TransaccionProgramada(
                        siguienteProgramada++,
                        tipo,
                        cliente,
                        desde,
                        destino,
                        hacia,
                        monto,
                        fecha,
                        frecuencia
                );

        listaProgramadas.add(programada);
        ordenarProgramadas();

        listaNotificaciones.add(
                "Recordatorio: " + cliente.getNombre()
                        + " programó una operación para " + fecha
        );

        return programada;
    }

    public void cancelarProgramada(int codigo) {
        for (TransaccionProgramada programada : listaProgramadas) {
            if (programada.getCodigo() == codigo) {
                listaProgramadas.remove(programada);
                return;
            }
        }

        throw new IllegalArgumentException(
                "No existe esa operación programada."
        );
    }

    public String ejecutarPendientes(LocalDate hoy) {
        int realizadas = 0;
        int fallidas = 0;

        ordenarProgramadas();

        ArrayList<TransaccionProgramada> fallos = new ArrayList<>();

        while (true) {
            ordenarProgramadas();

            TransaccionProgramada pendiente = null;

            for (TransaccionProgramada programada : listaProgramadas) {
                if (programada.getFecha() != null
                        && !programada.getFecha().isAfter(hoy)
                        && !fallos.contains(programada)) {
                    pendiente = programada;
                    break;
                }
            }

            if (pendiente == null) {
                break;
            }

            try {
                if (pendiente.getTipo().equals("Depósito")) {
                    depositar(
                            pendiente.getOrigen(),
                            pendiente.getMonederoOrigen(),
                            pendiente.getMonto()
                    );
                } else {
                    transferir(
                            pendiente.getOrigen(),
                            pendiente.getMonederoOrigen(),
                            pendiente.getDestino(),
                            pendiente.getMonederoDestino(),
                            pendiente.getMonto()
                    );
                }

                pendiente.avanzar();
                realizadas++;

            } catch (IllegalArgumentException | IllegalStateException e) {
                fallos.add(pendiente);
                fallidas++;

                listaNotificaciones.add(
                        "No se ejecutó la programada #"
                                + pendiente.getCodigo()
                                + ": " + e.getMessage()
                );
            }
        }

        ordenarProgramadas();

        return "Operaciones realizadas: " + realizadas
                + " | Fallidas: " + fallidas;
    }

    private void ordenarProgramadas() {
        listaProgramadas.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) {
                return 0;
            }

            if (a.getFecha() == null) {
                return 1;
            }

            if (b.getFecha() == null) {
                return -1;
            }

            return a.getFecha().compareTo(b.getFecha());
        });
    }

    public String reporte(Cliente cliente) {
        verificarCliente(cliente);

        double depositos = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && t.getTipo().equals("Depósito"))
                .mapToDouble(t -> t.getMonto())
                .sum();

        double retiros = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && t.getTipo().equals("Retiro"))
                .mapToDouble(t -> t.getMonto())
                .sum();

        double transferencias = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && t.getTipo().equals("Transferencia"))
                .mapToDouble(t -> t.getMonto())
                .sum();

        long alertas = cliente.getHistorial().stream()
                .filter(t -> t.isSospechosa() && !t.isRevertida())
                .count();

        double comisiones = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida())
                .mapToDouble(t -> t.getComision())
                .sum();

        double recibidas = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && t instanceof TransferenciaRecibida)
                .mapToDouble(t -> t.getMonto())
                .sum();

        double promedioGasto = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && (t instanceof Retiro
                        || t instanceof Transferencia))
                .mapToDouble(t -> t.getMonto() + t.getComision())
                .average()
                .orElse(0);

        double mayorGasto = cliente.getHistorial().stream()
                .filter(t -> !t.isRevertida()
                        && (t instanceof Retiro
                        || t instanceof Transferencia))
                .mapToDouble(t -> t.getMonto() + t.getComision())
                .max()
                .orElse(0);

        return "REPORTE DE " + cliente.getNombre()
                + "\nDepósitos: $" + String.format("%.2f", depositos)
                + "\nRetiros: $" + String.format("%.2f", retiros)
                + "\nTransferencias: $" + String.format("%.2f", transferencias)
                + "\nTotal de gastos: $"
                + String.format("%.2f", retiros + transferencias + comisiones)
                + "\nComisiones: $" + String.format("%.2f", comisiones)
                + "\nTransferencias recibidas: $"
                + String.format("%.2f", recibidas)
                + "\nGasto promedio por operación: $"
                + String.format("%.2f", promedioGasto)
                + "\nMayor gasto: $" + String.format("%.2f", mayorGasto)
                + "\nPatrón predominante: "
                + (retiros + transferencias == 0
                ? "Sin gastos"
                : retiros > transferencias
                ? "Retiros"
                : "Transferencias")
                + "\nAlertas: " + alertas
                + "\nPuntos actuales: " + cliente.getPuntos()
                + "\nRango: " + cliente.getRango();
    }
}