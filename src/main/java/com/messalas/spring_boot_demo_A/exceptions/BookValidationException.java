package com.messalas.spring_boot_demo_A.exceptions;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.CLIENT)
public class BookValidationException extends RuntimeException {
    public BookValidationException(String message) {
        super(message);
    }
}
