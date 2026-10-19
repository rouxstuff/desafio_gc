const campo  = document.getElementById('nomeServico');
const cont   = document.getElementById('cont');
const barra  = document.getElementById('barra');
const botao  = document.getElementById('botao');
const limite = Number(campo.dataset.limite);

function atualizar() {
    const n = campo.value.trim().length;
    const estourou = n > limite;

    cont.textContent = n + ' / ' + limite;
    barra.style.width = Math.min(100, n * 100 / limite) + '%';
    barra.classList.toggle('estourou', estourou);
    botao.disabled = estourou || n === 0;
}

campo.addEventListener('input', atualizar);
atualizar();