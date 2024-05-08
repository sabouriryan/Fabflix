function goBack() {
    window.location.href = "api/return";
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param cartItems jsonObject
 */

function populateShoppingCartTable(cartItems) {
    console.log("populating cart table");

    let cartTableBodyElement = $("#cart-items-table-body");
    cartTableBodyElement.empty();

    // Loop through the cart items
    for (let cartItem of cartItems) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + cartItem["movie_title"] + "</td>";
        rowHTML += "<td>$" + cartItem["movie_price"] + "</td>";
        rowHTML += "<td>";
        rowHTML += "<button class='btn-decrease' data-id='" + cartItem["movie_id"] + "'>+</button>";
        rowHTML += "<span class='quantity'>" + cartItem["movie_quantity"] + "</span>";
        rowHTML += "<button class='btn-increase' data-id='" + cartItem["movie_id"] + "'>-</button>";
        rowHTML += "</td>";
        rowHTML += "<td><button class='btn-delete' data-id='" + cartItem["movie_id"] + "'>Delete</button></td>";
        rowHTML += "</tr>";

        cartTableBodyElement.append(rowHTML);
    }
}

// Event listener for decreasing quantity
$(document).on("click", ".btn-decrease", function() {
    let movieId = $(this).data("id");
    fetchCart("?action=add&movie-id=" + movieId)
});

// Event listener for increasing quantity
$(document).on("click", ".btn-increase", function() {
    let movieId = $(this).data("id");
    fetchCart("?action=remove&movie-id=" + movieId)
});

$(document).on("click", ".btn-delete", function() {
    let movieId = $(this).data("id");
    fetchCart("?action=delete&movie-id=" + movieId)
});

function fetchCart(update) {
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/shopping-cart" + update,
        success: (resultData) => populateShoppingCartTable(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    })
}

fetchCart("");