package controllers;

import bussiness.MessageIndexService;
import bussiness.NewsInfoService;
import bussiness.TemplateService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.*;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Util;
import play.mvc.With;
import play.vfs.VirtualFile;
import util.CommonUtils;
import util.UserComposeInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资讯索引
 * User: panzhiwei
 * Date: 13-3-26
 * Time: 下午3:51
 */
@With({AuthorBaseIntercept.class})
public class MessageIndexCt extends BaseController {
    protected static final String EXCEL_FILE_SUFFIX = "xls";

    //股票池分类列表
    static List<BulletinClassify> bulletinClassifies;

    static {
        VirtualFile vf = Play.getVirtualFile("public/js/treeResource/bulletinOrg.js");
        String json = CommonUtils.readJsonConfigFile2String(vf.inputstream());
        bulletinClassifies = new Gson().fromJson(json, new TypeToken<List<BulletinClassify>>() {
        }.getType());
    }

    /**
     * 滚动分页枚举类型  1：普通搜索 2：高级搜索 3：直接搜索
     */
    public static enum MoreType {
        commonSearch(1), advanceSearch(2), directSearch(3);

        int type;

        MoreType(int type) {
            this.type = type;
        }
    }

    /**
     * 资讯索引列表
     *
     * @param cs
     * @param pageNo
     */
    public static void messageIndex(CommonSearchDto cs, int pageNo) {
        cs = cs == null ?new  CommonSearchDto():cs;
        /*增加了一个标识字段,如果没有修改自定义时间,但是点击自定义,它会给startTime赋值custom,所以我定义了一个标识字段,用于保存
        自定义的开始时间,当它没有修改但是点击了,我就把custom改为tempField,*/
        if("custom".equals(cs.startTime)){
            cs.startTime =cs.tempField;
        }
        cs.timeValue = cs.startTime;
        F.T2<List<MessageIndexDto>, Integer> t2 = getMessageIndexList(cs, pageNo);
        List<MessageIndexDto> messageIndexDtoList = t2._1;
        int moreType = t2._2;
        //我的订阅
        UserComposeInfo uci = fetchUserComposeInfo();
        List<UserTemplate> utList = null;
        if (uci == null) {
            utList = Lists.newArrayList();
        } else {
            utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.messageIndex);
        }
        render(messageIndexDtoList, cs, pageNo, utList, moreType);
    }

    @Util
    private static F.T2<List<MessageIndexDto>, Integer> getMessageIndexList(CommonSearchDto cs, int pageNo) {
        List<MessageIndexDto> messageIndexDtoList  = null;
        if(cs!=null && StringUtils.isNotBlank(cs.keyWord)){
            cs.searchType = 1;
        }
        int moreType = cs.searchType;
        long advanceId  = cs.advanceId;
        switch (moreType){
            case 2:
                messageIndexDtoList = doAdvanceInfoSearch(advanceId, cs, pageNo);
                break;
            case 3:
                messageIndexDtoList =  doDirectAdvanceSearch(pageNo, cs) ;
                break;
            default:
                messageIndexDtoList = doCommonInfoSearch(cs);
        }
        return F.T2(messageIndexDtoList,moreType);
    }

    public static void moreMessage(CommonSearchDto cs, int pageNo) {
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        cs.pageNo = pageNo;
        List<MessageIndexDto> messageIndexDtoList = MessageIndexService.commonList(cs);
        render(messageIndexDtoList, cs, pageNo);
    }

    /**
     * excel 导出
     *
     * @param cs
     */
    public static void exportMessageIndex(CommonSearchDto cs, int pageNo) {
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        cs.pageSize = 1000;
        F.T2<List<MessageIndexDto>, Integer> t2 = getMessageIndexList(cs, pageNo);
        List<MessageIndexDto> list = t2._1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put("dateFormat", dateFormat);
        renderArgs.put(RenderExcel.RA_FILENAME, "资讯索引.xls");
        request.format = EXCEL_FILE_SUFFIX;
        render(list);

    }

    /**
     * 高级搜索
     *
     * @param advanceId 高级搜索ID
     */
    private static  List<MessageIndexDto>  doAdvanceInfoSearch(long advanceId, CommonSearchDto cs, int pageNo) {

        if (advanceId < 0) advanceId = 0;
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        UserComposeInfo uci = fetchUserComposeInfo();
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.messageIndex);

        //从JS读取到的公告分类
        List bulletinList = bulletinClassifies;
        List<MessageIndexDto> messageIndexDtoList = MessageIndexService.advanceInfoSearch(advanceId, pageNo, uci, bulletinList, cs);
        int moreType = MoreType.advanceSearch.type; //高级搜索标识(滚动分页需要)
        return   messageIndexDtoList;
       // render("@messageIndex", messageIndexDtoList, cs, utList, moreType, advanceId);
    }

    private static  List<MessageIndexDto>  doCommonInfoSearch(CommonSearchDto cs) {

        if (cs == null) {
            cs = new CommonSearchDto();
        }
        if (cs.keyWord.equals("null")) {
            return null;
        }
        //cs.timeValue = cs.startTime;
        List<MessageIndexDto> messageIndexDtoList = MessageIndexService.commonList(cs);
        return    messageIndexDtoList;
    }

    public static void moreAdvanceSearch(long advanceId, CommonSearchDto cs, int pageNo) {
        if (advanceId < 0) advanceId = 0;
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        if(cs!=null&&cs.advanceId>0){
            advanceId = cs.advanceId;
        }
        advanceId = cs.advanceId;
        UserComposeInfo uci = fetchUserComposeInfo();
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.messageIndex);

        //从JS读取到的公告分类
        List bulletinList = bulletinClassifies;
        List<MessageIndexDto> messageIndexDtoList = MessageIndexService.advanceInfoSearch(advanceId, pageNo, uci, bulletinList, cs);
        render("@moreMessage", messageIndexDtoList, pageNo);
    }

    //验证是否重名
    public static void findReportByName(String name) {
        Map<String, Object> json = Maps.newHashMap();
        UserComposeInfo uci = fetchUserComposeInfo();
        if (!TemplateService.hasSameName(name, uci, TemplateService.TemplateType.messageIndex)) {
            json.put("success", true);
            renderJSON(json);
        } else {
            json.put("success", false);
            renderJSON(json);
        }
    }

    //保存订阅
    public static void addMessageIndexCnd(MessageIndexCnd cnd, String name) {
        UserComposeInfo uci = fetchUserComposeInfo();
        Map<String, Object> json = new HashMap<String, Object>();
        if (cnd == null) {
            json.put("success", false);
            json.put("msg", "订阅条件不存在,请重新输入");
        } else {
            String content = cnd.toJson();
            long id = TemplateService.addUserTemplate(uci, name, content, TemplateService.TemplateType.messageIndex);
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

        renderJSON(json);
    }

    //编辑订阅
    public static void editMessageIndexCnd(MessageIndexCnd cnd,Long id) {
        Map<String, Object> json = new HashMap<String, Object>();
        if (cnd == null) {
            json.put("success", false);
            json.put("msg", "订阅条件不存在,请重新输入");
        } else {
            String content = cnd.toJson();
            UserComposeInfo uci = fetchUserComposeInfo();
            TemplateService.editUserTemplateById(id, content, uci);
            json.put("success", true);
            json.put("msg", "保存成功");
            json.put("info", content);
        }
        renderJSON(json);
    }

    //查看新闻内容
    public static void getNewsInfo(String newsId) {
        NewsInfoDto newsInfo = NewsInfoService.getNewsInfo(newsId);
        if (newsInfo != null &&newsInfo.newsContent==null) {
            newsInfo.newsContent = "暂无新闻";
        }
        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if ("Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsId);
        }
        if(nadlist.size()>0){
            NewsAccessoryDto newsAccessoryDto = nadlist.get(0);
            newsInfo.accessOryroute = newsAccessoryDto.accessOryroute;
            newsInfo.fullName = newsAccessoryDto.fullName;
            newsInfo.filenameExtension = newsAccessoryDto.filenameExtension;
        }
        renderJSON(newsInfo);
    }

    /**
     * 搜索条件重命名或新建
     */
    public static void renameOrNewCond(Long id, String name) {
        UserComposeInfo uci = fetchUserComposeInfo();
        if (id == -999) { //这是新加一个
            Map<String, Object> json = null;

            json = addCond(new MessageIndexCnd(), name, uci);

            if (json == null) {
                json = new HashMap<String, Object>();
            }
            renderJSON(json);
        } else {
            UserTemplate ut = TemplateService.findUserTemplateById(id);
            Map<String, Object> json = new HashMap<String, Object>();
            if (ut == null) {
                json.put("op", false);
                json.put("msg", "自定义查询条件不存在");
            } else {
                TemplateService.editUserTemplateById(id, name, new MessageIndexCnd().toJson(), uci);
                json.put("op", true);
                json.put("msg", "改名成功");
            }
            renderJSON(json);
        }

    }

    @Util
    static Map<String, Object> addCond(MessageIndexCnd cnd, String cndName, UserComposeInfo uci) {
        Map<String, Object> json = new HashMap<String, Object>();

        //先检查一下是否重名
        if (TemplateService.hasSameName(cndName, uci, TemplateService.TemplateType.messageIndex)) {
            json.put("op", false);
            json.put("msg", "名称已存在");
            json.put("cndName", cndName);
            json.put("sameName", true);
        } else {
            Long id = TemplateService.addUserTemplate(uci, cndName, cnd.toJson(), TemplateService.TemplateType.messageIndex);
            json.put("op", true);
            json.put("msg", "增加成功");
            json.put("id", id);
            json.put("utInfo", cnd);
        }
        return json;
    }

    /**
     * 不保存搜索条件直接搜索
     *
     * @param pageNo
     */
    private static List<MessageIndexDto>  doDirectAdvanceSearch(int pageNo, CommonSearchDto cs) {
        MessageIndexCnd cnd = new MessageIndexCnd();
        cnd.newsSource = cs.newsSource;
        cnd.newsClassify = cs.newsClassify;
        cnd.bulletinClassify = cs.bulletinClassify;
        cnd.bulletinPlateTree = cs.reportPlateTree;
        cnd.reportClassify = cs.reportClassify;
        cnd.reportOrg = cs.reportOrg;
        cnd.reportPlateTree = cs.reportPlateTree;
        cnd.advanceType = cs.advanceType;
        List<BulletinClassify> bulletinList = bulletinClassifies;
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        List<MessageIndexDto> messageIndexDtoList = null;
        if (cs.advanceId != 0) {
            messageIndexDtoList = doAdvanceInfoSearch(cs.advanceId, cs, pageNo);
        } else {
            messageIndexDtoList = MessageIndexService.doAdvanceSearch(cnd, bulletinList, pageNo, cs);
        }

       /* UserComposeInfo uci = fetchUserComposeInfo();
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.messageIndex);
        int moreType = MoreType.directSearch.type;*/
        return   messageIndexDtoList;
        //render("@messageIndex", messageIndexDtoList, cs, utList, moreType);
    }

    public static void moreDirectAdvanceSearch(int pageNo, CommonSearchDto cs) {
        List<BulletinClassify> bulletinList = bulletinClassifies;
        MessageIndexCnd cnd = new MessageIndexCnd();
        cnd.newsSource = cs.newsSource;
        cnd.newsClassify = cs.newsClassify;
        cnd.bulletinClassify = cs.bulletinClassify;
        cnd.bulletinPlateTree = cs.reportPlateTree;
        cnd.reportClassify = cs.reportClassify;
        cnd.reportOrg = cs.reportOrg;
        cnd.reportPlateTree = cs.reportPlateTree;
        cnd.advanceType = cs.advanceType;
        if (cs == null) {
            cs = new CommonSearchDto();
        }
        List<MessageIndexDto> messageIndexDtoList = null;
        if (cs.advanceId != 0) {
            messageIndexDtoList = doAdvanceInfoSearch(cs.advanceId, cs, pageNo);
        } else {
            messageIndexDtoList = MessageIndexService.doAdvanceSearch(cnd, bulletinList, pageNo, cs);
        }
        render("@moreMessage", messageIndexDtoList, cnd, pageNo, cs);
    }

}
