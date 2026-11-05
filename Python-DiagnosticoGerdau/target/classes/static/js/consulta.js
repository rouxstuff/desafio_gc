// consulta.js: comportamento da tela de detalhes.

// 1) Pega os elementos da pagina html pelo id
const botao  = document.getElementById('ddBotao');
const painel = document.getElementById('ddPainel');
const texto  = document.getElementById('ddTexto');
const chips  = document.getElementById('chips');
const resumo = document.getElementById('resumo');
const nota   = document.getElementById('nota');

const NOTA_SEM_ADICIONAL = 'Serviço encontrado no catálogo. Marque os adicionais, se algum se aplica.';
const NOTA_COM_ADICIONAL = 'Esses adicionais ficam registrados junto ao serviço no contrato.';

// 2) Abre e fecha o dropdown
botao.addEventListener('click', function () {
    const abriu = painel.classList.toggle('aberto');
    botao.setAttribute('aria-expanded', abriu);
});

document.addEventListener('click', function (evento) {
    if (!evento.target.closest('.dd')) {
        painel.classList.remove('aberto');
        botao.setAttribute('aria-expanded', false);
    }
});

// Marca ou desmarca uma caixinha
painel.addEventListener('change', atualizar);

function tituloDe(caixa) {
    return caixa.closest('label').querySelector('strong').textContent;
}

function atualizar() {
    const marcadas = painel.querySelectorAll('input[type=checkbox]:checked');

    // texto do botão
    if (marcadas.length > 0) {
        texto.textContent = marcadas.length + ' adicional(is) selecionado(s)';
    } else {
        texto.textContent = 'Selecione os adicionais';
    }

    // limpa as tags e o resumo para refazer do zero
    chips.innerHTML = '';
    resumo.innerHTML = '';
    if (marcadas.length === 0) {
        chips.innerHTML = '<span class="vazio">Nenhum adicional marcado.</span>';
    }

    // cria uma tag e um item de resumo para cada caixinha marcada
    marcadas.forEach(function (caixa) {
        const titulo = tituloDe(caixa);

        const chip = document.createElement('span');
        chip.className = 'chip';
        chip.textContent = titulo;

        const x = document.createElement('button');
        x.type = 'button';
        x.textContent = '×';
        x.setAttribute('aria-label', 'Remover ' + titulo);
        x.addEventListener('click', function () {
            caixa.checked = false;
            atualizar();
        });

        chip.appendChild(x);
        chips.appendChild(chip);

        const item = document.createElement('li');
        item.textContent = titulo;
        resumo.appendChild(item);
    });

    // painel da direita
    resumo.classList.toggle('oculto', marcadas.length === 0);
    nota.textContent = marcadas.length > 0 ? NOTA_COM_ADICIONAL : NOTA_SEM_ADICIONAL;

    // com adicionais já marcados, o passo atual vira o 3
    const passoAtual = marcadas.length > 0 ? 3 : 2;
    document.querySelectorAll('#passos li').forEach(function (li) {
        const numero = Number(li.dataset.p);
        if (numero < passoAtual) {
            li.className = 'feito';
        } else if (numero === passoAtual) {
            li.className = 'atual';
        } else {
            li.className = '';
        }
    });
}