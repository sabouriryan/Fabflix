let search_form = $("#search-dropdown-form");

function submitSearch(searchEvent) {
    console.log("Search form submitted");
    searchEvent.preventDefault();

    // Serialize form data into a query string
    let formData = $(this).serializeArray().filter(function(item) {
        return item.value.trim() !== ""; // Filter out inputs with empty values
    });

    let queryString = $.param(formData);
    if (queryString !== "")
        queryString = "?" + queryString;

    // Construct the URL with the query string
    // Redirect to the constructed URL
    window.location.href = "/Fabflix/public/movie-list.html" + queryString;
}

search_form.submit(submitSearch);

