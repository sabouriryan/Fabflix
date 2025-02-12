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
    movieRatingElement.append("<i class='fas fa-star'></i> " + resultData["movie_rating"] + "/10")

    let genreListElement = jQuery("#genre_list");

    var genres = resultData["movie_genres"];
    var genresHTML = "";
    for (var j = 0; j < genres.length; j++) {
        var genre = genres[j];
        genresHTML = "<a class='genre-bubble' href='movie-list.html?genre=" + genre + "'>" + genre + "</a>";
        genreListElement.append(genresHTML);
    }

    let starListElement = jQuery("#star_list");
    for (let i = 0; i < resultData['movie_stars'].length; ++i) {
        starListHTML = "<a href='single-star.html?id=" + resultData["movie_stars"][i]["star_id"] + "'>" + resultData["movie_stars"][i]["star_name"] + "</a>";
        if (i < resultData['movie_stars'].length - 1) {
            starListHTML += "<i class='fas fa-circle star-delimiter'></i>";
        }
        starListElement.append(starListHTML);
    }

    let addToCartHTML = "<button id='add-to-cart-btn' data-movie-id='" + movieId + "' onclick=addToCart(this)><i class=\"fa fa-cart-plus\"></i> Add To Cart</button>"
    $("#add-to-cart-container").append(addToCartHTML);
}

function goBack() {
    window.location.href = "api/return";
}

function addToCart(button) {
    let movieId = $(button).data("movie-id");

    $.ajax({
        url: "api/shopping-cart?action=insert&movie-id=" + movieId,
        type: "GET",
        success: function(response) {
            if (response.status === "success") {
                alert("Added to shopping cart successfully!");
            } else {
                alert("Failed to add to shopping cart. Please try again.");
            }
        }
    });
}

let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});
