// login.js: mostra/esconde a senha na tela de login.
// Os ids vêm do login.html: campo da senha = "password" e botão = "mostrarSenha".
(function () {
    const campoSenha = document.getElementById('password');
    const btnMostrar = document.getElementById('mostrarSenha');
    if (!campoSenha || !btnMostrar) return;   // página sem esses elementos: não faz nada

    btnMostrar.addEventListener('click', function () {
        const escondida = campoSenha.type === 'password';
        campoSenha.type = escondida ? 'text' : 'password';
        btnMostrar.textContent = escondida ? 'Ocultar' : 'Mostrar';
        btnMostrar.setAttribute('aria-label', escondida ? 'Ocultar senha' : 'Mostrar senha');
        campoSenha.focus();
    });
})();
