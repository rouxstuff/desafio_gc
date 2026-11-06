package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: AdminImportacaoController.java
 * TIPO: controller de telas (@Controller), rota /admin/importacao, restrito ao ADMIN.
 * FUNÇÃO: tela do administrador para importar o catálogo saneado e os vínculos fornecedor x serviço por
 *   arquivo CSV, e para recarregar o índice de busca em memória. A regra da importação fica em
 *   ImportacaoService; aqui só se recebe o arquivo e se devolve o relatório para a tela.
 * MÉTODOS:
 * - tela(): abre a página admin/importacao.
 * - servicos(arquivo, simular, model): recebe o CSV de serviços e chama ImportacaoService.importarServicos.
 *   Com simular=true só valida, sem gravar.
 * - vinculos(arquivo, simular, model): idem para o CSV de vínculos (ImportacaoService.importarVinculos).
 * - reindexar(model): descarta o índice em memória (IndiceServicos.invalidar); a próxima consulta recarrega
 *   do banco. Usar depois de alterar o banco por SQL ou o dicionário regras.csv.
 * - processar(arquivo, model, acao): método auxiliar. Valida que há arquivo, executa a ação, coloca o
 *   relatório (ou a mensagem de erro) no model e volta para a mesma página.
 */

import gerdauGrupo16.desafiogc.service.IndiceServicos;
import gerdauGrupo16.desafiogc.service.importacao.ImportacaoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Function;

@Controller
@RequestMapping("/admin/importacao")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportacaoController {

    private final ImportacaoService importacao;
    private final IndiceServicos indice;

    public AdminImportacaoController(ImportacaoService importacao, IndiceServicos indice) {
        this.importacao = importacao;
        this.indice = indice;
    }

    @GetMapping
    public String tela() {
        return "admin/importacao";
    }

    @PostMapping("/servicos")
    public String servicos(@RequestParam("arquivo") MultipartFile arquivo,
                           @RequestParam(value = "simular", defaultValue = "false") boolean simular, Model model) {
        return processar(arquivo, model, bytes -> importacao.importarServicos(bytes, simular));
    }

    @PostMapping("/vinculos")
    public String vinculos(@RequestParam("arquivo") MultipartFile arquivo,
                           @RequestParam(value = "simular", defaultValue = "false") boolean simular, Model model) {
        return processar(arquivo, model, bytes -> importacao.importarVinculos(bytes, simular));
    }

    @PostMapping("/reindexar")
    public String reindexar(Model model) {
        indice.invalidar();
        model.addAttribute("sucesso", "Índice recarregado. A próxima consulta já usa os dados atuais do banco.");
        return "admin/importacao";
    }

    private String processar(MultipartFile arquivo, Model model, Function<byte[], ImportacaoService.Relatorio> acao) {
        if (arquivo == null || arquivo.isEmpty()) {
            model.addAttribute("erro", "Escolha um arquivo CSV antes de enviar.");
            return "admin/importacao";
        }
        try {
            model.addAttribute("relatorio", acao.apply(arquivo.getBytes()));
        } catch (IOException e) {
            model.addAttribute("erro", "Não consegui ler o arquivo: " + e.getMessage());
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Falha ao importar (nada foi gravado): " + e.getMessage());
        }
        return "admin/importacao";
    }
}
