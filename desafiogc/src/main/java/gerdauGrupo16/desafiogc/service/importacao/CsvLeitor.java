package gerdauGrupo16.desafiogc.service.importacao;

/*
 * ARQUIVO: CsvLeitor.java
 * TIPO: classe utilitária (final, só métodos estáticos), sem dependências externas.
 * FUNÇÃO: lê arquivos CSV. Aceita separador ponto e vírgula, vírgula ou TAB (detectado no cabeçalho);
 *   campos entre aspas com aspas duplas e quebra de linha dentro do campo; UTF-8 com ou sem BOM ou
 *   Windows-1252 (o CSV antigo do Excel); cabeçalhos com acento ou maiúsculas viram minúsculos com
 *   sublinhado (Código do Serviço vira codigo_do_servico).
 * CLASSES INTERNAS: Linha (record) é uma linha de dados com o número no arquivo (cabeçalho = 1) e o método
 *   get(apelidos), que devolve o primeiro valor não vazio; Resultado (record) traz o cabeçalho e as linhas
 *   e o método temColuna(apelidos).
 * MÉTODOS:
 * - ler(bytes): lê o arquivo inteiro e devolve cabeçalho e linhas.
 * - normalizarCabecalho(c): padroniza o nome de uma coluna.
 * - decodificar(bytes): tenta UTF-8 e, se falhar, usa Windows-1252.
 * - detectarSeparador(texto): escolhe o separador pela primeira linha.
 * - dividir(texto, sep): quebra o texto em registros e campos respeitando as aspas.
 * - registroVazio(campos): verdadeiro se todos os campos estão em branco.
 */

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CsvLeitor {

    private CsvLeitor() { }

    public record Linha(int numero, Map<String, String> campos) {

        public String get(String... apelidos) {
            for (String a : apelidos) {
                String v = campos.get(a);
                if (v != null && !v.isBlank()) return v.strip();
            }
            return "";
        }
    }

    public record Resultado(List<String> cabecalho, List<Linha> linhas) {
        public boolean temColuna(String... apelidos) {
            for (String a : apelidos) if (cabecalho.contains(a)) return true;
            return false;
        }
    }

    public static Resultado ler(byte[] bytes) {
        String texto = decodificar(bytes);
        if (!texto.isEmpty() && texto.charAt(0) == '﻿') texto = texto.substring(1);
        char separador = detectarSeparador(texto);
        List<List<String>> registros = dividir(texto, separador);
        if (registros.isEmpty()) return new Resultado(List.of(), List.of());

        List<String> cabecalho = new ArrayList<>();
        for (String c : registros.get(0)) cabecalho.add(normalizarCabecalho(c));

        List<Linha> linhas = new ArrayList<>();
        for (int i = 1; i < registros.size(); i++) {
            List<String> campos = registros.get(i);
            Map<String, String> mapa = new HashMap<>();
            for (int j = 0; j < cabecalho.size(); j++) {
                mapa.put(cabecalho.get(j), j < campos.size() ? campos.get(j) : "");
            }
            linhas.add(new Linha(i + 1, mapa));
        }
        return new Resultado(cabecalho, linhas);
    }

    public static String normalizarCabecalho(String c) {
        String s = Normalizer.normalize(c == null ? "" : c, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private static String decodificar(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    private static char detectarSeparador(String texto) {
        int fim = texto.indexOf('\n');
        String primeira = fim < 0 ? texto : texto.substring(0, fim);
        long pv = primeira.chars().filter(c -> c == ';').count();
        long v = primeira.chars().filter(c -> c == ',').count();
        long t = primeira.chars().filter(c -> c == '\t').count();
        if (t >= pv && t >= v && t > 0) return '\t';
        return pv >= v ? ';' : ',';
    }

    private static List<List<String>> dividir(String texto, char sep) {
        List<List<String>> registros = new ArrayList<>();
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (entreAspas) {
                if (c == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') { atual.append('"'); i++; }
                    else entreAspas = false;
                } else {
                    atual.append(c);
                }
            } else if (c == '"') {
                entreAspas = true;
            } else if (c == sep) {
                campos.add(atual.toString()); atual.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') i++;
                campos.add(atual.toString()); atual.setLength(0);
                if (!registroVazio(campos)) registros.add(campos);
                campos = new ArrayList<>();
            } else {
                atual.append(c);
            }
        }
        if (atual.length() > 0 || !campos.isEmpty()) {
            campos.add(atual.toString());
            if (!registroVazio(campos)) registros.add(campos);
        }
        return registros;
    }

    private static boolean registroVazio(List<String> campos) {
        return campos.stream().allMatch(String::isBlank);
    }
}
