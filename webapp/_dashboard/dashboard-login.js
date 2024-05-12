let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log("status: " + resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        window.location.replace("dashboard.html");
    } else {
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();

    let formData = login_form.serialize();

    $.ajax(
        "api/login", {
            method: "POST",
            data: formData,
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
