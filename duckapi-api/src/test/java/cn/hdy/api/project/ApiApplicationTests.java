package cn.hdy.api.project;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class ApiApplicationTests {

	@Test
	void contextLoads() {
		String txtUrl = "https://img.btstu.cn/api/images/5a0cfd6703524.jpg";
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
			String result = body.substring(start+11, end);
			System.out.println(result);
		}
	}

}
