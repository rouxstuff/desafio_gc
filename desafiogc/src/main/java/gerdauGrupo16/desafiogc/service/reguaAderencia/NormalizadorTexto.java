package gerdauGrupo16.desafiogc.service.reguaAderencia;

/*
 * ARQUIVO: NormalizadorTexto.java
 * TIPO: componente do Spring (@Component).
 * FUNÇÃO: deixa o texto de um serviço numa forma padrão para comparar (não para exibir). Passos: maiúsculas
 *   sem acento; > vira MAIOR e < vira MENOR; tira (A) de gênero; 50 % vira 50%; pontuação vira espaço;
 *   dicionário de abreviações do arquivo normalizacao/regras.csv; 25 MM vira 25MM; tira o prefixo de
 *   sistema (SV, EV, SERV); espaços simples.
 * CLASSE INTERNA: Regra (record) é uma linha do dicionário: expressão regular e substituição.
 * MÉTODOS:
 * - NormalizadorTexto(): construtor usado pelo Spring; carrega o dicionário do classpath.
 * - NormalizadorTexto(linhasDeRegras): construtor usado nos testes; recebe as regras prontas.
 * - carregar(linhas): lê as regras (expressão;substituição) e a linha @modificadores; ignora vazias e
 *   comentários.
 * - modificadores(): palavras que diferenciam serviços (HE, HN, NOT, DF...).
 * - semAcentos(texto): remove acentos.
 * - normalizar(texto): aplica todos os passos e devolve o texto padrão.
 */

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class NormalizadorTexto {

    private static final String ARQUIVO_REGRAS = "normalizacao/regras.csv";

    private static final Pattern GENERO = Pattern.compile("\\((A|O|AS|OS)\\)");
    private static final Pattern PERCENTUAL = Pattern.compile("(\\d)\\s+%");
    private static final Pattern DECIMAL = Pattern.compile("(?<=\\d)[.,](?=\\d)");
    private static final Pattern MAIS = Pattern.compile("\\+");
    private static final Pattern BARRA_SOLTA = Pattern.compile("(?<!\\d)/|/(?!\\d)");
    private static final Pattern PONTUACAO = Pattern.compile("[^A-Z0-9%/,\\s]");
    private static final Pattern VIRGULA_SOLTA = Pattern.compile("(?<!\\d),|,(?!\\d)");
    private static final Pattern NUMERO_UNIDADE = Pattern.compile(
            "(\\d)\\s+(MM|CM|KM|M2|M3|KG|ML|POL|KV|KVA|KW|HP|CV|HZ)\\b");
    private static final Pattern PREFIXO_SISTEMA = Pattern.compile("^(SV|EV|SERV)\\s+");
    private static final Pattern ESPACOS = Pattern.compile("\\s+");

    private record Regra(Pattern padrao, String substituicao) { }

    private final List<Regra> regras = new ArrayList<>();
    private final Set<String> modificadores = new LinkedHashSet<>();

    public NormalizadorTexto() {
        try (InputStream in = NormalizadorTexto.class.getClassLoader().getResourceAsStream(ARQUIVO_REGRAS)) {
            if (in != null) {
                try (BufferedReader leitor = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    carregar(leitor.lines().toList());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não consegui ler " + ARQUIVO_REGRAS, e);
        }
    }

    public NormalizadorTexto(List<String> linhasDeRegras) {
        carregar(linhasDeRegras);
    }

    private void carregar(List<String> linhas) {
        for (String bruta : linhas) {
            String linha = bruta.replace("﻿", "").strip();
            if (linha.isEmpty() || linha.startsWith("#")) continue;
            int corte = linha.lastIndexOf(';');
            if (corte < 0) continue;
            String esquerda = linha.substring(0, corte);
            String direita = linha.substring(corte + 1).strip();
            if (esquerda.equals("@modificadores")) {
                Arrays.stream(direita.split(",")).map(String::strip).filter(s -> !s.isEmpty()).forEach(modificadores::add);
            } else {
                regras.add(new Regra(Pattern.compile(esquerda), direita));
            }
        }
    }

    public Set<String> modificadores() {
        return modificadores;
    }

    public String semAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    public String normalizar(String texto) {
        if (texto == null) return "";
        String s = semAcentos(texto).toUpperCase();
        s = s.replace(">", " MAIOR ").replace("<", " MENOR ");
        s = GENERO.matcher(s).replaceAll("");
        s = PERCENTUAL.matcher(s).replaceAll("$1%");
        s = DECIMAL.matcher(s).replaceAll(",");
        s = MAIS.matcher(s).replaceAll(" ");
        s = BARRA_SOLTA.matcher(s).replaceAll(" ");
        s = PONTUACAO.matcher(s).replaceAll(" ");
        s = VIRGULA_SOLTA.matcher(s).replaceAll(" ");
        s = ESPACOS.matcher(s).replaceAll(" ").strip();
        for (Regra r : regras) {
            s = r.padrao().matcher(s).replaceAll(r.substituicao());
            s = ESPACOS.matcher(s).replaceAll(" ").strip();
        }
        s = NUMERO_UNIDADE.matcher(s).replaceAll("$1$2");
        s = PREFIXO_SISTEMA.matcher(s).replaceFirst("");
        return ESPACOS.matcher(s).replaceAll(" ").strip();
    }
}
