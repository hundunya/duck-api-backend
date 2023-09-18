package cn.hdy.backend.project.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author 混沌鸭
 **/
@Component
public class ImageUtils {
    @Value("${spring.tencent.SecretId}")
    private String secretId;

    @Value("${spring.tencent.SecretKey}")
    private String secretKey;

    @Value("${spring.tencent.region}")
    private String region;

    @Value("${spring.tencent.bucketName}")
    private String bucketName;

    private COSClient cosClient;

    /**
      初始化客户端
     */
    private void initCosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成 cos 客户端。
        cosClient = new COSClient(cred, clientConfig);
    }

    /**
     * 上传文件
     */
    public String upLoad(String directory, File file) {
        initCosClient();
        try {
            String name = file.getName();
            String key = directory + UUID.randomUUID() + name.substring(name.lastIndexOf("."));
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
            cosClient.putObject(putObjectRequest);
            return "https://duck-image.hundunya.cn/" + key;
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
            return "";
        }
    }

    /**
     * 下载文件
     */
    public String downLoad(String directory, String fileName) throws IOException {
        // Bucket的命名格式为 BucketName-APPID ，此处填写的存储桶名称必须为此格式
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, directory + fileName);
        // 限流使用的单位是 bit/s, 这里设置下载带宽限制为10MB/s
        getObjectRequest.setTrafficLimit(80*1024*1024);
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        COSObjectInputStream cosObjectInput = cosObject.getObjectContent();
        ObjectMetadata objectMetadata = cosObject.getObjectMetadata();
        String contentType = objectMetadata.getContentType();
        byte[] bytes = cosObjectInput.readAllBytes();
        Base64.Encoder encoder = Base64.getEncoder();
        String str = encoder.encodeToString(bytes);
        // 关闭输入流
        cosObjectInput.close();
        return "data:" + contentType + ";base64," + str;
    }
}
