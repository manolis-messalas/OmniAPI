package com.messalas.spring_boot_demo_A.exceptions;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.SERVER)
public class BookServiceException extends RuntimeException {
    public BookServiceException(String message) {
        super(message);
    }
}