package org.javamaster.httpclient.dubbo.fake;

import org.javamaster.httpclient.dubbo.fake.dto.*;

import java.util.ArrayList;

public interface ServiceExchangeDubboService {

    ResultDto<ArrayList<ExchangeDetailResDto>> getExchangeDetails();

    ResultDto<ExchangeDetailResDto> getExchangeDetail1(ExchangeDetailReqDto reqDto);

    ExchangeDetailResDto getExchangeDetail2(ExchangeDetailReqDto reqDto);

    Boolean checkMainImgAndType(String clothesUniqueId, String typeCode);

    boolean checkHandOver(Long clothesId, boolean hasHang);

    void updateWhenInitialFinalCheckBack();

}
