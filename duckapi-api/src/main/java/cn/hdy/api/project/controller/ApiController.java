package cn.hdy.api.project.controller;

import cn.hdy.api.project.model.dto.WallpaperRequest;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 混沌鸭
 **/
@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * 随机壁纸
     * @param wallpaperRequest 壁纸请求参数
     * @return 壁纸网址
     */
    @GetMapping("/random/wallpaper")
    public String randomWallpaper(WallpaperRequest wallpaperRequest) {
        String method = wallpaperRequest.getMethod();
        String lx = wallpaperRequest.getLx();
        String format = wallpaperRequest.getFormat();
        String url = "https://api.btstu.cn/sjbz/api.php";
        String split = "?";
        // 封装参数
        if (StrUtil.isNotBlank(method)) {
            url = url + "?method=" + method;
            split = "&";
        }
        if (StrUtil.isNotBlank(lx)) {
            url = url + split + "lx=" + lx;
            split = "&";
        }
        if (StrUtil.isNotBlank(format)) {
            url = url + split + "format=" + format;
        }
        // 发送亲求
        HttpResponse response = HttpRequest.get(url).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 Edg/116.0.1938.76")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .execute();
        try (response) {
            String location = response.header("Location");
            if (StrUtil.isBlank(location)){
                return "生成失败";
            }
            return location;
        }
    }

    /**
     * 随机毒鸡汤
     * @param map 毒鸡汤请求参数
     * @return 毒鸡汤文本
     */
    @GetMapping("/random/chicken/soup")
    public String randomPoisonousChickenSoup(Map<String, String> map) {
        String charset = map.get("charset");
        String encode = map.get("encode");
        String url = "https://api.btstu.cn/yan/api.php";
        String split = "?";
        // 封装参数
        if (StrUtil.isNotBlank(charset)) {
            url = url + "?charset=" + charset;
            split = "&";
        }
        if (StrUtil.isNotBlank(encode)) {
            url = url + split + "encode=" + encode;
        }
        // 发送亲求
        HttpResponse response = HttpRequest.get(url).execute();
        try (response) {
            String body = response.body();
            return StrUtil.isBlank(body) ? "生成失败" : body;
        }
    }

    /**
     * 短网址生成
     * @param map 网址参数
     * @return 生成后的短网址
     */
    @PostMapping("/short/url/generation")
    public String shortUrlGeneration(@RequestBody Map<String, String> map) {
        String txtUrl = map.get("txtUrl");
        if (StrUtil.isBlank(txtUrl)){
            return "生成失败，输入的网址为空";
        }
        String url = "http://dwurl.cn/U/Index";
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "dwurl.cn");
        headers.put("Origin", "http://dwurl.cn");
        headers.put("Referer", "http://dwurl.cn/U/index");
        Map<String, Object> params = new HashMap<>();
        params.put("txtUrl", txtUrl);
        params.put("domain", "dwurl.cn");
        params.put("hidUrl", "");
        params.put("userid", "a3156493-e6f1-464f-bc20-f3b1864edc4b");
        // 发送亲求
        HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .form(params)
                .execute();
        try (response) {
            String body = response.body();
            int start = body.indexOf("ShortUrl\":\"");
            int end = body.indexOf("\",\"Code");
            if (start == -1){
                return "生成失败";
            }
            return body.substring(start+11, end);
        }
    }

}
