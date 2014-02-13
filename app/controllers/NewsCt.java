package controllers;

import bussiness.NewsInfoService;
import bussiness.TemplateService;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import dto.NewsAccessoryDto;
import dto.NewsInfoDto;
import dto.ToDayNewsSearchCnd;
import dto.UserTemplate;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.mvc.Util;
import play.mvc.With;
import util.CommonUtils;
import util.NewsHelper;
import util.Page;
import util.UserComposeInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新闻
 * User: wenzhihong
 * Date: 13-1-17
 * Time: 上午10:24
 */
@With({AuthorBaseIntercept.class})
public class NewsCt extends BaseController {

    //开关变量, 是否从es还是db里搜索. 在没有理解代码前, 请不要修改这个变量的值.
    private static boolean fromEs = true;

    public static void todayNews(String content, int pageNo, String guid) {
        if (pageNo < 1) {
            pageNo = 1;
        }
        F.T2<List<NewsInfoDto>, Page> t2 = getMoreNewsUtil(content, pageNo);
        List<NewsInfoDto> newsList = t2._1;
        Page page = t2._2;
        NewsInfoDto newsInfo = null;

        if (StringUtils.isNotBlank(guid)) { //有传guid, 则显示guid新闻内容
            newsInfo = NewsInfoService.getNewsInfo(guid);
        } else if (newsList != null && newsList.size() > 0) {
            newsInfo = NewsInfoService.getNewsInfo(newsList.get(0).newsId);
        }

        if (newsInfo == null) {
            newsInfo = new NewsInfoDto();
        }

        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if ("Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsList.get(0).newsId);
        }
        //用户的自定义搜索条件
        UserComposeInfo uci = fetchUserComposeInfo();
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.todayNews);

        String maxDate;
        if (fromEs) {
            maxDate = NewsInfoService.fetchMaxDeclareDateFromEs();
        } else {
            maxDate =  NewsInfoService.fetchMaxDeclareDate();
        }
        render(newsInfo, newsList, page, utList, content, nadlist, maxDate);
    }

    /**
     * 滚动分页.从db小表里查找
     *
     * @param content 关键字
     * @param pageNo  当前页
     * @return
     */
    @Util
    private static F.T2<List<NewsInfoDto>, Page> getMoreNewsUtil(String content, int pageNo) {
        if (pageNo < 1) {
            pageNo = 1;
        }

        if (fromEs) {
            return NewsInfoService.searchFromEs(content, pageNo);
        } else {
            return NewsInfoService.search(content, pageNo);
        }
    }

    /**
     * 下拉分页
     * @param content
     * @param pageNo
     */
    public static void moreNews(String content, int pageNo) {
        if (pageNo < 1) {
            pageNo = 1;
        }
        F.T2<List<NewsInfoDto>, Page> t2 = getMoreNewsUtil(content, pageNo);
        List<NewsInfoDto> newsList = t2._1;
        render(newsList, content);
    }

    /**
     * ajax 获取新闻内容
     * @param newsId
     */
    public static void ajaxRefreshContent(String newsId) {
        NewsInfoDto newsInfo = NewsInfoService.getNewsInfo(newsId);
        if (newsInfo == null) {
            newsInfo = new NewsInfoDto();
        }
        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if ("Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsId);
            //newsInfo.newsContent = "内容请查看附件";
        }
        render(newsInfo, nadlist);
    }


    /**
     * 新闻明细. f10用
     * @param newsId 新闻id
     */
    public static void newsDetail(String newsId) {
        NewsInfoDto newsInfo = NewsInfoService.getNewsInfo(newsId);
        if (newsInfo == null) {
            newsInfo = new NewsInfoDto();
        }
        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if (newsInfo.newsContent==null&&"Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsId);
        }
        render(newsInfo, nadlist);
    }

    private static Splitter sectionSplitter = Splitter.on('\n').trimResults().omitEmptyStrings();

    /**
     * word导出新闻
     * @param newsId 新闻id
     */
    public static void exportNews(String newsId){
        NewsInfoDto newsInfo = NewsInfoService.getNewsInfo(newsId);
        List<String> sectionList = Lists.newArrayList();
        if (newsInfo == null) {
            newsInfo = new NewsInfoDto();
        }else{
            sectionList = Lists.newArrayList(sectionSplitter.split(newsInfo.newsContent));
        }
        request.format="xml";
        String encodeTitle = "unknow";
        try {
            encodeTitle = URLEncoder.encode(newsInfo.title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + encodeTitle + ".doc");
        response.setContentTypeIfNotSet("application/msword");
        render(newsInfo, sectionList);
    }

    /**
     * 检查是否有比date更新的新闻
     * @param date
     */
    public static void  examineNews(String date){
        Map<String, Object> json = Maps.newHashMap();
        int num = 0 ;
        if(StringUtils.isNotBlank(date)) {
            if (fromEs) {
                num = NewsInfoService.fetchNewNewstotalFromEs(CommonUtils.parseDate(date));
            } else {
                num = NewsInfoService.fetchNewNewstotal(CommonUtils.parseDate(date));
            }
        }
        if(num>0){
            json.put("content","新增"+num+"条新闻,点击刷新");
            json.put("success",true);
        }else{
            json.put("success",false);
        }
        renderJSON(json);

    }

    /**
     * 删除订阅新闻
     *
     * @param id 订阅id
     */
    public static void delCndNews(Long id) {
        UserComposeInfo uci = fetchUserComposeInfo();
        TemplateService.deleteUserTemplateById(id, uci);

        Map<String, Object> json = Maps.newHashMap();
        json.put("success", true);
        json.put("msg", "删除成功");

        renderJSON(json);
    }

    /**
     * 根据订阅名称查找订阅(唯一性)
     *
     * @param name
     */
    public static void findSubByName(String name) {
        Map<String, Object> json = new HashMap<String, Object>();
        UserComposeInfo uci = fetchUserComposeInfo();
        if (! TemplateService.hasSameName(name, uci, TemplateService.TemplateType.todayNews)) {
            json.put("success", true);
            renderJSON(json);
        } else {
            json.put("success", false);
            renderJSON(json);
        }
    }

    /**
     * 保存新闻订阅
     * @param name
     */
    public static void addNewsCnd(String[] newsClass, String[] source, String name,Long cid) {
        UserComposeInfo uci = fetchUserComposeInfo();

        ToDayNewsSearchCnd cnd = new ToDayNewsSearchCnd(source, newsClass);
        Map<String, Object> json = new HashMap<String, Object>();

        if(cid==-999){//新增
            if (cnd == null) {
                json.put("success", false);
                json.put("msg", "订阅条件不存在,请重新输入");
            } else {
                if (TemplateService.hasSameName(name, uci, TemplateService.TemplateType.todayNews)) { //重名
                    json.put("success", false);
                } else {
                    String content = CommonUtils.createGson().toJson(cnd);
                    long id = TemplateService.addUserTemplate(uci, name, content, TemplateService.TemplateType.todayNews);
                    if (id > 0) {
                        json.put("success", true);
                        json.put("msg", "保存成功!");
                        json.put("id", id);
                        json.put("info", content);
                    } else {
                        json.put("success", false);
                        json.put("mag", "保存失败!");
                    }
                }
            }
            renderJSON(json);
        }else{//修改已有的
            editNewsCnd(newsClass,source,name,1,cid);
        }
    }


    /**
     * 编辑新闻订阅信息
     * @param newsClass
     * @param source
     * @param name
     * @param id
     * @param updateName 是否修改订阅名称. 1名称做修改. 2名称不做修改
     */
    public static void editNewsCnd(String[] newsClass, String[] source, String name, int updateName, Long id) {
        UserComposeInfo uci = fetchUserComposeInfo();
        ToDayNewsSearchCnd cnd = new ToDayNewsSearchCnd(source, newsClass);
        Map<String, Object> json = Maps.newHashMap();
        if (cnd == null) {
            json.put("success", false);
            json.put("msg", "订阅条件不存在,请重新输入");
        } else {
            if (updateName == 1) {
                if (TemplateService.hasSameName(name, uci, TemplateService.TemplateType.todayNews)) {
                    json.put("success", false);
                    json.put("msg", "您输入的名称已存在,请重新输入!");
                } else {
                    json = editUserTemplate(name, id, uci, cnd);
                    json.put("id", id);
                }
            } else {
                json = editUserTemplate(name, id, uci, cnd);
                json.put("id", id);
            }
        }
        renderJSON(json);
    }

    @Util
    private static Map<String, Object> editUserTemplate(String name, Long id, UserComposeInfo uci, ToDayNewsSearchCnd cnd) {
        Map<String, Object> json = Maps.newHashMap();
        String content = CommonUtils.createGson().toJson(cnd);
        TemplateService.editUserTemplateById(id, name, content, uci);
        json.put("success", true);
        json.put("msg", "保存成功");
        json.put("info", content);
        return json;
    }


    /**
     * 按订阅条件搜索新闻.
     * @param id
     */
    public static void orderNews(Long id, int pageNo){
        UserComposeInfo userComposeInfo = fetchUserComposeInfo();
        F.T2<List<NewsInfoDto>, Page> t2;
        if (fromEs) {
            t2 = NewsInfoService.orderSelectFromEs(id, pageNo, userComposeInfo);
        } else {
            t2 = fetchNewsListByOrderId(id, pageNo);
        }

        List<NewsInfoDto> newsList = t2._1;
        Page page = t2._2;
        NewsInfoDto newsInfo = null;
        if (newsList != null && newsList.size() > 0) {
            newsInfo = NewsInfoService.getNewsInfo(newsList.get(0).newsId);
        }
        if (newsInfo == null) {
            newsInfo = new NewsInfoDto();
        }

        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if (StringUtils.isBlank(newsInfo.newsContent) && "Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsList.get(0).newsId);
            //newsInfo.newsContent = "内容请查看附件";
        }

        //用户的自定义搜索条件
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(userComposeInfo, TemplateService.TemplateType.todayNews);
        String maxDate = NewsInfoService.fetchMaxDeclareDate();
        render("@todayNews", newsInfo, newsList, page, utList, nadlist, id,maxDate);
    }

    /**
     * 按条件搜索的滚动分页
     * @param id
     * @param pageNo
     */
    public static void advanceMoreNews(long id, int pageNo) {
        if (pageNo < 1) {
            pageNo = 1;
        }
        UserComposeInfo userComposeInfo = fetchUserComposeInfo();

        F.T2<List<NewsInfoDto>, Page> t2;
        if (fromEs) {
            t2 = NewsInfoService.orderSelectFromEs(id, pageNo, userComposeInfo);
        } else {
            t2 = fetchNewsListByOrderId(id, pageNo);
        }

        List<NewsInfoDto> newsList = t2._1;
        render(newsList, id);
    }

    /**
     * 按订阅id获取新闻列表
     * @param id
     * @param pageNo 从 0 开始
     * @return
     */
    @Util
    private static F.T2<List<NewsInfoDto>, Page> fetchNewsListByOrderId(long id, int pageNo) {
        if (pageNo < 1) {
            pageNo = 1;
        }
        UserComposeInfo uci = fetchUserComposeInfo();

        //得到模板内容
        String content = TemplateService.fetchUserTemplateContentById(id, uci);
        Gson gson = CommonUtils.createGson();
        ToDayNewsSearchCnd cnd = gson.fromJson(content, ToDayNewsSearchCnd.class);

        List<String> realNewsClassifyList = Lists.newArrayList();
        if (cnd.newsClass != null && cnd.newsClass.length > 0) {
            realNewsClassifyList = NewsHelper.getRealNewsClassifyIdByCode(Lists.newArrayList(cnd.newsClass));
        }
        List<String> newsSourceList = Lists.newArrayList();
        if (cnd.source != null && cnd.source.length > 0) {
            newsSourceList = Lists.newArrayList(cnd.source);
        }
        String newClassifySql = ""; //新闻分类 sql 条件
        String newsSourceSql = ""; //新闻来源 sql 条件
        if (realNewsClassifyList.size() > 0) {
            newClassifySql = " ( " + CommonUtils.sqlStrJoin(realNewsClassifyList.iterator()) + " ) ";
        }
        if (newsSourceList.size() > 0) {
            newsSourceSql = " ( " + CommonUtils.sqlStrJoin(newsSourceList.iterator()) + " ) ";
        }

        return NewsInfoService.orderSelect(newClassifySql, newsSourceSql, pageNo);
    }
}
