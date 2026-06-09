import java.util.Scanner;

public class AeroUSAC {

    
    static final char LIBRE = 'L';
    static final char OCUPADO = 'X';
    static final char BLOQUEADO = 'B';

    static char[][] cabina = new char[20][6];
    static int ingresos = 0;

    public static void main(String[] args) {
        inicializarCabina();
        Scanner sc = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n=== AERO-USAC: SISTEMA DE ABORDAJE ===");
            System.out.println("1. Venta de Boleto Individual");
            System.out.println("2. Buscar Boletos Contiguos");
            System.out.println("3. Asignacion Automatica");
            System.out.println("4. Mostrar Mapa de la Cabina");
            System.out.println("5. Reporte de Vuelo");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1: ventaIndividual(sc); break;
                case 2: buscarContiguos(sc); break;
                case 3: asignacionAutomatica(sc); break;
                case 4: mostrarCabina(); break;
                case 5: reporteVuelo(); break;
                case 6: System.out.println("Hasta luego."); break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 6);

        sc.close();
    }

    static void inicializarCabina() {
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 6; j++)
                cabina[i][j] = LIBRE;
    }

    static int letraAColumna(char letra) {
        letra = Character.toUpperCase(letra);
        if (letra < 'A' || letra > 'F') return -1;
        return letra - 'A';
    }

    static char columnaALetra(int col) {
        return (char) ('A' + col);
    }

    static int[] parsearAsiento(String codigo) {
        if (codigo == null || codigo.length() < 2) return null;

        char letra = codigo.charAt(0);
        int col = letraAColumna(letra);
        if (col == -1) {
            System.out.println("Error: La columna ingresada no existe. Debe ingresar una letra entre A y F.");
            return null;
        }

        int fila;
        try {
            fila = Integer.parseInt(codigo.substring(1));
        } catch (NumberFormatException e) {
            System.out.println("Error: Formato invalido. Ejemplo correcto: C15");
            return null;
        }

        if (fila < 1 || fila > 20) {
            System.out.println("Error: El numero de fila debe estar entre 1 y 20.");
            return null;
        }

        return new int[]{fila - 1, col}; // índices base-0
    }

    static int precioFila(int filaIdx) {
        return (filaIdx < 5) ? 150 : 50;
    }

    static void mostrarCabina() {
        System.out.println("\n     A   B   C      D   E    F");
        for (int i = 0; i < 20; i++) {
            String num = (i + 1 < 10) ? " " + (i + 1) : "" + (i + 1);
            System.out.print(num + ": ");
            for (int j = 0; j < 6; j++) {
                if (j == 3) System.out.print("|| ");
                System.out.print("[" + cabina[i][j] + "] ");
            }
            System.out.println();
        }
    }

    static void ventaIndividual(Scanner sc) {
        System.out.print("Ingrese el asiento que desea comprar: ");
        String codigo = sc.nextLine().trim().toUpperCase();

        int[] pos = parsearAsiento(codigo);
        if (pos == null) return;

        int fila = pos[0], col = pos[1];
        String codigoLabel = columnaALetra(col) + "" + (fila + 1);

        if (cabina[fila][col] == OCUPADO) {
            System.out.println("Error: El asiento " + codigoLabel + " ya se encuentra ocupado.");
            return;
        }
        if (cabina[fila][col] == BLOQUEADO) {
            System.out.println("Error: El asiento " + codigoLabel + " esta bloqueado por distanciamiento VIP.");
            return;
        }

        if (fila < 5) {
            venderVIP(fila, col, codigoLabel);
        } else {
            cabina[fila][col] = OCUPADO;
            ingresos += 50;
            System.out.println("Asiento " + codigoLabel + " vendido exitosamente.");
            System.out.println("Total a pagar: $50");
        }
    }

    static void venderVIP(int fila, int col, String codigoLabel) {
        int precio = 150;
        boolean hayAdelante = (fila - 1 >= 0);
        boolean hayAtras    = (fila + 1 <= 19);

        if (hayAdelante && cabina[fila - 1][col] != LIBRE) {
            System.out.println("Error: No se puede vender el asiento " + codigoLabel +
                    " porque no se puede garantizar el distanciamiento de Primera Clase.");
            System.out.println("El asiento " + columnaALetra(col) + (fila) + " ya esta ocupado.");
            return;
        }
        if (hayAtras && cabina[fila + 1][col] != LIBRE) {
            System.out.println("Error: No se puede vender el asiento " + codigoLabel +
                    " porque no se puede garantizar el distanciamiento de Primera Clase.");
            System.out.println("El asiento " + columnaALetra(col) + (fila + 2) + " ya esta ocupado.");
            return;
        }

        cabina[fila][col] = OCUPADO;
        ingresos += precio;

        String bloqueados = "";
        if (hayAdelante) {
            cabina[fila - 1][col] = BLOQUEADO;
            bloqueados += columnaALetra(col) + "" + (fila);
        }
        if (hayAtras) {
            cabina[fila + 1][col] = BLOQUEADO;
            if (!bloqueados.isEmpty()) bloqueados += " y ";
            bloqueados += columnaALetra(col) + "" + (fila + 2);
        }

        System.out.println("Asiento VIP " + codigoLabel + " vendido exitosamente.");
        if (!bloqueados.isEmpty())
            System.out.println("Se bloquearon los asientos " + bloqueados + " por distanciamiento.");
        System.out.println("Total a pagar: $" + precio);
    }

    static int[] seleccionarClase(Scanner sc) {
        System.out.println("¿En que clase desea buscar sus asientos?");
        System.out.println("1. Primera Clase (Filas 1 a 5)  - $150 c/u");
        System.out.println("2. Clase Economica (Filas 6 a 20) - $50 c/u");
        System.out.print("Seleccione: ");
        String entrada = sc.nextLine().trim();
        if (entrada.equals("1")) return new int[]{0, 4};
        if (entrada.equals("2")) return new int[]{5, 19};
        System.out.println("Opcion invalida.");
        return null;
    }

    static void buscarContiguos(Scanner sc) {
        int[] rango = seleccionarClase(sc);
        if (rango == null) return;

        int inicio = rango[0], fin = rango[1];
        int precio = (inicio == 0) ? 150 : 50;

        // Parejas válidas respetando pasillo: (0,1),(1,2),(3,4),(4,5)
        int[][] parejas = {{0,1},{1,2},{3,4},{4,5}};

        for (int f = inicio; f <= fin; f++) {
            for (int[] par : parejas) {
                int c1 = par[0], c2 = par[1];
                if (cabina[f][c1] == LIBRE && cabina[f][c2] == LIBRE) {
                    cabina[f][c1] = OCUPADO;
                    cabina[f][c2] = OCUPADO;
                    ingresos += precio * 2;
                    String a1 = "" + columnaALetra(c1) + (f + 1);
                    String a2 = "" + columnaALetra(c2) + (f + 1);
                    System.out.println("Asientos contiguos asignados: " + a1 + " y " + a2);
                    System.out.println("Total a pagar: $" + (precio * 2));
                    return;
                }
            }
        }
        System.out.println("No se encontraron 2 asientos contiguos disponibles en la clase seleccionada.");
    }

    static void asignacionAutomatica(Scanner sc) {
        int[] rango = seleccionarClase(sc);
        if (rango == null) return;

        int inicio = rango[0], fin = rango[1];
        int precio = (inicio == 0) ? 150 : 50;

        int izq = 0, der = 0;
        for (int f = inicio; f <= fin; f++) {
            for (int c = 0; c < 3; c++) if (cabina[f][c] == OCUPADO) izq++;
            for (int c = 3; c < 6; c++) if (cabina[f][c] == OCUPADO) der++;
        }

        boolean balanceados = (izq == der);
        int colInicio, colFin;

        if (izq <= der) { colInicio = 0; colFin = 2; }
        else            { colInicio = 3; colFin = 5; }

        if (balanceados)
            System.out.println("Ambos lados se encuentran balanceados.");

        for (int f = inicio; f <= fin; f++) {
            for (int c = colInicio; c <= colFin; c++) {
                if (cabina[f][c] == LIBRE) {
                    cabina[f][c] = OCUPADO;
                    ingresos += precio;
                    String asiento = "" + columnaALetra(c) + (f + 1);
                    if (!balanceados)
                        System.out.println("Para mantener el balance del vuelo, se le ha asignado el asiento " + asiento + ".");
                    else
                        System.out.println("Se le ha asignado el asiento " + asiento + ".");
                    System.out.println("Total a pagar: $" + precio);
                    return;
                }
            }
        }
        System.out.println("No hay asientos disponibles en la clase seleccionada.");
    }

    static void reporteVuelo() {
        int libres = 0, ocupados = 0, bloqueados = 0;
        int ocIzq = 0, ocDer = 0;
        int totalIzq = 20 * 3, totalDer = 20 * 3;

        for (int f = 0; f < 20; f++) {
            for (int c = 0; c < 6; c++) {
                if      (cabina[f][c] == LIBRE)     libres++;
                else if (cabina[f][c] == OCUPADO)   { ocupados++; if (c < 3) ocIzq++; else ocDer++; }
                else if (cabina[f][c] == BLOQUEADO) bloqueados++;
            }
        }

        int pctIzq = (ocIzq * 100) / totalIzq;
        int pctDer = (ocDer * 100) / totalDer;

        System.out.println("\n=== REPORTE DE VUELO ===");
        System.out.println("Asientos libres:     " + libres);
        System.out.println("Asientos ocupados:   " + ocupados);
        System.out.println("Asientos bloqueados: " + bloqueados);
        System.out.println("Ingresos recaudados: $" + ingresos);
        System.out.println("Ocupacion lado izquierdo: " + pctIzq + "%");
        System.out.println("Ocupacion lado derecho:   " + pctDer + "%");
    }
}