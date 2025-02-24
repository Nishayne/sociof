package com.hashedin.huSpark.exception;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public final ResponseEntity<?> handleAllExceptions(ResourceNotFoundException ex) {
//        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage());
//
//        return ResponseEntity.notFound().;
//    }
//    @ExceptionHandler(ResourceExistsException.class)
//    public final ResponseEntity<Object> handleResourceExistsException(ResourceExistsException ex){
//        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage());
//
//        return new ResponseEntity(exceptionResponse, HttpStatus.ALREADY_REPORTED);
//    }
//
//    public class ExceptionResponse {
//
//        private Date timestamp;
//        private String message;
//
//
//        public ExceptionResponse(Date timestamp, String message) {
//            super();
//            this.timestamp = timestamp;
//            this.message = message;
//        }
//
//        public Date getTimestamp() {
//            return timestamp;
//        }
//
//        public void setTimestamp(Date timestamp) {
//            this.timestamp = timestamp;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }
//
//    }
}
