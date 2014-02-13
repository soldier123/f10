package controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.industryana.CompanyScale;
import dto.newestinfo.ReportRatingStatisticDto;
import dto.predictprofit.DetailByReport;
import dto.predictprofit.PriceIt2;
import dto.predictprofit.PriceItem;
import dto.predictprofit.RatingChange;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.HighChartDataType;
import util.RedisKey;
import util.RedisUtil;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 盈利预测
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:48
 */
@With(value = {BaseInfoIntercept.class})
public class PredictProfitCt extends Controller {

    public static void info(String scode){
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();
        //研报评级
        ReportRatingStatisticDto reportRating = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.report_rating + sec.secId, ReportRatingStatisticDto.class, gson);
        //画图要用的数据
        Map<String, Object> reportRatingCharMap = Maps.newHashMap();
        if(reportRating != null){
            reportRatingCharMap.put("rating", CommonUtils.scaleNum(reportRating.calcLastRateNumber(), 0, "#.00"));
            reportRatingCharMap.put("sname", BondSec.secMap.get(scode) != null ? BondSec.secMap.get(scode) : "");
            reportRatingCharMap.put("reportDate", reportRating.statisticDate);
        }else{
            reportRating = new ReportRatingStatisticDto();
            reportRatingCharMap.put("rating","");
            reportRatingCharMap.put("sname", "");
            reportRatingCharMap.put("reportDate", "");
        }

        String reportRatingCharJson = gson.toJson(reportRatingCharMap);

        //评级变动
        List<RatingChange> ratingChangeList = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.ratingChange + sec.secId,
                new TypeToken<List<RatingChange>>(){}.getType(), gson);
        RatingChange ratingChange=null;
        if(ratingChangeList!=null&&ratingChangeList.size()>0){
            ratingChange = ratingChangeList.get(0);
        }

        //画图数据为  预测目标价 [[Date.UTC(2010, 0, 1), 29.9], [Date.UTC(2010, 0, 2), 71.5]]  格式
        //当前价 [[Date.UTC(2010, 0, 1), 29.9], [Date.UTC(2010, 0, 2), 71.5]] 格式
        //预测目标价图
        Type priceItemType = new TypeToken<List<PriceItem>>(){}.getType();
        final List<PriceItem> targetPriceList = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.targetPrice + sec.secId, priceItemType, gson);
        //近一个月的价格
        List<PriceItem> actualPriceList = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.actualPrice1Month + sec.secId, priceItemType, gson);
        List<Date> dateList = Lists.newArrayListWithCapacity(32); //近一个月的时间

        List<Object[]> targetPriceCharList = Lists.newLinkedList();
        List<Object[]> actualPriceCharList = Lists.newLinkedList();
        String targetPriceCharJson = "[]"; //目标价画图的数据json格式
        String actualPriceCharJson = "[]"; //近一个月的价格画图的数据json格式

        double forecastAvgPrice = 0; //目标均价
        double newestPrice = 0; //最新价
        double rating = 0;//涨幅

        if(actualPriceList != null && actualPriceList.size() > 0){
            for (PriceItem p : actualPriceList) {
                dateList.add(p.pdate);
                actualPriceCharList.add(new Object[]{new HighChartDataType.HCDate(p.pdate), CommonUtils.scaleNum(p.price, 0, "#.00")});
            }
            actualPriceCharJson = CommonUtils.toJsonWithHighChartDataType(actualPriceCharList);

            newestPrice = actualPriceList.get(actualPriceList.size()-1).price;
        }
        if(targetPriceList != null && targetPriceList.size() > 0){
            double sum = 0;
            for (PriceItem tp : targetPriceList) {
                sum += tp.price;
                //这里数组里的第三个第四个元素在 predictprofitct/info.js 里要用到. 用于提示信息
                targetPriceCharList.add(new Object[]{
                        new HighChartDataType.HCDate(tp.pdate),
                        CommonUtils.scaleNum(tp.price, 0, "#.00"),
                        StringUtils.defaultString(tp.priceterm), //周期
                        StringUtils.defaultString(tp.institutionname) //研报发布机构
                });
            }
            targetPriceCharJson = CommonUtils.toJsonWithHighChartDataType(targetPriceCharList);
            forecastAvgPrice = sum / targetPriceList.size();
        }

        if(newestPrice != 0){
            rating = (forecastAvgPrice - newestPrice) / newestPrice * 100;
        }


        //机构预测图
        //年度数据格式   ['2010', '2011', '2012']
        //eps 数据格式 [1.2, 1.3, 1.4]
        //净利润数据格式 [12, 23, 26]
        Type priceIt2Type = new TypeToken<List<PriceIt2>>(){}.getType();
        List<PriceIt2> last5YearEps = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.last5YearEps + sec.institutionId, priceIt2Type, gson);
        List<PriceIt2> last5YearNetProfit = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.last5YearNetProfit + sec.institutionId, priceIt2Type, gson);
        List<PriceIt2> forecast3YearEps = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.forecast3YearEps + sec.secId, priceIt2Type, gson);
        List<PriceIt2> forecast3YearNetProfit = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.forecast3YearNetProfit + sec.secId, priceIt2Type, gson);

        List<Integer> yearList = Lists.newArrayList(); //年度列表
        List<String> yearStrList  = Lists.newArrayList(); //年度列表, 字符串的

        List<String> yearTabList  = Lists.newArrayList(); //表格数据year
        String yearListJson = "[]";
        List<Double> epsList = Lists.newLinkedList();
        List<Double> epsTabList = Lists.newLinkedList();//表格eps
        String epsListJson = "[]";
        if(last5YearEps != null && last5YearEps.size() > 0){
            for (PriceIt2 p : last5YearEps) {
                yearList.add(p.yearVal);
                yearStrList.add(p.yearVal + "A");
                epsList.add(CommonUtils.scaleNum(p.price, 0, "#.00"));
            }

            //取近两年的值 用于表格
            if (last5YearEps.size() > 2) {
                for (int i = last5YearEps.size() - 2; i < last5YearEps.size(); i++) {
                    PriceIt2 p = last5YearEps.get(i);
                    yearTabList.add(p.yearVal + "A");
                    epsTabList.add(CommonUtils.scaleNum(p.price, 0, "#.00"));
                }
            }else{
                for (PriceIt2 p : last5YearEps) {
                    yearTabList.add(p.yearVal + "A");
                    epsTabList.add(CommonUtils.scaleNum(p.price, 0, "#.00"));
                }
            }

            if(forecast3YearEps != null && forecast3YearEps.size() > 0){
                for (PriceIt2 p : forecast3YearEps) {
                    yearList.add(p.yearVal);
                    yearStrList.add(p.yearVal + "E");
                    epsList.add(CommonUtils.scaleNum(p.price, 0, "#.00"));
                    yearTabList.add(p.yearVal + "E");
                    epsTabList.add(CommonUtils.scaleNum(p.price, 0, "#.00"));
                }
            }

            //补充到5个
            if (yearTabList.size() < 5) {
                int ysize = yearTabList.size();
                String lastStr = yearTabList.get(yearTabList.size() - 1);
                int lyear = Integer.valueOf(lastStr.substring(0, 4));
                for (int m = ysize; m < 5; m++) {
                    lyear += 1;
                    yearTabList.add(lyear + "E");
                    epsTabList.add(CommonUtils.scaleNum(0, 0, "#.00"));
                }
            }

            if (forecast3YearEps != null && forecast3YearEps.size() > 0) { //画图只画一年的预测值
                yearListJson = gson.toJson(yearList.subList(0, last5YearEps.size() + 1));
                epsListJson = gson.toJson(epsList.subList(0, last5YearEps.size() + 1));
            } else {
                yearListJson = gson.toJson(yearList);
                epsListJson = gson.toJson(epsList);
            }

        }

        List<Double> netProfitList = Lists.newLinkedList();
        List<Double> netTabProfitList = Lists.newLinkedList();//用于表格展示
        String netProfitListJson = "[]";
        if(last5YearNetProfit != null && last5YearNetProfit.size() > 0){
            for (PriceIt2 p : last5YearNetProfit) {
                netProfitList.add(CommonUtils.scaleNum(p.price, -8, "#.00"));
            }

            //取两年实际值
            if (last5YearNetProfit.size() > 2) {
                for (int i = last5YearNetProfit.size() - 2; i < last5YearNetProfit.size(); i++) {
                    PriceIt2 p = last5YearNetProfit.get(i);
                    netTabProfitList.add(CommonUtils.scaleNum(p.price, -8, "#.00"));
                }
            }else {
                for (PriceIt2 p : last5YearNetProfit) {
                    netTabProfitList.add(CommonUtils.scaleNum(p.price, -8, "#.00"));
                }
            }


            if (forecast3YearNetProfit != null && forecast3YearNetProfit.size() > 0) {
                for (PriceIt2 p : forecast3YearNetProfit) {
                    netProfitList.add(CommonUtils.scaleNum(p.price, -8, "#.00"));
                    netTabProfitList.add(CommonUtils.scaleNum(p.price, -8, "#.00"));
                }
            }

            //补充到5个
            if (netTabProfitList.size() < 5) {
                for (int i = netTabProfitList.size(); i < 5; i++) {
                    netTabProfitList.add(CommonUtils.scaleNum(0, -8, "#.00"));
                }
            }

            if (forecast3YearNetProfit != null && forecast3YearNetProfit.size() > 0) {//画图只画一年的预测值
                netProfitListJson = gson.toJson(netProfitList.subList(0, last5YearNetProfit.size() + 1));
            }else {
                netProfitListJson = gson.toJson(netProfitList);
            }
        }


        //预测明细
        Type fdType = new TypeToken<List<DetailByReport>>(){}.getType();
        List<DetailByReport> f3yearEpsDetail = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.f3yearEpsDetail + sec.secId, fdType, gson);
        List<DetailByReport> f3yearNetProfitDetail = RedisUtil.fetchFromRedis(RedisKey.PredictProfit.f3yearNetProfitDetail + sec.secId, fdType, gson);


        //用年度做为key, price做为value
        Map<String, Double> actualEpsMap = Maps.newLinkedHashMap();//eps跟年度的对应值
        int epsNewestYear = 0; //最新的eps年度
        if (last5YearEps != null && last5YearEps.size() > 0) {
            int size = last5YearEps.size();
            List<PriceIt2> list = null;
            if (size >= 2) {
                list = last5YearEps.subList(size - 2, size);
            } else {
                list = last5YearEps;
            }
            for (PriceIt2 p : list) {
                actualEpsMap.put(p.yearVal + "A", p.price);
            }
            epsNewestYear = last5YearEps.get(size - 1).yearVal;
        }

        Map<String, Double> actualNetProfitMap = Maps.newLinkedHashMap();//净利润跟年度的对应值
        int netProfitNewestYear = 0; //最新的净利润年度
        if (last5YearNetProfit != null && last5YearNetProfit.size() > 0) {
            int size = last5YearNetProfit.size();
            List<PriceIt2> list = null;
            if (size >= 2) {
                list = last5YearNetProfit.subList(size - 2, size);
            } else {
                list = last5YearNetProfit;
            }
            for (PriceIt2 p : list) {
                actualNetProfitMap.put(p.yearVal + "A", p.price);
            }

            netProfitNewestYear = last5YearNetProfit.get(size - 1).yearVal;
        }
        //根据reportDate降序 （resolve bug-8100 liuhj)
        if(f3yearEpsDetail!=null){
            Collections.sort(f3yearEpsDetail, new Comparator<DetailByReport>() {
                @Override
                public int compare(DetailByReport o1, DetailByReport o2) {
                    int compare = Double.compare(o2.reportDate.getTime(), o1.reportDate.getTime());
                    return compare;
                }
            });
        }
        if(f3yearNetProfitDetail!=null){
            Collections.sort(f3yearNetProfitDetail, new Comparator<DetailByReport>() {
                @Override
                public int compare(DetailByReport o1, DetailByReport o2) {
                    int compare = Double.compare(o2.reportDate.getTime(), o1.reportDate.getTime());
                    return compare;
                }
            });
        }

        render(sec,
                reportRating, reportRatingCharJson, ratingChangeList,
                targetPriceCharJson, actualPriceCharJson, forecastAvgPrice, newestPrice, rating,
                yearListJson, epsListJson, netProfitListJson, yearStrList, epsList, netProfitList,
                actualEpsMap, epsNewestYear, f3yearEpsDetail,
                actualNetProfitMap, netProfitNewestYear, f3yearNetProfitDetail,ratingChange,
                yearTabList,epsTabList,netTabProfitList);

    }
}
