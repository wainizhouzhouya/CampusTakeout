package com.sky.test;


import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/10 19:18
 *Description:
 */
//@SpringBootTest
public class HttpClientTest {

    /**
     * 测试通过httpclient发送GET请求
     */
    @Test
    public void testGET() throws Exception{
        //创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //创建请求对象
        HttpGet httpGet=new HttpGet("http://localhost:8080/user/shop/status");

        //发送请求，接收响应结果
        CloseableHttpResponse response = httpClient.execute(httpGet);

        //获取服务端返回的状态码
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("服务端返回的状态码："+statusCode);

        //获取服务端返回的信息体
        HttpEntity entity = response.getEntity();
        //利用工具类将信息体转换格式
        String body= EntityUtils.toString(entity);
        System.out.println("服务端返回的信息体:"+body);

        //关闭资源
        response.close();
        httpClient.close();
    }

    /**
     * 测试通过httpclient发送POST请求
     */
    @Test
    public void testsPOST() throws Exception{
        //创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //创建请求对象
        HttpPost httpPost=new HttpPost("http://localhost:8080/admin/employee/login");

        //创建JSON数据格式的对象并添加数据
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("username","admin");
        jsonObject.put("password","123456");

        StringEntity entity=new StringEntity(jsonObject.toString());
        //指定请求编码格式
        entity.setContentEncoding("utf-8");
        //指定请求数据格式
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        //发送请求
        CloseableHttpResponse response = httpClient.execute(httpPost);

        //获取状态码
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("服务端返回的状态码:"+statusCode);

        //获取信息体
        HttpEntity entity1 = response.getEntity();
        //利用工具类将信息体转换格式
        String body = EntityUtils.toString(entity1);
        System.out.println("信息体："+body);

        //关闭资源
        response.close();
        httpClient.close();

    }
}
