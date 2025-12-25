package com.example.back.exception;

import com.example.back.dto.response.APIResponse;
import com.example.back.enums.ErrorCodes;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;

@RestControllerAdvice // Chi dinh cho lop xu li ngoai le chung
//@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException appException){
        ErrorCodes errorCodes =appException.errorCodes;
        APIResponse apiResponse =new APIResponse();
        apiResponse.setCode(errorCodes.getCode());
        apiResponse.setMessage(errorCodes.getMessage());
        return ResponseEntity.status(errorCodes.getStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<APIResponse> handleException(Exception exception){
        APIResponse apiResponse =new APIResponse();
        apiResponse.setCode(ErrorCodes.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCodes.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.internalServerError().body(apiResponse);
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<APIResponse> handeAccessDeniedException(AccessDeniedException accessDeniedException){
        APIResponse apiResponse =new APIResponse();
        apiResponse.setCode(ErrorCodes.UNAUTHORIZED.getCode());
        apiResponse.setMessage(ErrorCodes.UNAUTHORIZED.getMessage());
        return ResponseEntity.status(ErrorCodes.UNAUTHORIZED.getStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException){
            String message =methodArgumentNotValidException.getFieldError().getDefaultMessage();
            System.out.println("message "+message);
            ErrorCodes errorCodes = ErrorCodes.valueOf(message);
            APIResponse apiResponse =new APIResponse();
            apiResponse.setMessage(errorCodes.getMessage());
            apiResponse.setCode(errorCodes.getCode());
            return ResponseEntity.status(errorCodes.getStatus()).body(apiResponse);


    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<APIResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(404);
        apiResponse.setMessage("Resource not found: " + e.getResourcePath());
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(apiResponse);
    }

    // Handler cho System Overload Exception
    @ExceptionHandler(value = SystemOverloadException.class)
    public ResponseEntity<APIResponse> handleSystemOverloadException(SystemOverloadException e) {
        ErrorCodes errorCodes = e.getErrorCodes();
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(errorCodes.getCode());
        apiResponse.setMessage(errorCodes.getMessage());
        return ResponseEntity.status(errorCodes.getStatus()).body(apiResponse);
    }

    // Handler cho Database timeout/overload
    @ExceptionHandler(value = QueryTimeoutException.class)
    public ResponseEntity<APIResponse> handleQueryTimeoutException(QueryTimeoutException e) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(ErrorCodes.DATABASE_OVERLOAD.getCode());
        apiResponse.setMessage(ErrorCodes.DATABASE_OVERLOAD.getMessage());
        return ResponseEntity.status(ErrorCodes.DATABASE_OVERLOAD.getStatus()).body(apiResponse);
    }

    // Handler cho Database connection failure
    @ExceptionHandler(value = DataAccessResourceFailureException.class)
    public ResponseEntity<APIResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(ErrorCodes.DATABASE_OVERLOAD.getCode());
        apiResponse.setMessage(ErrorCodes.DATABASE_OVERLOAD.getMessage());
        return ResponseEntity.status(ErrorCodes.DATABASE_OVERLOAD.getStatus()).body(apiResponse);
    }

    // Handler cho Transaction creation failure (database overload)
    @ExceptionHandler(value = CannotCreateTransactionException.class)
    public ResponseEntity<APIResponse> handleCannotCreateTransactionException(CannotCreateTransactionException e) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(ErrorCodes.DATABASE_OVERLOAD.getCode());
        apiResponse.setMessage(ErrorCodes.DATABASE_OVERLOAD.getMessage());
        return ResponseEntity.status(ErrorCodes.DATABASE_OVERLOAD.getStatus()).body(apiResponse);
    }
}
