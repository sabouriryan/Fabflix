let page = 1;
let pageLimit = 25;
let sort = 1;

$(document).ready(function() {
    let urlPage = getParameterByName('page');
    let urlPageLimit = getParameterByName('pageLimit');
    let urlSort = getParameterByName('sort');

    if (urlPage) { page = parseInt(urlPage); }
    if (urlPageLimit) { pageLimit = parseInt(urlPageLimit); }
    if (urlSort) { sort = parseInt(urlSort); }

    fetchMovies();
});

$('#pageLimitDropdown').change(function() {
    pageSize = $(this).val();
    page = 1; // Reset page number when page size changes
    fetchMovies(); // Fetch data with updated page size
});

$('#sort-dropdown').change(function() {
    sort = $(this).val();
    page = 1; // Reset page number when sort changes
    fetchMovies(); // Fetch data with updated page size
});

// Event listener for previous page button
$('#prev-page').click(function() {
    if (page > 1) {
        page--;
        fetchMovies(); // Fetch previous page
    }
});

// Event listener for next page button
$('#next-page').click(function() {
    page++;
    fetchMovies(); // Fetch next page
});

function updatePaginationButtons(numRecords) {
    $('#prev-page').prop('disabled', page === 1); // Disable previous button if on the first page
    $('#current-page').text("Page " + page);

    if (numRecords < pageLimit) {
        $('#next-page').prop('disabled', true); // Disable next button if no more pages
    } else {
        $('#next-page').prop('disabled', false); // Enable next button if more pages
    }
}

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
 * Populates data into html elements from cache
 * @param movieResultData the movie data fetched
 */
function populateMovieTable(movieResultData) {
    console.log("populateMovieTable: populating movie table");

    // Populate the movie table by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    for (let i = 0; i < movieResultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + "<a href='single-movie.html?id=" + movieResultData[i]["movie_id"] + "'>" + movieResultData[i]["movie_title"] + "</a>";
        rowHTML += "<td>" + movieResultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + movieResultData[i]["movie_director"] + "</td>";
        //rowHTML += "<td>" + recordsToDisplay[i]["movie_genres"].join(", ") + "</td>";
        var genres = movieResultData[i]["movie_genres"];
        var genresHTML = "";
        for (var j = 0; j < genres.length; j++) {
            var genre = genres[j];
            if (j > 0) {
                genresHTML += ", ";
            }
            genresHTML += "<a href='movie-list.html?genre=" + genre +  "'>" +  genre + "</a>";
        }
        rowHTML += "<td>" + genresHTML + "</td>";


        rowHTML += "<td>";
        let stars = movieResultData[i]["movie_stars"];
        for (let i = 0; i < stars.length; ++i) {
            rowHTML += "<a href='single-star.html?id=" + stars[i]["star_id"] + "'>" + stars[i]["star_name"] + "</a>";
            if (i !== stars.length - 1) rowHTML += ", ";
        }
        rowHTML += "</td>";
        rowHTML += "<td><span class='rating-td'><i class='fas fa-star'></i> " + movieResultData[i]["movie_rating"] + "</span></td>";
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML); // refreshes page
    }
}

/**
 * Fetches movie data through an HTTP request
 */
function fetchMovies() {
    let params = [];

    let genre = getParameterByName('genre');
    let firstChar = getParameterByName('firstChar');
    let title = getParameterByName('title');
    let year = getParameterByName('year');
    let director = getParameterByName('director');
    let starName = getParameterByName('starName')

    if (genre !== null) {
        params.push('genre=' + genre);
    }
    if (firstChar !== null) {
        params.push('firstChar=' + firstChar);
    }
    if (title !== null) {
        params.push('title=' + title);
    }
    if (year !== null) {
        params.push('year=' + year);
    }
    if (director !== null) {
        params.push('director=' + director);
    }
    if (starName !== null) {
        params.push('starName=' + starName);
    }

    let pageSetupURL = "page=" + page + "&pageLimit=" + pageLimit + "&sort=" + sort + "&";

    // Makes the HTTP GET request and registers on success callback function
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movie-list?" + pageSetupURL + params.join('&'), // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
        success: (resultData) => { // Setting callback function to handle data returned
            populateMovieTable(resultData);
            updatePaginationButtons(resultData.length);
        }
    });
}