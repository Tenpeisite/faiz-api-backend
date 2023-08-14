package com.zhj.apiclientsdk.utis;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/4 19:43
 */
public class SignUtils {
    /***
     * @description 生成签名
     * @param body
     * @param secretKey
     * @return java.lang.String
     * @author 朱焕杰
     * @date 2023/4/4 19:41
     */
    public static String getSign(String body, String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        String content = body + "." + secretKey;
        return md5.digestHex(content);
    }
}
