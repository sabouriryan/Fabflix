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


let cachedRecords = []; // array
const recordsPerPage = 20;

/**
 * Handles the data returned by the API, read the jsonObject and copy records into cache
 * @param resultData jsonObject
 */
function movieRecordHandler(resultData) {
    console.log("movieRecordHandler: Uploading top 100 records to cache");
    cachedRecords = []; // clear the cache

    cachedRecords = resultData;
    //cachedRecords = resultData.slice(0, 100); // If more than 100 records, cache only 100
    console.log("Logged " + cachedRecords.length + " records")
    console.log(cachedRecords);
    populateMovieTable(1);
}

/**
 * Populates data into html elements from cache
 * @param pageNumber the current page number
 */
function populateMovieTable(pageNumber) {
    console.log("populateMovieTable: populating movie table");
    const startIndex = (pageNumber - 1) * recordsPerPage;
    const endIndex = Math.min(startIndex + recordsPerPage, cachedRecords.length);
    const recordsToDisplay = cachedRecords.slice(startIndex, endIndex);

    // Populate the movie table by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < recordsToDisplay.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + "<a href='single-movie.html?id=" + recordsToDisplay[i]["movie_id"] + "'>" + recordsToDisplay[i]["movie_title"] + "</a>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_genres"].join(", ") + "</td>";

        rowHTML += "<td>";
        let stars = recordsToDisplay[i]["movie_stars"];
        for (let i = 0; i < stars.length; ++i) {
            rowHTML += "<a href='single-star.html?id=" + stars[i]["star_id"] + "'>" + stars[i]["star_name"] + "</a>";
            if (i !== stars.length - 1) rowHTML += ", ";
        }
        rowHTML += "</td>";

        rowHTML += "<td>" + recordsToDisplay[i]["movie_rating"] + "</td>";
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML); // refreshes page
    }
}
    // implement pagination here later

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let params = [];

let genre = getParameterByName('genre');
let title = getParameterByName('title');
let firstChar = getParameterByName('firstChar');
let page = getParameterByName('page');
let pageLimit = getParameterByName('pageLimit');
let year = getParameterByName('year');
let director = getParameterByName('director');

if (genre !== null) {
    params.push('genre=' + genre);
}
if (title !== null) {
    params.push('title=' + title);
}
if (firstChar !== null) {
    params.push('firstChar=' + firstChar);
}
if (page !== null) {
    params.push('page=' + page);
}
if (pageLimit !== null) {
    params.push('pageLimit=' + pageLimit);
}
if (year !== null) {
    params.push('year=' + year);
}
if (director !== null) {
    params.push('director=' + director);
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list?" + params.join('&'), // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => movieRecordHandler(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});