package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;
import util.UserComposeInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 把一些常用的方法放在这里
 * User: wenzhihong
 * Date: 13-3-18
 * Time: 上午10:50
 */
public class BaseController extends Controller {
    /**
     * 返回用户包装信息
     */
    @Util
    protected static UserComposeInfo fetchUserComposeInfo() {
        UserComposeInfo uci = null;
        uci = (UserComposeInfo) renderArgs.get(UserComposeInfo.key);

        if (uci == null) {
            Logger.warn("没有找到用户包装信息...在处理用户相关资源,可能会出错");
        }

        return uci;
    }

    /**
     * （由于框架本身已经对其他非法情况进行过滤）此处之针对ID为负数的情况过滤为0
     * @param id 常规ID
     * @return 过滤后的ID
     */
    protected static long  filterId(long id){
        if (id <0){
            return 0;
        }else {
            return id;
        }
    }

    /**
     * 获取请求的body信息
     * @return
     */
    protected static String getBody() {
        InputStream is = request.body;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        StringBuffer sb = new StringBuffer();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Logger.error("请取数据出错:\n%s" ,  sb.toString());
        }
        Logger.debug("收到请求数据:\n%s" , sb.toString());
        return sb.toString();
    }
}
