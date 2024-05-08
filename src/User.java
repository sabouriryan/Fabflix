import java.util.HashMap;
import java.util.Map;

public class User {
    private final String username;
    private final Map<String, Integer> shoppingCart;

    public User(String username) {
        this.username = username;
        shoppingCart = new HashMap<>();
    }

    public void addItemToCart(String movieId) {
        shoppingCart.compute(movieId, (key, value) -> (value == null) ? 1 : value + 1);
    }

    public void removeItemFromCart(String movieId) {
        shoppingCart.computeIfPresent(movieId, (key, value) -> (value > 1) ? value - 1 : null);
    }

    public void deleteItemFromCart(String movieId) {
        shoppingCart.remove(movieId);
    }

    public Map<String, Integer> getShoppingCart() {
        return this.shoppingCart;
    }
}
