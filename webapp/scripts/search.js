let cachedSuggestions = [];

let adv_search_toggle = $("#adv-search-toggle");
let adv_search_dropdown = $("#adv-search-dropdown");

$(".nav-title-search-bar").on("focus", function() {
    adv_search_toggle.removeClass("hidden");
});

$(".adv-search-dropdown").on("click", function(event) {
    event.stopPropagation();
});

$(".nav-title-search-bar").on("focus", function() {
    $("#nav-search-form").addClass("nav-focused");
});

$(".nav-title-search-bar").on("blur", function() {
    $("#nav-search-form").removeClass("nav-focused");
});

function showAdvanced() {
    adv_search_dropdown.toggleClass("hidden");
    adv_search_dropdown.toggleClass("show");
}

function getSuggestions(query, done) {
    console.log("Autocomplete search initiated")

    // TODO: if you want to check past query results first, you can do it here


    $.ajax({
        "method": "GET",
        "url": "/Fabflix/public/api/search-suggestion?query=" + encodeURIComponent(query),
        "success": function(data) {
            ajaxSuccessCallback(data, query, done)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })

}

function ajaxSuccessCallback(data, query, done) {
    console.log("raw data" + data)

    // TODO: if you want to cache the result into a global variable you can do it here

    done( { suggestions: data } );
}

function selectSuggestion(suggestion) {
    console.log("Suggestion selected" + suggestion);
    window.location.href = "/Fabflix/public/single-movie.html?id=" + suggestion["data"]["movie_id"];
}

$(document).ready(function() {
    let search_form = $(".search-form");
    function submitSearch(searchEvent) {
        console.log("Search form submitted");
        searchEvent.preventDefault();

        let formData = $(this).serializeArray().filter(function(item) {
            return item.value.trim() !== ""; // Filter out inputs with empty values
        });

        let queryString = $.param(formData);
        if (queryString !== "") {
            queryString = "?" + queryString;
            window.location.href = "/Fabflix/public/movie-list.html" + queryString;
        }
    }
    search_form.submit(submitSearch);

    $(document).on("click", function(event) {
        if (!search_form.is(event.target) && search_form.has(event.target).length === 0) {
            adv_search_dropdown.addClass("hidden");
            adv_search_dropdown.removeClass("show");
            adv_search_toggle.addClass("hidden");
        }
    });

    $(".full-text-search-bar").autocomplete({
        lookup: function (query, done) {
            getSuggestions(query, done);
        },
        onSelect: function (suggestion) {
            selectSuggestion(suggestion);
        },
        deferRequestBy: 300, // delay for 300ms between query lookups
        minChars: 3 // Minimum of 3 characters needed before a lookup is performed
    });
});
