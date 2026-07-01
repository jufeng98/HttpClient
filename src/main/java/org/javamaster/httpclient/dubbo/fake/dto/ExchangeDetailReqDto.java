package org.javamaster.httpclient.dubbo.fake.dto;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ExchangeDetailReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7637633258968927072L;

    private Long activityExchangeId;

    public Long getActivityExchangeId() {
        return activityExchangeId;
    }

    public void setActivityExchangeId(Long activityExchangeId) {
        this.activityExchangeId = activityExchangeId;
    }
}
