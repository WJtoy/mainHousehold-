package com.wolfking.jeesite.ms.im.entity;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.sys.IMNoticeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.httpclient.NameValuePair;

import java.util.List;

@Data
@NoArgsConstructor
public class IMNoticeModel extends IMNoticeInfo {

    private String userTypeLabels;

}
