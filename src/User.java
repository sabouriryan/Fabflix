import java.util.HashMap;
import java.util.Map;

public class User {
    private final String username;
    private Map<String, Integer> shoppingCartItems;

    public User(String username) {
        this.username = username;
        shoppingCartItems = new HashMap<>();
    }

    public Map<String, Integer> getShoppingCartItems() {
        return shoppingCartItems;
    }

    public void setShoppingCartItems(Map<String, Integer> shoppingCartItems) {
        this.shoppingCartItems = shoppingCartItems;
    }
}
