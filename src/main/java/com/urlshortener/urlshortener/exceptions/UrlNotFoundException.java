package com.urlshortener.urlshortener.exceptions;

public class UrlNotFoundException extends Exception{
    public UrlNotFoundException(String msg) {
        super(msg);
    }
}
