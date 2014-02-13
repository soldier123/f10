package bussiness;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.WS;
import util.CommonUtils;
import util.NewsHelper;
import util.UserComposeInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 资讯索引service类
 * User: panzhiwei
 * Date: 13-3-27
 * Time: 下午5:35
 * To change this template use File | Settings | File Templates.
 */
public class MessageIndexService {
    public static final String requestUrl = String.valueOf(Play.configuration.get("search.url"));

    /**
     * 普通搜索列表
     *
     * @param cs
     * @return
     */
    public static List<MessageIndexDto> commonList(CommonSearchDto cs) {
        cs.startTime = getRealStartDateValueFromDetail(cs.startTime );
        if (CommonSearchDto._CUSTOM.equals(cs.timeName)) {
            cs.endTime = getRealEndDateValueFromDetail(cs.endTime);
            cs.startTime = getRealEndDateValueFromDetail(cs.startTime);
        } else {
            cs.endTime = getRealEndDateValueFromDetail("");
        }
        Gson gson = CommonUtils.createIncludeNullsWithExpose();
        String json = gson.toJson(cs);
        JsonElement result = WS.url(requestUrl).body(json).post().getJson();
        JsonObject jsonObject = result.getAsJsonObject();
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<MessageIndexDto>>() {
        }.getType();
        List<MessageIndexDto> messageIndexDtoList = gson.fromJson(messageData, type);
        if (StringUtils.isNotBlank(cs.keyWord) && messageIndexDtoList!=null) {
            for (MessageIndexDto dto : messageIndexDtoList) {
                dto.keyword = cs.keyWord;
            }
        }
        return messageIndexDtoList;
    }

    /**
     * 获取高级搜索
     *
     * @param advanceId    高级搜索ID
     * @param pageNo       页码
     * @param uci          用户组合信息
     * @param classifyList 从JS读取到的公告分类
     * @return
     */
    public static List<MessageIndexDto> advanceInfoSearch(long advanceId, int pageNo, UserComposeInfo uci, List<BulletinClassify> classifyList, CommonSearchDto cs) {

        String json = TemplateService.fetchUserTemplateContentById(advanceId, uci);
        MessageIndexCnd mic = new Gson().fromJson(json, MessageIndexCnd.class);
        return doAdvanceSearch(mic, classifyList, pageNo, cs);
    }

    public static List<MessageIndexDto> doAdvanceSearch(MessageIndexCnd mic, List<BulletinClassify> classifyList, int pageNo, CommonSearchDto cs) {
        //从数据库中取出用户高级搜索，向指定DTO填充
        AdvanceSearchDto asd = new AdvanceSearchDto();
        if (mic != null) {
            asd.nsource = mic.newsSource;
            asd.ncids.addAll(NewsHelper.getRealNewsClassifyIdByCode(mic.newsClassify));
            //公告分类根据JS读取的分类进行进一步的格式化处理得到最终要的ID
            if (mic.bulletinClassify.size() > 0) {
                F.T2<List<String>, List<String>> result = getRealAnnouncementClassifyId(mic.bulletinClassify, classifyList);
                asd.keyword = Joiner.on(" ").skipNulls().join(result._1);
                asd.acids = result._2;
            }
            if (mic.reportOrg.size() > 0) {
                List<String> reportOrg2 = new ArrayList<String>();
                for (int i = 0; i < mic.reportOrg.size(); i ++) {
                     String s = mic.reportOrg.get(i);
                    if (s.indexOf("||") > 0) {
                        s = s.replaceAll("\\|\\|", ",");
                    }
                     reportOrg2.add(s);
                }
                mic.reportOrg = reportOrg2;
            }
            asd.aiids = mic.bulletinPlateTree;
            asd.rsource = mic.reportOrg;
            asd.rcids = mic.reportClassify;
            asd.riids = mic.reportPlateTree;
            if (mic.advanceType.size()==0) {
                asd.itype = Lists.newArrayList("1", "2", "3");
            } else {
                asd.itype = mic.advanceType;
            }
            //asd.symbols = ; //暂无
        } else {
            asd.itype = Lists.newArrayList("1", "2", "3"); //这是如果是空 就全选的情况
        }
        if (CommonSearchDto._CUSTOM.equals(cs.timeName)) {
            cs.endTime = getRealEndDateValueFromDetail(cs.endTime);
            cs.startTime = getRealEndDateValueFromDetail(cs.startTime);
        }  else {
            cs.startTime = getRealStartDateValueFromDetail(cs.startTime);
            cs.endTime = getRealEndDateValueFromDetail(cs.endTime);
        }
        asd.startTime = cs.startTime;
        asd.endTime = cs.endTime;
        //处理类型
        if (cs.iType != 0 && asd.itype.size() > 0) {
            List<String> iTypeList = new ArrayList<String>();
            iTypeList.add(String.valueOf(cs.iType)); //iType转换成String后放入一个临时linkedlist以便取交集
            asd.itype.retainAll(iTypeList);
        }

        //高级搜索不需要关键字所以在此清空
        cs.keyWord = "";

        //记住开始时间
        cs.timeValue = cs.startTime;
        asd.keyword = cs.keyWord;
        if (pageNo > 0) {
            asd.pageNo = pageNo;
        } else {
            asd.pageNo = 1;
        }
        Gson gson = CommonUtils.createIncludeNulls();
        String asdJson = asd.toJson();
        Logger.info("资讯索引,send data to es= %s", asdJson);
        JsonElement result = WS.url(requestUrl).body(asdJson).post().getJson();
        JsonObject jsonObject = result.getAsJsonObject();
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<MessageIndexDto>>() {
        }.getType();
        List<MessageIndexDto> messageIndexDtoList = gson.fromJson(messageData, type);
        return messageIndexDtoList;
    }
    private static String getRealStartDateValueFromDetail(String detail){
        String datePartern = "yyyy-MM-dd";
        if (detail.equals("today") || detail.equals("")) {
            return CommonUtils.getFormatDate(datePartern, new Date());
        } else if (detail.equals("week")) {
            return CommonUtils.getFormatDate(datePartern, DateUtils.addWeeks(new Date(), -1));
        } else if (detail.equals("month")) {
            return CommonUtils.getFormatDate(datePartern, DateUtils.addMonths(new Date(), -1));
        } else if (detail.equals("threeMonth")) {
            return CommonUtils.getFormatDate(datePartern, DateUtils.addMonths(new Date(), -3));
        } else if (detail.equals("year")) {
            return  CommonUtils.getFormatDate(datePartern, DateUtils.addYears(new Date(), -1));
        } else {
            return detail;
        }
    }
    private static String getRealEndDateValueFromDetail(String detail){
        if (detail.equals("")) {
            return CommonUtils.getFormatDate("yyyy-MM-dd", new Date());
        } else {
            return detail;
        }
    }
    /**
     *
     * @param lists
     * @param classifyList
     * @author:liujianli@gtadata.com
     * @return
     * 说明  用于替换上面的formatString方法，上面方法太复杂了
     */
    private static F.T2<List<String>,List<String>> getRealAnnouncementClassifyId(List<String> lists, List<BulletinClassify> classifyList){
        Joiner joiner = Joiner.on(" ").skipNulls();
        List<String> keywords =  Lists.newArrayList();
        List<String> annClassifyIds = Lists.newArrayList();
        for(String str :lists){
            retriveAnnoucemnetClassifyId(str,classifyList,keywords,annClassifyIds);
        }
        return F.T2(keywords,annClassifyIds);
    }
    private static void retriveAnnoucemnetClassifyId(String myCode, List<BulletinClassify> classifyList,List<String> keywords, List<String> annClassifyIds){

            for(BulletinClassify classify : classifyList){
                if(myCode.equals(classify.code) || myCode.equals(classify.pcode)){
                    List<String> curClassifyIds = Lists.newArrayList();
                    if(classify.classfyId != null){
                        curClassifyIds.addAll(Lists.newArrayList(classify.classfyId));
                    }
                    if(curClassifyIds.size()>0 && !curClassifyIds.get(0).contains("%")){
                        annClassifyIds.addAll(curClassifyIds);
                    }else if(curClassifyIds.size()==1 && curClassifyIds.get(0).contains("%")){//含有%号的转成关键字,从目前的结构都是只有数组第一个元素可能含有%
                        keywords.add(curClassifyIds.get(0).replaceAll("%",""));
                    }
                   // break;
                }
                //去子节点找
                if(classify.children != null && classify.children.size()>0){
                    retriveAnnoucemnetClassifyId(myCode,classify.children,keywords,annClassifyIds);
                    //break;
                }
            }

    }
    //查询某个分类的最大日期 1新闻 2分告 3 研报
    public static Date getMaxDateFromES(String newsType){
        AdvanceSearchDto advanceSearchDto = new AdvanceSearchDto();
        advanceSearchDto.itype.add(newsType);
        advanceSearchDto.pageNo = 1;
        advanceSearchDto.pageSize=1;
        Gson gson = CommonUtils.createGson("yyyy-MM-dd HH:mm:ss");
        String asdJson = advanceSearchDto.toJson();
        Logger.info("查询最大日期,send data to es= %s", asdJson);
        JsonElement result = WS.url(requestUrl).body(asdJson).post().getJson();
        Logger.debug("查询最大日期,es返回信息:%s" + result.toString());
        JsonObject jsonObject = result.getAsJsonObject();
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<MessageIndexDto>>() {
        }.getType();
       List<MessageIndexDto> messageIndexDtos = gson.fromJson(messageData, type);
       if(messageIndexDtos !=null && messageIndexDtos.size()>0){
           return messageIndexDtos.get(0).declaredate;//第一条的日期即为最大日期
       }else{
           return new Date();//没有的话返回当天
       }
    }

}
