const form = document.querySelector('form')
form.addEventListener('submit', (e) => {
  const password = document.getElementById('password').value
  const confirmPassword = document.getElementById('confirm-password').value

  if (password !== confirmPassword) {
    e.preventDefault()
    const errorMessage = document.getElementById('error-message')
    errorMessage.textContent = 'Passwords do not match.'
    errorMessage.style.display = 'block'
  }
})
