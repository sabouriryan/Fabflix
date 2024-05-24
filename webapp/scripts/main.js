/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function getGenres(resultData) {
    let genresElement = jQuery("#genres-container");
    for (let i = 0; i < resultData.length; i++) {
        let genresHTML = "";
        genresHTML += "<a class='genre-item' href='public/movie-list.html?genre=" + resultData[i] + "'>" + resultData[i] + "</a>";

        // Append the row created to the table body, which will refresh the page
        genresElement.append(genresHTML);
    }
}

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

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/main",
    success: (resultData) => getGenres(resultData)
});