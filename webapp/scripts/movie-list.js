/**
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

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
        rowHTML += "<td>" + recordsToDisplay[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + recordsToDisplay[i]["movie_genres"].join(", ") + "</td>";

        let stars = recordsToDisplay[i]["movie_stars"];
        for (let i = 0; i < stars.length; ++i) {
            rowHTML += "<td>" +
                "<a href='public/single-star.html'>" +
                "</a>" +
            "</td>";
        }


        rowHTML += "<td>" + recordsToDisplay[i]["movie_rating"] + "</td>";
        rowHTML += "</tr>";
        //'<a href="public/single-movie.html?id=' + recordsToDisplay[i]['id'] + '">'
        //+ recordsToDisplay[i]["star_name"] +     // display star_name for the link text
        //'</a>' +

        movieTableBodyElement.append(rowHTML); // refreshes page
    }
}
    // implement pagination here later

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
    success: (resultData) => movieRecordHandler(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});