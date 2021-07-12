package com.wolfking.jeesite.common.ckeditor;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @autor Ryan Lu
 * @date 2019/5/12 12:53 PM
 */
@Slf4j
@Configuration
@Controller
@RequestMapping("/ckeditor")
public class CKController {

    @Value("${userfiles.uploaddir}")
    private String uploadDir;

    @Value("${userfiles.host}")
    private String imgShowUrl;

    @PostMapping("/uploadImage")
    @ResponseBody
    public UploadImageResModel uploadImage(@RequestParam("upload") MultipartFile multipartFile, HttpServletRequest request) {
        UploadImageResModel res = new UploadImageResModel();
        res.setUploaded(0);

        if (multipartFile == null || multipartFile.isEmpty()) {
            return res;
        }
        //生成新的文件名及存储位置
        String fileName = multipartFile.getOriginalFilename();
        String newFileName = UUID.randomUUID().toString()
                //.replaceAll("-", "")
                .concat(fileName.substring(fileName.lastIndexOf(".")));
        //String savePath = Global.getUploadfilesDir();

        try {
            if(StringUtils.isBlank(uploadDir)){
                String dir = request.getServletContext().getRealPath("");
                System.out.println("realPath:" + dir);
                uploadDir = dir + "/uploads";
                /*
                dir = System.getProperty("user.dir");
                System.out.println("user.dir:" + dir);
                dir = ResourceUtils.getURL("classpath:").getPath();
                System.out.println("classpath:" + dir);
                File path = new File(dir);
                uploadDir = path.getAbsolutePath() + "/uploads";
                */
            }
            String datePath = DateUtils.getDate("/yyyy/MM/dd/");
            String fullPath = uploadDir + datePath + newFileName;
            fullPath=fullPath.replaceAll("%20"," ");
            File target = new File(fullPath);
            if (!target.getParentFile().exists()) { //判断文件父目录是否存在
                target.getParentFile().mkdirs();
            }

            multipartFile.transferTo(target);

            String imgUrl = imgShowUrl + datePath + newFileName;

            res.setUploaded(1);
            res.setFileName(fileName);
            res.setUrl(imgUrl);
            return res;
        } catch (IOException ex) {
            log.error("上传图片异常", ex);
        }

        return res;
    }

    @PostMapping("/uploadMDImage")
    @ResponseBody
    public UploadImageResModel uploadMDImage(@RequestParam("upload") MultipartFile multipartFile, HttpServletRequest request) {
        UploadImageResModel res = new UploadImageResModel();
        res.setUploaded(0);

        if (multipartFile == null || multipartFile.isEmpty()) {
            return res;
        }
        String categoryName = request.getParameter("category");
        if (StringUtils.isEmpty(categoryName)) {
            return res;
        }

        //生成新的文件名及存储位置
        String fileName = multipartFile.getOriginalFilename();
        String newFileName = UUID.randomUUID().toString()
                .concat(fileName.substring(fileName.lastIndexOf(".")));

        try {
            if(StringUtils.isBlank(uploadDir)){
                String dir = request.getServletContext().getRealPath("");
                System.out.println("realPath:" + dir);
                uploadDir = dir + "/uploads";
            }
            String datePath = "/" + categoryName + DateUtils.getDate("/yyyy/MM/dd/");
            String fullPath = uploadDir + datePath + newFileName;
            fullPath=fullPath.replaceAll("%20"," ");
            File target = new File(fullPath);
            if (!target.getParentFile().exists()) { //判断文件父目录是否存在
                target.getParentFile().mkdirs();
            }

            multipartFile.transferTo(target);

            String imgUrl = imgShowUrl + datePath + newFileName;

            res.setUploaded(1);
            res.setFileName(fileName);
            res.setUrl(imgUrl);
            return res;
        } catch (IOException ex) {
            log.error("上传图片异常", ex);
        }

        return res;
    }
}
