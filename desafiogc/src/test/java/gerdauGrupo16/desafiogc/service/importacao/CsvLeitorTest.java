package gerdauGrupo16.desafiogc.service.importacao;

/*
 * ARQUIVO: CsvLeitorTest.java
 * TIPO: classe de teste (JUnit) de CsvLeitor.
 * MÉTODOS:
 * - normalizaCabecalhosDaPlanilhaOriginal(): cabeçalhos com acento viram minúsculos com sublinhado; o
 *   número da linha conta o cabeçalho.
 * - entendeAspasSeparadorDentroDoCampoEQuebraDeLinha(): campos entre aspas com separador, aspas e quebra de
 *   linha.
 * - lePortuguesDoExcelAntigoEBomDoUtf8(): lê Windows-1252 e UTF-8 com BOM.
 */

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvLeitorTest {

    @Test
    void normalizaCabecalhosDaPlanilhaOriginal() {
        String csv = "Grupo de Serviço;Código do Fornecedor;Código do Serviço;Descrição do Item;Unidade medida\n"
                + "Civil;200982674;4033776;SV-EQUIPE TELHADO;SV\n";
        var r = CsvLeitor.ler(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals("codigo_do_servico", r.cabecalho().get(2));
        assertTrue(r.temColuna("codigo_servico", "codigo_do_servico"));
        assertEquals("4033776", r.linhas().get(0).get("codigo_servico", "codigo_do_servico"));
        assertEquals(2, r.linhas().get(0).numero());
    }

    @Test
    void entendeAspasSeparadorDentroDoCampoEQuebraDeLinha() {
        String csv = "codigo;nome\n4001;\"SV COTOVELOS 1\"\" ; com ponto e vírgula\"\n\n4002;\"linha\ncom quebra\"\n";
        var r = CsvLeitor.ler(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, r.linhas().size());
        assertEquals("SV COTOVELOS 1\" ; com ponto e vírgula", r.linhas().get(0).get("nome"));
        assertEquals("linha\ncom quebra", r.linhas().get(1).get("nome"));
    }

    @Test
    void lePortuguesDoExcelAntigoEBomDoUtf8() {
        byte[] win = "codigo,nome\n1,AÇÃO\n".getBytes(Charset.forName("windows-1252"));
        assertEquals("AÇÃO", CsvLeitor.ler(win).linhas().get(0).get("nome"));
        byte[] bom = "﻿codigo;nome\n1;AÇÃO\n".getBytes(StandardCharsets.UTF_8);
        var r = CsvLeitor.ler(bom);
        assertTrue(r.temColuna("codigo"));
        assertEquals("AÇÃO", r.linhas().get(0).get("nome"));
    }
}
