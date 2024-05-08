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

function goBack() {
    window.location.href = "api/return";
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    let starNameElement = jQuery("#star-name");
    starNameElement.append(resultData["star_name"]);

    let starDobElement = jQuery("#star-dob");
    starDobElement.append("("+ resultData["star_dob"] + ")");

    console.log("handleResult: populating movie table from resultData");

    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < resultData["star_movies"].length; i++) {
        let movieTableHTML = "";
        movieTableHTML += "<tr>";
        movieTableHTML += "<td><a href='single-movie.html?id=" + resultData["star_movies"][i]["movie_id"] + "'>" +
            resultData["star_movies"][i]["movie_title"] + "</a></td>";

        movieTableHTML += "<td>" + resultData["star_movies"][i]["movie_year"] + "</td>";
        movieTableHTML += "<td>" + resultData["star_movies"][i]["movie_director"] + "</td>";
        movieTableHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(movieTableHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});