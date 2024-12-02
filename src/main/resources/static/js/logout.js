// logout logic
function logout() {
    fetch('/logout', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
            .then(response => {
                if (response.ok) {
                    // Redirect to the login page after successful logout
                    window.location.href = '/';
                } else {
                    console.error('Logout failed');
                }
            })
            .catch(error => {
                console.error('Error during logout:', error);
            });
}