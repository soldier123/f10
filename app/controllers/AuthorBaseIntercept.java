package controllers;

import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import util.LoginTokenCompose;
import util.Tokens;
import util.UserComposeInfo;

/**
 * 基本 拦截器, 提供一些公用的功能.
 * before, after, finally, catch 的 priority 0 -- 29 做为系统保留使用
 * 用这个拦截器控制所有要登陆的功能的使用. 在controller方法里只要定义 Long uid 这个一个参数, 框架就会自动把当前用户的id给这个变量.
 * User: wenzhihong
 * Date: 12-11-11
 * Time: 下午3:40
 */
public class AuthorBaseIntercept extends Controller {
    public static final String USER_ID_SESSION_KEY = "uid";

    /**
     * 获取nt用户的token
     */
    @Before(priority = 1, unless = "NewsCt.newsDetail")
    static void fetchNtUserId(){
        String ntToken = params.get("ntToken");
        boolean fromCookie = false;
        if (StringUtils.isBlank(ntToken)) {
            Http.Cookie ntTokenCookie = request.cookies.get("ntToken");
            if (ntTokenCookie != null) {
                ntToken = ntTokenCookie.value;
                fromCookie = true;
            }
        }

        if (!fromCookie && StringUtils.isNotBlank(ntToken)) {
            //设置cookie
            response.setCookie("ntToken", ntToken);
        }

        //登陆token解密信息,并存放于render中
        Tokens.checkValidateLoginTokenAndSaveToRender(ntToken, Scope.RenderArgs.current());
        LoginTokenCompose compose = LoginTokenCompose.current();
        if (compose != null) {
            renderArgs.put(UserComposeInfo.key, new UserComposeInfo(compose.uid, "nt"));
        }
    }


    /**
     * 获取qic用户的id
     */
    @Before(priority = 5, unless = "NewsCt.newsDetail")
    static void fetchQicUserId() {
        String uidFromSession = session.get(USER_ID_SESSION_KEY);
        if (StringUtils.isNotBlank(uidFromSession)) {
            renderArgs.put(UserComposeInfo.key, new UserComposeInfo(new Long(uidFromSession), "qic"));
        }
    }

}
