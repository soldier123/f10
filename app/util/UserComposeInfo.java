package util;

/**
 * 用户信息组合.
 * 因为要融合nt系统跟qic系统. 这二者的用户还不在一个系统里. 所以要用uid + utype来标识
 * User: wenzhihong
 * Date: 13-3-18
 * Time: 下午1:07
 */
public class UserComposeInfo {
    public static final String key = "__UserComposeInfo"; //用于保存的key

    public Long uid;

    public String utype; //nt:nt用户, qic: qic用户. 目前就这两种

    public UserComposeInfo(Long uid, String utype) {
        this.uid = uid;
        this.utype = utype;
    }
}
