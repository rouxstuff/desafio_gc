// mostra/esconde a senha
const campoSenha = document.getElementById('senha');
const btnMostrar = document.getElementById('mostrarSenha');
btnMostrar.addEventListener('click', () => {
    const escondida = campoSenha.type === 'password';
    campoSenha.type = escondida ? 'text' : 'password';
    btnMostrar.textContent = escondida ? 'Ocultar' : 'Mostrar';
});