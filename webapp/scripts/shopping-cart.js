let total_price = 0;
let total_items = 0;

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
    total_price = 0;
    total_items = 0;

    // Loop through the cart items
    for (let cartItem of cartItems) {
        let rowHTML = "<tr>";
        rowHTML += "<td><div class='movie-title-container'>" + cartItem["movie_title"] +
            "<button class='cart-btn btn-delete' data-id='" + cartItem["movie_id"] + "'><i class='fas fa-trash-alt'></i></button>" +
        "</div></td>";
        rowHTML += "<td>";
        rowHTML += "<button class='cart-btn btn-decrease' data-id='" + cartItem["movie_id"] + "'><i class='fas fa-plus-square'></i></button>";
        rowHTML += "   <span class='quantity'>" + cartItem["movie_quantity"] + "</span>   ";
        rowHTML += "<button class='cart-btn btn-increase' data-id='" + cartItem["movie_id"] + "'><i class='fas fa-minus-square'></i></button>";
        rowHTML += "</td>";
        total_items += cartItem["movie_quantity"];
        rowHTML += "<td>$" + cartItem["movie_price"] + "</td>";
        total_price += cartItem["movie_price"] * cartItem["movie_quantity"]
        rowHTML += "<td>$" + cartItem["movie_price"] * cartItem["movie_quantity"] + "</td>";
        rowHTML += "</tr>";

        cartTableBodyElement.append(rowHTML);
    }

    let itemCountElement = $("#item-count");
    itemCountElement.empty();
    itemCountElement.append(total_items + " Items")

    let totalPriceElement = $("#total-price");
    totalPriceElement.empty();
    totalPriceElement.append("$" + total_price);


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