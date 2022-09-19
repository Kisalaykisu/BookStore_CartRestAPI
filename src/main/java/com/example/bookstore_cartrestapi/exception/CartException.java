package com.example.bookstore_cartrestapi.exception;

public class CartException extends RuntimeException{
    public CartException(String exception){
        super(exception);
    }
}
