package com.wolfking.jeesite.common.ckeditor;

import lombok.Data;

/**
 * @autor Ryan Lu
 * @date 2019/5/12 12:50 PM
 */
@Data
public class UploadImageResModel {
    /**
     * 1成功，0失败
     */
    private Integer uploaded;

    private String fileName;

    private String url;

}
