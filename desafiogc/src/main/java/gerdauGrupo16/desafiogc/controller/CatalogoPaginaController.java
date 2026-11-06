package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: CatalogoPaginaController.java
 * TIPO: controller de telas (@Controller).
 * FUNÇÃO: tela /catalogo: lista de todos os serviços com status, para o comprador. O vínculo de comprador
 *   com serviço fica na tela de contratações (ContratacaoController). Não tem regra de negócio: tudo vem dos
 *   services.
 * MÉTODOS:
 * - catalogo(q, pagina, auth, model): catálogo paginado (50 por página) com busca por nome ou código, sem
 *   os serviços rejeitados. O administrador é redirecionado para /admin/servicos.
 */

import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@AllArgsConstructor
public class CatalogoPaginaController {

    private final ServicoService servicoService;

    private static final int TAMANHO_PAGINA = 50;

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                           Authentication auth, Model model) {

        if (PerfilAcesso.ehAdmin(auth)) {
            return "redirect:/admin/servicos" + (q == null || q.isBlank() ? "" : "?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        }
        Page<Servico> resultado = servicoService.listarPagina(q, pagina, TAMANHO_PAGINA, false);

        model.addAttribute("servicos", resultado.getContent().stream()
                .filter(s -> ServicoService.visivelPara(s, false)).toList());
        model.addAttribute("q", q == null || q.isBlank() ? "" : q);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("total", resultado.getTotalElements());
        return "servicos/catalogo";
    }
}
