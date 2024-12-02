const form = document.querySelector('form');
form.addEventListener('submit', (e) => {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        e.preventDefault();
        const errorMessage = document.getElementById('errorMessage');
        errorMessage.textContent = "Passwords do not match.";
        errorMessage.style.display = 'block';
    }
});