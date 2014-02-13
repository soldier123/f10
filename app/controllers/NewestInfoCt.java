package controllers;

import bussiness.NewsInfoService;
import bussiness.NewsSearchService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.IndexInfo;
import dto.NewsAccessoryDto;
import dto.NewsInfoDto;
import dto.newestinfo.*;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import util.CommonUtils;
import util.Page;
import util.RedisKey;
import util.RedisUtil;

import java.util.*;

/**
 * 最新动态 控制器
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:32
 */
@With(value = {BaseInfoIntercept.class})
public class NewestInfoCt extends Controller {

    public static void info(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        long institutionId = sec.institutionId;
        boolean institutionIdIsValidate = BondSec.idIsValid(institutionId);
        Gson gson = CommonUtils.createGson();

        Map<String, Object> markUpPicData = new HashMap<String, Object>(); //行情画图数据
        List<Map<String,Object>> seriesData = Lists.newArrayList();
        //股票
        {
            QuoteMarkup markup = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.markup_stock + scode, QuoteMarkup.class);
            Map<String,Object> secItem = new HashMap<String, Object>();
            secItem.put("name", sec.name);
            secItem.put("data", quoteMarkupToArr2(markup,2));
            seriesData.add(secItem);
        }
        //行业指数
        {
            QuoteMarkup markup = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.markup_index + IndexInfo.indu_index_stock_relevance.get(scode), QuoteMarkup.class);
            Map<String,Object> secItem = new HashMap<String, Object>();
            IndexInfo indexInfo = IndexInfo.secCode2InduIdx(scode);
            secItem.put("name", indexInfo == null ? "" : indexInfo.name);
            secItem.put("data", quoteMarkupToArr2(markup,0));
            if(indexInfo!=null){
                seriesData.add(secItem);
            }
        }
        //沪深300
        {
            QuoteMarkup markup = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.markup_index + IndexInfo.INDEX_HS300, QuoteMarkup.class);
            Map<String,Object> secItem = new HashMap<String, Object>();
            secItem.put("name", "沪深300");
            secItem.put("data", quoteMarkupToArr2(markup,0));
            seriesData.add(secItem);
        }
        //市场指数
        {
            String maketIndexCode = IndexInfo.marketIndex_stock_relevance.get(scode);
            if(maketIndexCode == null){ //如果没有的话, 就认为是 深证成份指数
                maketIndexCode = IndexInfo.INDEX_SZ;
            }
            QuoteMarkup markup = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.markup_index + maketIndexCode, QuoteMarkup.class);
            Map<String,Object> secItem = new HashMap<String, Object>();
            IndexInfo indexInfo = IndexInfo.secCode2MarketIdx(scode);
            secItem.put("name", indexInfo == null ? "深证成份指数" : indexInfo.name); //没有的话, 就认为是 深证成份指数
            secItem.put("data", quoteMarkupToArr2(markup,0));
            seriesData.add(secItem);
        }

        markUpPicData.put("seriesData", seriesData);

        //计算最大值,最小值是否需要, 现在先不计算, 默认使用highChart自己计算的最大值,最小值. 如果要的话, 可以调用 CommonUtils.charMinMax 方法

        String markUpPicDataStr = gson.toJson(markUpPicData);

        /*
        String returnValue = "[{name: '深发展A',data: [1.0, 6.9, 9.5, 14.5, 15.2]}, " +
                "{name: '行业',data: [0.2, 0.8, 5.7, 11.3, 15.0]}, " +
                "{name: '沪深300',data: [0.9, 0.6, 3.5, 8.4, 13.5]}," +
                " {name: '上证指数',data: [3.9, 4.2, 5.7, 8.5, 11.9]}]";
                */

        //所属概念板块
        List<PlateDto> plateDtoList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.concept_plate + secA.code,  new TypeToken<List<PlateDto>>(){}.getType());

        //研报评级
        ReportRatingStatisticDto reportRating = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.report_rating + sec.secId, ReportRatingStatisticDto.class);

        //画图要用的数据
        Map<String, Object> reportRatingCharMap = new HashMap<String, Object>();
        if(reportRating != null){
            reportRatingCharMap.put("rating", CommonUtils.scaleNum(reportRating.calcLastRateNumber(), 0, "#.0"));
            reportRatingCharMap.put("sname", BondSec.secMap.get(scode) != null ? BondSec.secMap.get(scode) : "");
            reportRatingCharMap.put("reportDate", reportRating.statisticDate);
        }else{
            reportRating = new ReportRatingStatisticDto();
            reportRatingCharMap.put("rating","");
            reportRatingCharMap.put("sname", "");
            reportRatingCharMap.put("reportDate", "");
        }
        String reportRatingCharJson = gson.toJson(reportRatingCharMap);


        //取新闻,得到list
        F.T2<List<NewsInfoDto>, Page> newsT2 = NewsSearchService.secNewsShortInfoFromEs(1, 6, secA.code);
        List<NewsInfoDto> newsList = Lists.newArrayList();
        if (newsT2 != null) {
            newsList = newsT2._1;
        }

        //取公告,得到list
        F.T2<List<CompanyBulletin>, Page> bulletinT2 = NewsSearchService.secBulletinShortInfoFromEs(1, 6, secA.code);
        List<CompanyBulletin> bulletinsList = Lists.newArrayList();
        if (bulletinT2 != null) {
            bulletinsList = bulletinT2._1;
        }

        //取到投资要点
        String investMainpoint = RedisUtil.get(RedisKey.NewestInfo.invest_Mainpoint + secA.code);

        //大事提醒
        List<GreatInventRemind> remindList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.great_InventRemind + secA.secId,
                new TypeToken<List<GreatInventRemind>>(){}.getType(), gson);

        //三大主营业务
        List<Top3MainBusinessDto> top3MainBusinessList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.top3MainBusiness + sec.institutionId,
                new TypeToken<List<Top3MainBusinessDto>>(){}.getType(), gson);

        //财务比率
        List<FinancialRatiosDto> financialRatiosDtoList = null;
        if (institutionIdIsValidate) {
            financialRatiosDtoList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.financialRatios_short + sec.institutionId,
                            new TypeToken<List<FinancialRatiosDto>>(){}.getType(), gson);
        }else{
            financialRatiosDtoList = new ArrayList<FinancialRatiosDto>(0);
        }

        //财务画图数据
        List<FinanceDataShortDto> finDrawDataList = null;
        if (institutionIdIsValidate) {
            finDrawDataList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.financial_draw_data + sec.institutionId,
                            new TypeToken<List<FinanceDataShortDto>>(){}.getType(), gson);
        }
        if(finDrawDataList == null){
            finDrawDataList = new ArrayList<FinanceDataShortDto>(0);
        }

        //构建hightChart要的数据
        List<String> yearList = new ArrayList<String>();
        List<Double> mainIncomeYearData = Lists.newArrayList();
        List<Double> mainIncomeSeasonData = Lists.newArrayList();
        List<Double> netProfitYearData = Lists.newArrayList();
        List<Double> netProfitSeasonData = Lists.newArrayList();
        List<Double> epsYearData = Lists.newArrayList();
        List<Double> epsSeasonData = Lists.newArrayList();

        String seasonName = ""; //季节名称
        if (finDrawDataList.size() > 0) {
            int itemIndex = 0; //项的计数
            Calendar calendar =Calendar.getInstance();
            calendar.setTime(new Date());
            for (FinanceDataShortDto dto : finDrawDataList) { //日期按从小到大
                itemIndex ++;
                String year = dto.endDate.substring(0, 4);
                String monthDay = dto.endDate.substring(4);
                if(yearList.contains(year) == false){
                    yearList.add(year);
                }
                if(seasonName != null && ( "-12-31".equals(monthDay) == false ) ){ //不是年报
                    if("-03-31".equals(monthDay)) seasonName = "一季度";
                    if("-06-30".equals(monthDay)) seasonName = "中报";
                    if("-09-30".equals(monthDay)) seasonName = "三季度";
                }

                if("-12-31".equals(monthDay)){ //年报
                    mainIncomeYearData.add(CommonUtils.scaleNum(dto.mainIncome, -4, "#.00"));
                    netProfitYearData.add(CommonUtils.scaleNum(dto.netProfit, -4, "#.00"));
                    epsYearData.add(CommonUtils.scaleNum(dto.eps, 0, "#.0000")  );

                    if(itemIndex == 1){ //第一项是年报, 则要说明往前没有季度了. 要填充一个节点
                        mainIncomeSeasonData.add(CommonUtils.scaleNum(0, -4, "#.00"));
                        netProfitSeasonData.add(CommonUtils.scaleNum(0, -4, "#.00"));
                        epsSeasonData.add(CommonUtils.scaleNum(0, 0, "#.0000"));
                    }
                }else{
                    mainIncomeSeasonData.add(CommonUtils.scaleNum(dto.mainIncome, -4, "#.00"));
                    netProfitSeasonData.add(CommonUtils.scaleNum(dto.netProfit, -4, "#.00"));
                    epsSeasonData.add(CommonUtils.scaleNum(dto.eps, 0, "#.0000"));
                    // 没有当前年的全年数据时用0填充，避免绘图组建出错。修复BUG liuhj
                    if(String.valueOf(calendar.get(Calendar.YEAR)).equals(year)){
                        epsYearData.add(CommonUtils.scaleNum(0, 0, "#.0000"));
                        netProfitYearData.add(CommonUtils.scaleNum(0, 0, "#.0000"));
                        mainIncomeYearData.add(CommonUtils.scaleNum(0, 0, "#.0000"));
                    }
                }
            }
        }

        Map<String, Object>  drawData = new HashMap<String, Object>();
        drawData.put("yearList", yearList);
        drawData.put("seasonName", seasonName);
        drawData.put("mainIncomeYearData", mainIncomeYearData);
        drawData.put("mainIncomeSeasonData", mainIncomeSeasonData);
        drawData.put("netProfitYearData", netProfitYearData);
        drawData.put("netProfitSeasonData",netProfitSeasonData);
        drawData.put("epsYearData",epsYearData);
        drawData.put("epsSeasonData",epsSeasonData);
        String finDrawDataJson = gson.toJson(drawData);  //画图的json数据

        render(sec,
                markUpPicDataStr, newsList, bulletinsList, investMainpoint, remindList,
                top3MainBusinessList, financialRatiosDtoList, finDrawDataJson, reportRatingCharJson, reportRating, plateDtoList);
    }

    /**
     * 更多新闻
     * @param scode
     */
    public static void moreNews(String scode, Integer pageNo) {
        if(pageNo == null){
            pageNo = 1;
        }else if(pageNo < 0){
            pageNo = 1;
        }
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        F.T2<List<NewsInfoDto>, Page> t2 = NewsSearchService.secNewsShortInfoFromEs(pageNo, Page.DEFAULT_PAGE_SIZE, secA.code);
        List<NewsInfoDto> newsList = t2 != null ? t2._1 : Lists.<NewsInfoDto>newArrayList();
        NewsInfoDto newsInfo = new NewsInfoDto();
        if(newsList != null && newsList.size() > 0){
            newsInfo = newsList.get(0);
        }
        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        if (newsInfo.newsContent == null && "Y".equalsIgnoreCase(newsInfo.isAccessory)) {
            nadlist = NewsInfoService.getNewAccessList(newsInfo.newsId);
        }
        Page page = t2._2;
        render(newsList, sec, page,newsInfo,nadlist);
    }

    /**
     * 更多新闻下拉分页
     */
    public static void downPageNews(BondSec sec,int pageNo){
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        F.T2<List<NewsInfoDto>, Page> t2 = NewsSearchService.secNewsShortInfoFromEs(pageNo, Page.DEFAULT_PAGE_SIZE, secA.code);
        List<NewsInfoDto> newsList = t2 != null ? t2._1 : Lists.<NewsInfoDto>newArrayList();
        render(newsList);
    }


    /**
     * 更多公告
     * @param scode
     */
    public static void moreBulletin(String scode, Integer pageNo) {
        if(pageNo == null){
            pageNo = 1;
        }else if(pageNo < 0){
            pageNo = 1;
        }

        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);

        F.T2<List<CompanyBulletin>, Page> t2 = NewsSearchService.secBulletinShortInfoFromEs(pageNo, Page.DEFAULT_PAGE_SIZE, secA.code);
        List<CompanyBulletin> bulletinsList = t2 != null ? t2._1 : Lists.<CompanyBulletin>newArrayList();
        Page page = t2._2;
        render(bulletinsList, sec, page);
    }

    public static void downPageBulletin(BondSec sec,int pageNo){
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        F.T2<List<CompanyBulletin>, Page> t2 = NewsSearchService.secBulletinShortInfoFromEs(pageNo, Page.DEFAULT_PAGE_SIZE, secA.code);
        List<CompanyBulletin> bulletinsList = t2 != null ? t2._1 : Lists.<CompanyBulletin>newArrayList();
        render(bulletinsList);
    }

    //这个是从数据库中提取出来,直接就是计算好的
    @Util
    private static Double[] quoteMarkupToArr2(QuoteMarkup q,int scale){
        if(q == null){
            return new Double[]{null, null, null, null, null};
        }else {
            Double[] arr = new Double[5];
            arr[0] = q.w1 != null ? CommonUtils.scaleNum(q.w1, scale, "#.00")  : null;
            arr[1] = q.m1 != null ? CommonUtils.scaleNum(q.m1, scale, "#.00") : null;
            arr[2] = q.m3 != null ? CommonUtils.scaleNum(q.m3, scale, "#.00") : null;
            arr[3] = q.m6 != null ? CommonUtils.scaleNum(q.m6, scale, "#.00") : null;
            arr[4] = q.utillNow != null ? CommonUtils.scaleNum(q.utillNow, scale, "#.00") : null;
            return arr;
        }

    }

    //计算, 组成涨跌幅率
    @Util
    private static Double[] quoteMarkupToArr(QuoteMarkup q){
        Double[] arr = new Double[5];
        if (q != null) {
            if(q.last == null){
                arr = new Double[]{null, null, null, null, null};
            }else{
                if(q.w1 == null){
                    arr[0] = null;
                }else if(q.w1 == 0){ //避免除数为0的情况
                    arr[0] = 0d;
                }else{
                    arr[0] = CommonUtils.scaleNum((q.last - q.w1) / q.w1, 0, "#.00");
                }

                if(q.m1 == null){
                    arr[1] = null;
                }else if(q.m1 == 0){ //避免除数为0的情况
                    arr[1] = 0d;
                }else{
                    arr[1] = CommonUtils.scaleNum((q.last - q.m1) / q.m1, 0, "#.00");
                }

                if(q.m3 == null){
                    arr[2] = null;
                }else if(q.m3 == 0){ //避免除数为0的情况
                    arr[2] = 0d;
                }else{
                    arr[2] = CommonUtils.scaleNum((q.last - q.m3) / q.m3, 0, "#.00");
                }

                if(q.m6 == null){
                    arr[3] = null;
                }else if(q.m6 == 0){ //避免除数为0的情况
                    arr[3] = 0d;
                }else{
                    arr[3] = CommonUtils.scaleNum((q.last - q.m6) / q.m6, 0, "#.00");
                }

                if(q.utillNow == null){
                    arr[4] = null;
                }else if(q.utillNow == 0){ //避免除数为0的情况
                    arr[4] = 0d;
                }else{
                    arr[4] = CommonUtils.scaleNum((q.last - q.utillNow) / q.utillNow, 0, "#.00");
                }
            }
        } else {
            arr = new Double[]{null, null, null, null, null};
        }
        return arr;
    }
}
