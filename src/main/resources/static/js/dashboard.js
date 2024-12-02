// Fetch and display upcoming assignments
function fetchUpcomingAssignments() {
    fetch(`/api/assignments/upcoming`)
            .then(response => response.json())
            .then(data => {
                const assignmentsContainer = document.querySelector('.assignment-list');
                assignmentsContainer.innerHTML = '';    // Clear existing content 

                // Loop through the assignments and create a list item for each one
                data.forEach(assignment => {
                    const li = document.createElement('li');
                    li.classList.add('assignment-item');
                    const dueDate = new Date(assignment.dueDate.toString());
                    li.innerHTML = `
                                <div class = "details">
                                    <div class="assignment-title">${assignment.title}</div>
                                    <div class="assignment-description">Description: ${assignment.description}</div>
                                    <div class="due-date">Due: ${dueDate.toLocaleDateString()}</div>
                                </div>
                                <button class="btn-primary" onclick="goToSubmission('${assignment.id}','${assignment.title}','${assignment.description}','${assignment.dueDate}')">Submit Assignment</button>
                            `;

                    assignmentsContainer.appendChild(li);
                });
            })
            .catch(error => {
                console.error('Error fetching assignments:', error);
            });
}

// Fetch and display submitted assignments
function fetchSubmittedAssignments() {
    fetch(`/api/assignments/submitted`)
            .then(response => response.json())
            .then(data => {
                const submissionsContainer = document.querySelector('.submission-list');
                submissionsContainer.innerHTML = ''; // Clear existing content

                data.forEach(submission => {
                    const li = document.createElement('li');
                    li.classList.add('submission-item');
                    const dueDate = new Date(submission.dueDate.toString());
                    li.innerHTML = `
                                <div class="details">
                                    <div class="assignment-title">${submission.title}</div>
                                    <div class="assignment-description">Description: ${submission.description}</div>
                                    <div class="due-date">Due: ${dueDate.toLocaleDateString()}</div>
                                </div>
                                <button class="btn-checkGrade" onclick="goToEvaluation('${submission.id}')">Check Evaluation</button>
                            `;
                    submissionsContainer.appendChild(li);
                });
            })
            .catch(error => {
                console.error('Error fetching submitted assignments:', error);
            });
}

fetchUpcomingAssignments();
fetchSubmittedAssignments();

// Redirect to the submit assignment page
function goToSubmission(id, title, description, dueDate) {
    const assignmentData = {
        id: id,
        title: title,
        description: description,
        dueDate: dueDate
    };

    fetch("/api/assignments/pushAssignmentDetails", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(assignmentData)
    })
            .then((response) => {
                if (response.ok) {
                    window.location.href = "/submit-page";
                } else {
                    throw new Error("Failed to save assignment details.");
                }
            })
            .catch((error) => {
                // Handle error
                console.error("Error:", error);
                alert("An error occurred while saving assignment details.");
            });
}

function goToEvaluation(id) {
    fetch(`/api/assignments/pushEvaluationDetails?assignmentId=${id}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
            .then((response) => {
                if (response.ok) {
                    window.location.href = "/evaluation-page";
                } else {
                    throw new Error("Failed to save assignment details.");
                }
            })
            .catch((error) => {
                // Handle error
                console.error("Error:", error);
                alert("An error occurred while saving assignment details.");
            });
}