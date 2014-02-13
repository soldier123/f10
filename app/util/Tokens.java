package util;

import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Crypto;
import play.mvc.Scope;

/**
 * 跟gtasso里的 Tokens一样. 考虑要合并起来.
 * User: wenzhihong
 * Date: 13-3-18
 * Time: 上午10:31
 */
public class Tokens {
    static final String SPLIT_CON = "@@";
    static final String LOGIN_TOKEN_PRE = Play.configuration.getProperty("nt.sso.login.token.pre", "gtaNT");
    static final long LOGIN_TOKEN_LIVE_MSEC = Long.parseLong(Play.configuration.getProperty("nt.sso.login.token.liveSecond", String.valueOf(60L * 60L * 1L))) * 1000;

    /**
     * 解密登陆token信息
     *
     * @param token token字符串
     * @return 返回解密后的LoginTokenCompose对象, 如果解密不成功, 返回null
     */
    public static LoginTokenCompose decryptLoginToken(String token) {
        String ntssoAppSecret = Play.configuration.getProperty("nt.sso.application.secret").substring(0,16);
        if (org.apache.commons.lang.StringUtils.isEmpty(token)) {
            return null;
        }
        String decStr = null;
        try {
            decStr = Crypto.decryptAES(token, ntssoAppSecret);
        } catch (UnexpectedException e) { //解析失败, 也就是说还原不回来, 也认为是失败
            Logger.warn("解析登陆token失败");
            return null;
        }
        String[] splitStrArr = decStr.split(SPLIT_CON);
        if (splitStrArr.length == 6 && LOGIN_TOKEN_PRE.equals(splitStrArr[0])) {
            long createTime = 0;
            try {
                createTime = Long.parseLong(splitStrArr[1]);
            } catch (NumberFormatException e) {
                Logger.warn("解析long型(创建时间)(%s)出错", splitStrArr[1]);
                return null;
            }

            long uid = 0;
            try {
                uid = Long.parseLong(splitStrArr[3]);
            } catch (NumberFormatException e) {
                Logger.warn("解析long型(u_id)(%s)出错", splitStrArr[3]);
                return null;
            }

            long pid = 0;
            try {
                pid = Long.parseLong(splitStrArr[5]);
            } catch (NumberFormatException e) {
                Logger.warn("解析long型(p_id)(%s)出错", splitStrArr[5]);
                return null;
            }

            long disMsec = System.currentTimeMillis() - createTime;
            //if (disMsec > 0 && disMsec < LOGIN_TOKEN_LIVE_MSEC) {
            if ( 1 == 1) { //这里设置 1 == 1 一定成立, 是为了f10不设置过期
                LoginTokenCompose compose = new LoginTokenCompose();
                compose.userName = splitStrArr[2];
                compose.uid = uid;
                compose.pid = pid;
                compose.mac = splitStrArr[4];
                compose.createTime = createTime;
                return compose;
            }else{
                Logger.warn("token过期,已发生时间:%d, 设置最长过期时间%d", disMsec, LOGIN_TOKEN_LIVE_MSEC);
            }
        }

        return null;
    }

    /**
     * 检查登陆的token是否有效性
     *
     * @param token token字符串
     * @return 有效true, 无效false
     */
    public static boolean checkValidateLoginToken(String token) {
        return checkValidateLoginTokenAndSaveToRender(token, null);
    }

    /**
     * 检查登陆的token是否有效性, 并把解析出来的对象放在Render作用域上
     *
     * @param token token字符串
     * @return 有效true, 无效false
     */
    public static boolean checkValidateLoginTokenAndSaveToRender(String token, Scope.RenderArgs renderArgs) {
        LoginTokenCompose compose = decryptLoginToken(token);

        if (renderArgs != null && compose != null) {
            compose.saveToRender(renderArgs);
        }

        if (compose == null) {
            return false;
        } else {
            return true;
        }
    }
}
