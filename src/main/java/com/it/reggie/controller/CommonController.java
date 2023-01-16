package com.it.reggie.controller;

import com.it.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

//进行文件的上传与下载
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    //从application配置文件中取出路径
    @Value("${reggie.path}")
    private String basePath;

    //文件上传
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //获取原始文件名 并截取.jpg
        String originalFilename = file.getOriginalFilename();
        String suffx = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名,防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()+suffx;

        //创建一个目录对象
        File dir=new File(basePath);
        if (!dir.exists()){
            //如果目录不存在,需要创建
            dir.mkdir();
        }

        //收到的文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(fileName);
    }

    //下载功能
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流,通过输入流读取文件内容
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));
            //输出流
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpg");

            int len=0;
            byte[] bytes=new byte[1024];
           while ((len=fileInputStream.read(bytes))!=-1){
               outputStream.write(bytes,0,len); //向浏览器写数据
               outputStream.flush();
           }

           //关闭资源
            outputStream.close();
           fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
