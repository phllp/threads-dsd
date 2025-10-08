package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MatrixParser {
    private MatrixParser() {}

    /** Continua funcionando para arquivos no filesystem (ex.: durante desenvolvimento). */
    public static int[][] readMatrix(Path file) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(br);
        }
    }

    /** Novo: lê a matriz a partir de um InputStream (útil para resources no classpath). */
    public static int[][] readMatrix(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return parse(br);
        }
    }

    /**
     * Novo: lê diretamente de um recurso dentro de src/main/resources.
     * Ex.: readMatrixResource("/malhas/malha-exemplo-1.txt")
     */
    public static int[][] readMatrixResource(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath não pode ser nulo/vazio.");
        }
        // Garante que começa com "/"
        String rp = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

        InputStream in = MatrixParser.class.getResourceAsStream(rp);
        if (in == null) {
            throw new FileNotFoundException("Recurso não encontrado no classpath: " + rp);
        }
        return readMatrix(in);
    }

    // ---------------------- implementação interna reaproveitada ----------------------

    private static int[][] parse(BufferedReader br) throws IOException {
        String line1 = nextDataLine(br);
        String line2 = nextDataLine(br);

        if (line1 == null || line2 == null) {
            throw new IOException("Arquivo incompleto: faltam linhas de dimensões.");
        }

        final int rows, cols;
        try {
            rows = Integer.parseInt(line1.trim());
            cols = Integer.parseInt(line2.trim());
        } catch (NumberFormatException e) {
            throw new IOException("As duas primeiras linhas devem conter inteiros (linhas e colunas).", e);
        }
        if (rows <= 0 || cols <= 0) {
            throw new IOException("Linhas e colunas devem ser > 0. Lidas: " + rows + "x" + cols);
        }

        int[][] grid = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            String line = nextDataLine(br);
            if (line == null) {
                throw new IOException("Arquivo terminou antes de ler todas as " + rows + " linhas da matriz (faltou a linha " + (r + 1) + ").");
            }

            // Aceita separação por tabs ou espaços múltiplos
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != cols) {
                throw new IOException(String.format(
                        "Quantidade de colunas inválida na linha %d. Esperado: %d, Encontrado: %d",
                        r + 1, cols, tokens.length
                ));
            }

            for (int c = 0; c < cols; c++) {
                try {
                    int val = Integer.parseInt(tokens[c]);
                    grid[r][c] = val;
                } catch (NumberFormatException e) {
                    throw new IOException(String.format(
                            "Token não numérico na linha %d, coluna %d: \"%s\"", r + 1, c + 1, tokens[c]
                    ), e);
                }
            }
        }

        return grid;
    }

    /** Lê próxima linha de dados, ignorando linhas vazias/whitespace. */
    private static String nextDataLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) return line;
        }
        return null;
    }

    public static void printMatrix(int[][] grid) {
        if (grid == null || grid.length == 0) {
            System.out.println("(matriz vazia)");
            return;
        }
        for (int[] row : grid) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < row.length; j++) {
                if (j > 0) {
                    if (row[j] > 9) {
                        sb.append(row[j]);
                    } else if (row[j] >= 0 && row[j] <=9) {
                        sb.append("0"+row[j]);
                    }
                }
                sb.append(' ');
            }
            System.out.println(sb);
        }
    }
}
