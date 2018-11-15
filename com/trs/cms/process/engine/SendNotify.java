package com.trs.cms.process.engine;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.security.MessageDigest;
import java.util.StringTokenizer;


/**
 * Created by Administrator on 2016/11/29.
 */
public class SendNotify {
    private static String idsUrl = "http://10.3.64.119:8080/ids";
    private static String serviceUrl = idsUrl + "/service?idsServiceType=remoteapi&method=sendMessage";
    
    public static void sendEmail(String data) throws Exception {
        PostMethod methodPost = new PostMethod(serviceUrl);
        methodPost.addParameter("appName", "xiezuo.wcmv7");
        methodPost.addParameter("type", "json");

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data.getBytes("UTF-8"));
        byte[] digestByte = md.digest();
        // 将生成的摘要转成16进制数表示
        String digest = StringHelper.toString(digestByte);
        String base64Encoded = new String(Base64.encodeBase64(data.getBytes("UTF-8")));
        // 对data进行加密处理 并将结果转换成16进制表示
        String dataAfterDESEncode = DesEncryptUtil.encryptToHex(base64Encoded.getBytes("UTF-8"), "12345678");
        String finalData = digest + "&" + dataAfterDESEncode;
        methodPost.addParameter("data", finalData);
        HttpClient httpClient = new HttpClient();
        httpClient.executeMethod(methodPost);
        String response = new String(methodPost.getResponseBody(), "utf-8");

        // 拆分摘要和结果信息
        String[] digestAndResult = StringHelper.split(response, "&");
        String digestOfServer = digestAndResult[0];
        String result = digestAndResult[1];
        // 解密响应结果
        String afterDESResult = DesEncryptUtil.decrypt(result, "12345678");
        String afterBase64Decode = new String(Base64.decodeBase64(afterDESResult.getBytes("UTF-8")), "UTF-8");
        MessageDigest sd = MessageDigest.getInstance("MD5");
        sd.update(afterBase64Decode.getBytes("UTF-8"));
        String digestOfAgent = StringHelper.toString(sd.digest());
        // 比较生成的摘要与响应结果中的摘要是否一致
        System.out.println(afterBase64Decode);

        methodPost.releaseConnection();
    }
}
