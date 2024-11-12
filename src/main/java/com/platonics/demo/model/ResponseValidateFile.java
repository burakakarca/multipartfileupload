package com.platonics.demo.model;

import java.util.List;

public class ResponseValidateFile {
    private List<ErrorDTO> errorList;

    public List<ErrorDTO> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorDTO> errorList) {
        this.errorList = errorList;
    }
}
