package org.javamaster.httpclient.dubbo.fake.dto;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ExchangeDetailResDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1580853567388652L;

    private Long activityExchangeId;
    private Integer serviceStatus;

    public Long getActivityExchangeId() {
        return activityExchangeId;
    }

    public void setActivityExchangeId(Long activityExchangeId) {
        this.activityExchangeId = activityExchangeId;
    }

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
}
