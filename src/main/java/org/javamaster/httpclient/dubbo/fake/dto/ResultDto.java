package org.javamaster.httpclient.dubbo.fake.dto;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ResultDto<T extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2178135506278023231L;
    private Boolean isSuccess;
    private Integer responseCode;
    private String responseMsg;
    private T data;

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
