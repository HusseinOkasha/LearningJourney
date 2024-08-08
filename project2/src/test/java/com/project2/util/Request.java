package com.project2.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class Request<T, E> {
    private String endPointUrl;
    private int portNumber;
    private RestTemplate restTemplate;
    private ParameterizedTypeReference<E> responseDataType;
    private HttpEntity<T> httpEntity;
    private HttpMethod httpMethod;
    private Request(){}

    public static class Builder<T, E> {
        private String endPointUrl;
        private int portNumber;
        private RestTemplate restTemplate;
        private ParameterizedTypeReference <E> responseDataType;
        private HttpEntity<T> httpEntity;
        private HttpMethod httpMethod;


        public static <T, E> Builder<T, E> builder() {
            return new Builder<>();
        }
        public Builder<T, E> endPointUrl(String url){
            this.endPointUrl = url;
            return this;
        }

        public Builder<T,E> portNumber(int portNumber){
            this.portNumber = portNumber;
            return this;
        }
        public Builder<T,E> restTemplate(RestTemplate restTemplate){
            this.restTemplate = restTemplate;
            return this;
        }
        public Builder<T,E> responseDataType(ParameterizedTypeReference<E> responseDataType){
            this.responseDataType = responseDataType;
            return this;
        }
        public Builder<T,E> httpEntity(T body){

            this.httpEntity  = new HttpEntity<>(body);
            return this;
        }
        public Builder<T,E> httpMethod(HttpMethod httpMethod){
            this.httpMethod = httpMethod;
            return this;
        }

        public Request<T, E> build(){
            Request<T, E> request = new Request<>();
            request.endPointUrl = this.endPointUrl;
            request.portNumber = this.portNumber;
            request.restTemplate = this.restTemplate;
            request.httpEntity = this.httpEntity;
            request.responseDataType = this.responseDataType;
            request.httpMethod = this.httpMethod;
            return request;
        }
    }
    public ResponseEntity<E> sendRequest(){
        String fullUrl = GeneralUtil.getBaseUrl(portNumber) + endPointUrl;
        try{
            return restTemplate.exchange(fullUrl, httpMethod, httpEntity, responseDataType);
        }
        catch (HttpServerErrorException e){
            return new ResponseEntity<>(e.getStatusCode());
        }

    }
}