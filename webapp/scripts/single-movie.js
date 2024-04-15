/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log(resultData);
    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieTitleElement = jQuery("#movie_title");
    movieTitleElement.append(resultData["movie_title"])

    let movieYearElement = jQuery("#movie_year");
    movieYearElement.append(resultData["movie_year"])


    let movieDirectorElement = jQuery("#movie_director");
    movieDirectorElement.append(resultData["movie_director"])
    
    let movieRatingElement = jQuery("#movie_rating");
    movieRatingElement.append(resultData["movie_rating"])
    
    let genreListElement = jQuery("#genre_list");

    for (let i = 0; i < resultData['movie_genres'].length; ++i){
        genreListHTML = resultData['movie_genres'][i];
        if (i !== resultData['movie_genres'].length - 1) genreListHTML += ", "
        genreListElement.append(genreListHTML);
    }

    let starListElement = jQuery("#star_list");
    for (let i = 0; i < resultData['movie_stars'].length; ++i){
        starListHTML = "<a href='single-star.html?id=" + resultData["movie_stars"][i]["star_id"] + "'>" + resultData["movie_stars"][i]["star_name"] + "</a>";
        if (i < resultData['movie_stars'].length - 1) {
            starListHTML+= ", ";
        }
        starListElement.append(starListHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});