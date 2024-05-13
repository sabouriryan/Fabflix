let add_star_form = $("#add-star-form");
let add_movie_form = $("#add-movie-form");

function addMovie(movieEvent) {
    console.log("Add movie form submitted");
    movieEvent.preventDefault();

    // Serialize form data into a query string
    let formData = $(this).serializeArray().filter(function(item) {
        return item.value.trim() !== ""; // Filter out inputs with empty values
    });

    let queryString = $.param(formData);
    if (queryString !== "") {
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/addMovie?" + queryString,
            success: function(response) {
                if (response["status"] === "success") {
                    alert("Movie added successfully!");
                } else {
                    alert(response["message"]);
                }
            }
        });
    }
}

function addStar(starEvent) {
    console.log("Add star form submitted");
    starEvent.preventDefault();

    // Serialize form data into a query string
    let formData = $(this).serializeArray().filter(function(item) {
        return item.value.trim() !== ""; // Filter out inputs with empty values
    });

    let queryString = $.param(formData);
    if (queryString !== "") {
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/addStar?" + queryString,
            success: function(response) {
                if (response["status"] === "success") {
                    alert("Star added successfully!");
                } else {
                    alert(response["message"]);
                }
            }
        });
    }
}

add_movie_form.submit(addMovie);
add_star_form.submit(addStar);

function getDatabaseMetadata(resultData) {
    let tableCardsContainerElement = jQuery("#db-table-card-container");
    for (let i = 0; i < resultData.length; i++) {
        let tableCardHTML = "";
        tableCardHTML += "<div class='db-table-card floating'>";
        tableCardHTML += "<p>" + resultData[i]["table_name"] + "</p>";
        tableCardHTML += "<table class='db-table-card-content'><tbody>";
        for (let j = 0; j < resultData[i]["columns"].length; j++) {
            tableCardHTML += "<tr>"
            tableCardHTML += "<td>" + resultData[i]["columns"][j]["column_name"] + "</td>";
            tableCardHTML += "<td>" + resultData[i]["columns"][j]["column_type"] + "</td>";
            tableCardHTML += "</tr>"
        }
        tableCardHTML += "</tbody></table></div></div>"
        tableCardsContainerElement.append(tableCardHTML);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/dashboard",
    success: (resultData) => getDatabaseMetadata(resultData)
});