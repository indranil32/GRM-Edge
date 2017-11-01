package com.att.ocnp.mgmt.grm_edge_service.exception;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class RestExceptionController {

  @ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 400
  @ExceptionHandler(BadRequestException.class)
  public String badRequest( BadRequestException ex) {
    return ex.getMessage();
  }
  
  @ResponseStatus(value=HttpStatus.NOT_FOUND)  // 404
  @ExceptionHandler(NotFoundException.class)
  public String noDataFound( NotFoundException ex) {
    return ex.getMessage();
  }
}