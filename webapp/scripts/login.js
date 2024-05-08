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
        window.location.replace("../index.html");
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

    // Get the reCAPTCHA response
    let recaptchaResponse = grecaptcha.getResponse();

    // Check if reCAPTCHA response is empty
    if (!recaptchaResponse) {
        console.log("reCAPTCHA not completed");
        $("#login_error_message").text("Please complete the reCAPTCHA");
        return;
    }

    // Include reCAPTCHA response in data sent with AJAX request
    let formData = login_form.serialize() + "&g-recaptcha-response=" + recaptchaResponse;

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
