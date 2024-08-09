package com.xxx.takeout.controller;

import com.xxx.takeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

// 文件上传下载
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${takeout.path}")
    private String basePath;

    @PostMapping("/upload")
    // MultipartFiles 与前端命名一致
    public R<String> upload(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();  // 原始文件名
        String suffix = originalFilename .substring(originalFilename.lastIndexOf("."));
        String randomName = UUID.randomUUID().toString();  // UUID 重新生成文件名 防止名称重复

        File dir = new File(basePath);
        if(!dir.exists()){
            // 目录不存在
            dir.mkdirs();
        }

        try{
            file.transferTo(new File(basePath + randomName + suffix)) ;
            log.info("文件上传成功");
        }catch (IOException e){
            e.printStackTrace();
        }

        // 此时file为临时文件
        return R.success(randomName + suffix);
    }

    // 文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));  // 输入流读取文件
            ServletOutputStream outputStream = response.getOutputStream();  // 输出流展示图片
            int len = 0;
            byte[] bytes = new byte[1024];
            response.setContentType("image/jpg");
            while((len = fileInputStream.read(bytes)) != 1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();

        } catch(Exception e){
            e.printStackTrace();
        }

    }
}
