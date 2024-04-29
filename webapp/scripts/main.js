/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function getGenres(resultData) {

    console.log("handleResult: populating genres into dropdown menu");

    let genresMenuElement = jQuery("#genres-container");
    for (let i = 0; i < resultData.length; i++) {
        let genresMenuHTML = "";
        genresMenuHTML += "<a class='genre-item' href='public/movie-list.html?genre=" + resultData[i] + "'>" + resultData[i] + "</a>";

        // Append the row created to the table body, which will refresh the page
        genresMenuElement.append(genresMenuHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

let letters = $("#letters-container");
for (let i = 97; i <= 122; i++) {
    let letter = String.fromCharCode(i);
    letters.append("<div><a href='public/movie-list.html?firstChar=" + letter + "'>" + letter.toUpperCase() + "</a></div>")
}

let numbers = $("#numbers-container");
for (let i = 0; i <= 9; i++) {
    numbers.append("<div><a href='public/movie-list.html?firstChar=" + i + "'>" + i + "</a></div>")
}
numbers.append("<div><a href='public/movie-list.html?firstChar=*'>*</a></div>")

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/main",
    success: (resultData) => getGenres(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});