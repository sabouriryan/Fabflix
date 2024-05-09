let payment_form = $("#payment-form");

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log("status: " + resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        console.log("SUCCESS!!!")
        window.location.href = "confirmation.html";
    } else {
        $("#payment-error-message").text(resultDataJson["message"]);
    }
}

function submitPaymentForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    let formData = payment_form.serialize();
    $.ajax(
        "api/payment", {
            method: "POST",
            data: formData,
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);

$("#total-price").append("$" + getParameterByName("total"));
