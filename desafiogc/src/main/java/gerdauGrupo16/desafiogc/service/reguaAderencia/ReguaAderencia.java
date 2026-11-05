package gerdauGrupo16.desafiogc.service.reguaAderencia;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Régua de aderência (nota de 0.0 a 1.0 entre dois nomes de serviço).
 *
 * Nota base = a maior entre: palavras em comum (Jaccard), "um nome está dentro do outro" (0.8)
 * e semelhança de letras (Dice sobre trigramas, que pega erro de digitação e nome truncado).
 *
 * TRAVAS (como na análise da trilha IA & Dados): se os números/medidas OU os modificadores (HE, HN, NOT...)
 * forem diferentes, a nota fica limitada a 0.89, então NUNCA bloqueia (o bloqueio começa em 0.90),
 * mas ainda pode aparecer como sugestão. Ex.: TUBO 25MM x TUBO 50MM, AJUDANTE HE x AJUDANTE HN.
 */
@Component
public class ReguaAderencia {

    public static final double TETO_COM_TRAVA = 0.89;

    private final NormalizadorTexto normalizador;

    public ReguaAderencia(NormalizadorTexto normalizador) {
        this.normalizador = normalizador;
    }

    /** Calcula uma vez o perfil de um nome (use para o texto digitado e para o índice). */
    public PerfilTexto perfil(String texto) {
        return PerfilTexto.de(normalizador.normalizar(texto), normalizador.modificadores());
    }

    /** Atalho: recebe textos crus (usado em testes e chamadas avulsas). */
    public double calcular(String a, String b) {
        return calcular(perfil(a), perfil(b));
    }

    public double calcular(PerfilTexto a, PerfilTexto b) {
        if (a.tokens().isEmpty() || b.tokens().isEmpty()) return 0.0;
        if (a.tokens().equals(b.tokens())) return 1.0;            // mesmas palavras, em qualquer ordem

        double nota = Math.max(jaccard(a.tokens(), b.tokens()), dice(a.trigramas(), b.trigramas()));
        if (a.tokens().containsAll(b.tokens()) || b.tokens().containsAll(a.tokens())) {
            nota = Math.max(nota, 0.8);                           // um nome está dentro do outro
        }
        if (!a.numeros().equals(b.numeros()) || !a.modificadores().equals(b.modificadores())) {
            nota = Math.min(nota, TETO_COM_TRAVA);
        }
        // Só BLOQUEIA (>= 0.90) quando as palavras que diferem são variações de grafia (erro de digitação
        // ou truncamento em 40 caracteres). Palavra realmente diferente (EXT x INT) ou palavra a mais vira só sugestão.
        if (nota > TETO_COM_TRAVA && !diferencasSaoGrafia(a.tokens(), b.tokens())) {
            nota = TETO_COM_TRAVA;
        }
        return nota;
    }

    private static boolean diferencasSaoGrafia(Set<String> a, Set<String> b) {
        List<String> soA = new ArrayList<>();
        List<String> soB = new ArrayList<>(b);
        for (String t : a) { if (!b.contains(t)) soA.add(t); }
        soB.removeIf(a::contains);
        if (soA.size() != soB.size()) return false;
        boolean[] usado = new boolean[soB.size()];
        for (String x : soA) {
            boolean achou = false;
            for (int j = 0; j < soB.size() && !achou; j++) {
                if (!usado[j] && parecidas(x, soB.get(j))) { usado[j] = true; achou = true; }
            }
            if (!achou) return false;
        }
        return true;
    }

    /** Duas palavras "iguais com erro": uma é começo da outra (truncamento) ou há 1 letra de diferença (2 em palavras longas). */
    private static boolean parecidas(String x, String y) {
        if (x.equals(y)) return true;
        if (x.chars().anyMatch(Character::isDigit) || y.chars().anyMatch(Character::isDigit)) return false;
        int menor = Math.min(x.length(), y.length());
        if (menor >= 3 && (x.startsWith(y) || y.startsWith(x))) return true;
        if (menor < 4) return false;
        return distancia(x, y) <= (Math.max(x.length(), y.length()) >= 8 ? 2 : 1);
    }

    private static int distancia(String x, String y) {
        int[] ant = new int[y.length() + 1];
        int[] atual = new int[y.length() + 1];
        for (int j = 0; j <= y.length(); j++) ant[j] = j;
        for (int i = 1; i <= x.length(); i++) {
            atual[0] = i;
            for (int j = 1; j <= y.length(); j++) {
                int custo = x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1;
                atual[j] = Math.min(Math.min(atual[j - 1] + 1, ant[j] + 1), ant[j - 1] + custo);
            }
            int[] t = ant; ant = atual; atual = t;
        }
        return ant[y.length()];
    }

    private static double jaccard(Set<String> a, Set<String> b) {
        int comuns = 0;
        for (String x : a) if (b.contains(x)) comuns++;
        int uniao = a.size() + b.size() - comuns;
        return uniao == 0 ? 0.0 : (double) comuns / uniao;
    }

    private static double dice(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int comuns = 0;
        for (String x : a) if (b.contains(x)) comuns++;
        return 2.0 * comuns / (a.size() + b.size());
    }
}
