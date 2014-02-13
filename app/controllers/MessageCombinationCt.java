package controllers;

import bussiness.MessageIndexService;
import bussiness.NewsInfoService;
import bussiness.NewsSearchService;
import bussiness.ReportService;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.NewsInfoDto;
import dto.ReportDetailDto;
import dto.ReportDto;
import dto.newestinfo.CompanyBulletin;
import dto.newestinfo.GreatInventRemind;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.libs.F;
import play.mvc.Util;
import play.mvc.With;
import util.CommonUtils;
import util.Page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 资讯组合管理
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-9-16
 * Time: 上午10:31
 */
//@With({AuthorBaseIntercept.class})
public class MessageCombinationCt extends BaseController {


    /**
     * 组合详情页面
     */
    public static void info(){
        String scodeStr = getBody();
        //String scodeStr = "000001,000002,000003,000004";
        if(StringUtils.isBlank(scodeStr)) return;//容错处理（无值直接白掉）
        String[] scodesArry = scodeStr.split(",");

        //取新闻,得到list
        F.T2<List<NewsInfoDto>, Page> newsT2 = NewsSearchService.secNewsShortInfoFromEs(1, 6, scodesArry);
        List<NewsInfoDto> newsList = Lists.newArrayList();
        if (newsT2 != null) {
            newsList = newsT2._1;
        }

        //取公告,得到list
        F.T2<List<CompanyBulletin>, Page> bulletinT2 = NewsSearchService.secBulletinShortInfoFromEs(1, 6, scodesArry);
        List<CompanyBulletin> bulletinsList = Lists.newArrayList();
        if (bulletinT2 != null) {
            bulletinsList = bulletinT2._1;
        }

        //行业新闻
        F.T2<List<NewsInfoDto>,Page> induNewsDtos = NewsSearchService.secInduNewsFromEs(1,6, scodesArry);
        List<NewsInfoDto> induNewstList = Lists.newArrayList();
        if (induNewsDtos != null) {
            induNewstList = induNewsDtos._1;
        }

        //公司研报
        F.T2<List<ReportDto>,Page> reportDtos = NewsSearchService.secReportFromEs(1, 6, scodesArry);
        List<ReportDto> reportList = Lists.newArrayList();
        if (reportDtos != null) {
            reportList = reportDtos._1;
        }

        //行业研报
        F.T2<List<ReportDto>,Page> induReportDtos = NewsSearchService.secInduReportFromEs(1,6, scodesArry);
        List<ReportDto> induReportList = Lists.newArrayList();
        if (induReportDtos != null) {
            induReportList = induReportDtos._1;
        }

        //大事提醒
        F.T2<List<GreatInventRemind>,Page> greatInventReminds =  NewsInfoService.fetchSecRemindList(1, 6, scodesArry);
        List<GreatInventRemind> remindList = Lists.newArrayList();
        if (greatInventReminds != null) {
            remindList = greatInventReminds._1;
        }
        render(newsList,bulletinsList,induReportList,reportList,remindList,scodeStr,induNewstList);
    }


    /**
     * 更多大事提醒列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreRemindList(String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<GreatInventRemind>,Page> greatInventReminds =  NewsInfoService.fetchSecRemindList(pageNo, 20, scodes);
        List<GreatInventRemind> remindList = Lists.newArrayList();
        if (greatInventReminds != null) {
            remindList = greatInventReminds._1;
        }

        if(pageNo >1) render("@pageRemindList", remindList);
        render(remindList);
    }

    /**
     * 更多新闻列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreNewsList(String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<NewsInfoDto>, Page> newsT2 = NewsSearchService.secNewsShortInfoFromEs(pageNo, 20, scodes);
        List<NewsInfoDto> newsList = Lists.newArrayList();
        if (newsT2 != null) {
            newsList = newsT2._1;
        }

        if(pageNo>=2) render("@pageNewsList", newsList);
        render(newsList);
    }

    /**
     * 更多公告列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreBulletinsList(String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<CompanyBulletin>, Page> bulletinT2 = NewsSearchService.secBulletinShortInfoFromEs(pageNo, 20, scodes);
        List<CompanyBulletin> bulletinsList = Lists.newArrayList();
        if (bulletinT2 != null) {
            bulletinsList = bulletinT2._1;
        }
        //从第二页开始调用另外滚动的模板（滚动分页append需要）
        if(pageNo>=2) render("@pageBulletinsList", bulletinsList);
        render(bulletinsList);
    }

    /**
     * 更多行业新闻列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreInduNewsList( String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<NewsInfoDto>,Page> induNewsDtos = NewsSearchService.secInduNewsFromEs(pageNo,20, scodes);
        List<NewsInfoDto> induNewstList = Lists.newArrayList();
        if (induNewsDtos != null) {
            induNewstList = induNewsDtos._1;
        }
        if(pageNo>=2) render("@pageInduNewsList", induNewstList);
        render(induNewstList);
    }

    /**
     * 更多公司研报列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreReportList(String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<ReportDto>,Page> reportDtos = NewsSearchService.secReportFromEs(pageNo, 20, scodes);
        List<ReportDto> reportList = Lists.newArrayList();
        if (reportDtos != null) {
            reportList = reportDtos._1;
        }
        if(pageNo>1) render("@pageReportList", reportList);
        render(reportList);
    }

    /**
     * 根据研报获取研报摘要
     * @param reportId
     */
    public static void fetchReportSummary(long reportId){
        ReportDetailDto reportDetailDto = ReportService.getDetail(reportId);
        if(reportDetailDto==null){
            reportDetailDto = new ReportDetailDto();
        }
        render(reportDetailDto);
    }

    static Splitter sectionSplitter = Splitter.on('\n').trimResults().omitEmptyStrings();

    /**
     * 把研报导出到word
     * @param reportId
     */
    public static void exportReportSummary(long reportId){
        ReportDetailDto reportDetailDto = ReportService.getDetail(reportId);
        List<String> sectionList = Lists.newArrayList();
        if (reportDetailDto == null) {
            reportDetailDto = new ReportDetailDto();
        }else{
            sectionList = Lists.newArrayList(sectionSplitter.split(reportDetailDto.summary));
        }
        request.format="xml";
        String encodeTitle = "unknow";
        try {
            encodeTitle = URLEncoder.encode(reportDetailDto.title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + encodeTitle + ".doc");
        response.setContentTypeIfNotSet("application/msword");
        render(reportDetailDto, sectionList);
    }

    /**
     * 更多行业研报列表
     * @param pageNo
     * @param scodeStr
     */
    public static void moreInduReportList(String scodeStr, int pageNo){
        String[] scodes = scodeStr.split(",");
        F.T2<List<ReportDto>,Page> induReportDtos = NewsSearchService.secInduReportFromEs(pageNo,20, scodes);
        List<ReportDto> induReportList = Lists.newArrayList();
        if (induReportDtos != null) {
            induReportList = induReportDtos._1;
        }
        if(pageNo>1) render("@pageInduReportList",induReportList);
        render(induReportList);
    }
}
