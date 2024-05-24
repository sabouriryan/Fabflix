let adv_search_toggle = $("#adv-search-toggle");
let adv_search_dropdown = $("#adv-search-dropdown");

// Show ellipsis button when search bar is focused
$(".nav-title-search-bar").on("focus", function() {
    adv_search_toggle.removeClass("hidden");
});

// Toggle advanced search dropdown when ellipsis button is clicked
function showAdvanced() {
    adv_search_dropdown.toggleClass("hidden");
    adv_search_dropdown.toggleClass("show");
}

// Prevent closing dropdown when clicking inside the dropdown
$(".adv-search-dropdown").on("click", function(event) {
    event.stopPropagation();
});

$(".nav-title-search-bar").on("focus", function() {
    $("#nav-search-form").addClass("nav-focused");
});

$(".nav-title-search-bar").on("blur", function() {
    $("#nav-search-form").removeClass("nav-focused");
});

$(document).ready(function() {
    let search_form = $(".search-form");

    // Close dropdown when clicking anywhere else on the website
    $(document).on("click", function(event) {
        if (!search_form.is(event.target) && search_form.has(event.target).length === 0) {
            adv_search_dropdown.addClass("hidden");
            adv_search_dropdown.removeClass("show");
            adv_search_toggle.addClass("hidden");
        }
    });

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
});
