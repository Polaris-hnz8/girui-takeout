package com.itcast.reggie.controller;

import com.itcast.reggie.common.R;
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

/**
 * 文件的上传与下载
 * 注意：这里的图片存储方式是将上传的资源全部直接存储在云端服务器上（业务处理+存储服务）
 * 注意：也可以采用阿里云oss对象存储服务作为图片资源存储地，相应的需要对前后端的代码进行小修改详见v2.0.x
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {//MultipartFile的名字必须与前端发送的name保持一致
        //用户将图片上传到服务器的指定目录中
        log.info(file.toString());

        //1.获取原始文件名 以及文件后缀名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //2.使用UUID为上传的文件重新生成文件名，防止文件名重复造成文件覆盖(生成随机的30多位的字符串)
        String fileName = UUID.randomUUID().toString() + suffix;

        //3.如果目标存储路径不存在，则需要创建目录
        File directory = new File(basePath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        //4.file是一个临时文件需要及时转存到指定的位置，否则本次请求完成后临时文件将会被删除
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //5.返回上传的文件名称
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        //从服务器指定的文件路径中下载到浏览器
        try {
            //1.通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //2.通过输出流将文件内容写回浏览器，然后在浏览器中进行展示
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //3.关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
