package com.example.bookstore_cartrestapi.service;

import com.example.bookstore_cartrestapi.dto.BookDataDTO;
import com.example.bookstore_cartrestapi.dto.CartDTO;
import com.example.bookstore_cartrestapi.dto.UserDataDTO;
import com.example.bookstore_cartrestapi.exception.CartException;
import com.example.bookstore_cartrestapi.model.Cart;
import com.example.bookstore_cartrestapi.repository.CartRepo;
import com.example.bookstore_cartrestapi.utility.TokenUtility;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class CartService implements ICartService{
    private String USER_URI = "http://localhost:9096/user/Id/";
    private String USER_Token_URI = "http://localhost:9096/user/GetUser/";
    private String BOOK_URI = "http://localhost:9093/book/Id/";
    private String CART_URI = "http://localhost:9097/cart/Data/";

    @Autowired
    CartRepo cartRepo;
    @Autowired
    TokenUtility tokenUtility;
    @Autowired
    private RestTemplate restTemplate;
    //Add Cart details
    @Override
    public Cart addCartData(CartDTO cartDTO) {
        ResponseEntity<UserDataDTO> userDetails = restTemplate.getForEntity(USER_URI+cartDTO.getUserId(), UserDataDTO.class);
        /*
        //Converting to the JSON object
        JSONObject user = new JSONObject(userDetails.getBody());
        //printing User details in JSON Object form
        System.out.println(user);
        System.out.println(user.get("emailAddress"));
        */
        System.out.println(userDetails.getBody());
        ResponseEntity<BookDataDTO> bookDetails = restTemplate.getForEntity(BOOK_URI+cartDTO.getBookId(), BookDataDTO.class);
        System.out.println(bookDetails.getBody());
        if (userDetails.hasBody() && bookDetails.hasBody()) {
            if(cartDTO.getQuantity()<=bookDetails.getBody().getQuantity()){
                Cart cartDetails = new Cart(cartDTO);
                return cartRepo.save(cartDetails);
            }else
                throw new CartException("Quantity Exceeds, Available Book Quantity: "+bookDetails.getBody().getQuantity());
        } else {
            throw new CartException("Invalid User Id | Book Id");
        }
    }
    //Get all Cart lists
    @Override
    public List<Cart> allCartList() {
        List<Cart> cartList = cartRepo.findAll();
        if(cartList.isEmpty()){
            throw new CartException("No Items added in cart yet!!");
        }else
            return cartList;
    }
    //Get cart details by cart Id
    @Override
    public Cart getCartDetailsByCartId(Long cartId) {
        Optional<Cart> cartDetails = cartRepo.findById(cartId);
        if(cartDetails.isPresent()){
            return cartDetails.get();
        }else
            throw new CartException("Cart ID does not exist: Invalid ID");
    }
    // Get cart details by cart Id (MicroService)
    @Override
    public Cart getCartDataByCartId(Long cartId) {
        Optional<Cart> cartDetails = cartRepo.findById(cartId);
        if(cartDetails.isPresent()){
            return cartDetails.get();
        }else
            return null;
    }
    //Get cart details by User Id
    @Override
    public List<Cart> getCartDetailsByUserId(Long userId) {
        ResponseEntity<UserDataDTO> userDetails = restTemplate.getForEntity(USER_URI+userId, UserDataDTO.class);
        List<Cart> userCartList = cartRepo.getCartListWithUserId(userId);
        if(userDetails.hasBody()){
            if(userCartList.isEmpty())
                throw new CartException("Cart is Empty!");
            else
                return userCartList;
        }else
            throw new CartException("User ID not found");
    }
    //Get cart details by Token
    @Override
    public List<Cart> getCartDetailsByToken(String token) {
        ResponseEntity<UserDataDTO> userDetails = restTemplate.getForEntity(USER_Token_URI+token, UserDataDTO.class);
        if(userDetails.hasBody()){
            Long userId = tokenUtility.decodeToken(token);
            List<Cart> userCartList = cartRepo.getCartListWithUserId(userId);
            if(userCartList.isEmpty()){
                throw new CartException("Cart is Empty!");
            }else
                return userCartList;
        }else
            throw new CartException("User ID Does not found!!!!");
    }
    //Edit cart details
    @Override
    public String editCartByCartId(Long cartId, CartDTO cartDTO) {
        ResponseEntity<UserDataDTO> userDetails = restTemplate.getForEntity(USER_URI+cartDTO.getUserId(), UserDataDTO.class);
        ResponseEntity<BookDataDTO> bookDetails = restTemplate.getForEntity(BOOK_URI+cartDTO.getBookId(), BookDataDTO.class);
        ResponseEntity<Cart> cartDetails = restTemplate.getForEntity(CART_URI+cartId, Cart.class);
        if(userDetails.hasBody() && cartDetails.hasBody() && bookDetails.hasBody()){
                if(userDetails.getBody().getUserId().equals(cartDetails.getBody().getUserId())){
                    if(cartDTO.getQuantity()<=bookDetails.getBody().getQuantity()){
                        cartDetails.getBody().setBookId(cartDTO.getBookId());
                        cartDetails.getBody().setQuantity(cartDTO.getQuantity());
                        cartRepo.save(cartDetails.getBody());
                        return "Cart Details Updated! with Book ID: "+cartDTO.getBookId()+", Quantity: "+cartDTO.getQuantity();
                    }else
                        throw new CartException("Quantity Exceeds, Available Book Quantity: "+bookDetails.getBody().getQuantity());
                }else
                    throw new CartException("Cart ID does not match for the User ID: "+cartDTO.getUserId());
        }else
            throw new CartException("Invalid User ID | book ID | Cart ID");
    }
    //delete cart details
    @Override
    public String deleteCartByCartId(Long userId, Long cartId) {
        ResponseEntity<UserDataDTO> userDetails = restTemplate.getForEntity(USER_URI+userId, UserDataDTO.class);
        ResponseEntity<Cart> cartDetails = restTemplate.getForEntity(CART_URI+cartId, Cart.class);
        if(userDetails.hasBody() && cartDetails.hasBody()){
            if(userDetails.getBody().getUserId().equals(cartDetails.getBody().getUserId())){
                cartRepo.deleteByCartId(cartId);
                return "Deleted Cart ID: "+cartId;
            }else
                throw new CartException("Cart ID does not match for the User ID: "+userId);
        }else {
            throw new CartException("User ID | Cart ID, does not found");
        }
    }
}
