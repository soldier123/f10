package bussiness;

import com.google.common.collect.Lists;
import dbutils.CustomDbUtil;
import dbutils.SqlLoader;
import dto.UserTemplate;
import util.UserComposeInfo;

import java.util.List;

/**
 * 订阅服务
 * User: liangbing
 * Date: 13-1-21
 * Time: 下午3:37
 */
public class TemplateService {

    /**
     * 订阅类型.
     * 101. 今日头条 102. 公司公告  103.研究报告   104.资讯所引
     */
    public static enum TemplateType {
        todayNews(101), bulletin(102), report(103), messageIndex(104);

        int type;
        TemplateType(int type){
            this.type = type;
        }
    }

    /**
     * 返回用户已定义的模板
     *
     * @param uci       用户信息
     * @param templType
     * @return 返回的UserTemplate包含属性: id, name, type, content
     */
    public static List<UserTemplate> fetchUserTemplateList(UserComposeInfo uci, TemplateType templType) {
        if (uci == null) {
            return Lists.newArrayList();
        }
        String sql = SqlLoader.getSqlById("fetchUserTemplateList");
        List<UserTemplate> templateList = CustomDbUtil.queryCustomDBBeanList(sql, UserTemplate.class, uci.uid, uci.utype, templType.type);
        return templateList;
    }

    /**
     * 获得订阅模板的内容
     */
    public static String fetchUserTemplateContentById(Long id, UserComposeInfo uci) {
        if (uci == null) {
            return "";
        }
        String sql = SqlLoader.getSqlById("fetchUserTemplateContentById");
        String content = CustomDbUtil.queryOneFieldOneRow(sql, String.class, id, uci.uid, uci.utype);
        return content == null ? "" : content;
    }

    public static UserTemplate fetchUserTemplateByName(String name, UserComposeInfo uci, TemplateType templType) {
        String sql = SqlLoader.getSqlById("fetchUserTemplateByName");
        UserTemplate userTemplate = CustomDbUtil.queryCustomDBSingleBean(sql, UserTemplate.class, name, uci.uid, uci.utype, templType.type);
        return userTemplate;
    }

    /**
     * 是否有重名. 重名返回 true, 不重名返回 false
     */
    public static boolean hasSameName(String name, UserComposeInfo uci, TemplateType templType){
        return fetchUserTemplateByName(name, uci, templType) != null;
    }

    /**
     * 增加用户定义模板
     * @param uci
     * @param name
     * @param content
     * @param templeType
     * @return
     */
    public static long addUserTemplate(UserComposeInfo uci, String name, String content, TemplateType templeType) {
        String sql = SqlLoader.getSqlById("addUserTemplate");
        if(name!=null){
            name = name.replaceAll("\\.","");//没查出原因为什么保存到数据库为出现省略号 ，先在这里堵一下
        }
        return CustomDbUtil.insert(sql, uci.uid, uci.utype, name, content, templeType.type);
    }

    public static void deleteUserTemplateById(Long id, UserComposeInfo uci){
        String sql = SqlLoader.getSqlById("deleteUserTemplateById");
        int effectRow = CustomDbUtil.updateCustomDB(sql, id, uci.uid, uci.utype);
    }

    public static void editUserTemplateById(Long id, String name, String content, UserComposeInfo uci) {
        if(name!=null){
            name = name.replaceAll("\\.","");//没查出原因为什么保存到数据库为出现省略号 ，先在这里堵一下
        }
        String sql = SqlLoader.getSqlById("editUserTemplateById");
        int effectRow = CustomDbUtil.updateCustomDB(sql, name, content, id, uci.uid, uci.utype );
    }
    public static void editUserTemplateById(Long id, String content, UserComposeInfo uci) {
        String sql = SqlLoader.getSqlById("editUserTemplateWithOutNameById");
        int effectRow = CustomDbUtil.updateCustomDB(sql, content, id, uci.uid, uci.utype );
    }

    public static UserTemplate findUserTemplateById(Long id) {
        String sql = SqlLoader.getSqlById("findUserTemplateById");
        return CustomDbUtil.queryCustomDBSingleBean(sql, UserTemplate.class, id);
    }
}
