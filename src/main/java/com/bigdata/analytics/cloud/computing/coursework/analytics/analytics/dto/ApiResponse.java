package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto;

import java.util.List;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;
    private Integer processed;
    private List<ValidationError> errors;
    
    public ApiResponse() {}
    
    public ApiResponse(boolean success) {
        this.success = success;
    }
    
    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }
    
    public ApiResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }
    
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true);
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, error);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Integer getProcessed() { return processed; }
    public void setProcessed(Integer processed) { this.processed = processed; }
    
    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }
    
    public static class ValidationError {
        private int index;
        private String error;
        
        public ValidationError() {}
        
        public ValidationError(int index, String error) {
            this.index = index;
            this.error = error;
        }
        
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
