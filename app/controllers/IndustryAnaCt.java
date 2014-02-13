package controllers;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.IndexInfo;
import dto.industryana.*;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import util.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 行业分析
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:47
 */
@With(value = {BaseInfoIntercept.class})
public class IndustryAnaCt extends Controller {
    protected static final String EXCEL_FILE_SUFFIX = "xls";

    public static void info(String scode) {
        Gson gson = CommonUtils.createGson();
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        //市场表现
        F.T3<String, SecMarketPerfor[], IndexInfo[]> t3 = lastedMarketPerfor(gson, sec);
        String marketPerforseriesDataJson = t3._1;
        SecMarketPerfor[] arr = t3._2;
        SecMarketPerfor marketPerfor = arr[0];
        SecMarketPerfor induPerfor = arr[1];
        SecMarketPerfor secPerfor = arr[2];
        IndexInfo marketIdxInfo = t3._3[0];
        IndexInfo induIdxInfo = t3._3[1];

        //公司规模
        F.T2<List<CompanyScale>, String> t2 = compareCompanyScale(gson, secA);
        List<CompanyScale> companyScaleList = t2._1;
        String companyScaleCharJson = t2._2;

        //估值水平
        F.T2<List<AppraisementRankItem>, String> appT2 = appraisement(gson, sec);
        List<AppraisementRankItem> appraisementList = appT2._1;
        String appraisementCharJson = appT2._2;

        //财务状况
        F.T2<List<SecEps>, String> finaT2 = financeStatus(gson, secA);
        List<SecEps> pre6SecEpsList = finaT2._1; //前6名
        String financeStatusCharJson = finaT2._2;
        List<String> zeroRow = Lists.newLinkedList();//第0行. 股票名称
        List<Double> firstRow = Lists.newLinkedList(); //第一行
        List<Double> secondRow = Lists.newLinkedList(); //第二行
        List<Integer> thirdRow = Lists.newLinkedList();//第三行(排名)
        Date[] fullDates = paddingLackData(pre6SecEpsList);
        Date firstDate = null;
        Date secondDate = null;
        if (fullDates != null) {
            for (SecEps s : pre6SecEpsList) {
                zeroRow.add(s.secName());
                firstRow.add(CommonUtils.scaleNum(s.detail.get(0).eps, 0, "0.0000")); //取4位小数
                secondRow.add(CommonUtils.scaleNum(s.detail.get(1).eps, 0, "0.0000"));
                thirdRow.add(s.rank);
            }
            firstDate = fullDates[0];
            secondDate = fullDates[1];
        }

        //准备财务状态的画图数据
        String financeXData = gson.toJson(zeroRow);
        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("name", CommonUtils.getFormatDate("yyyy-MM-dd", firstDate));
        data1.put("data", firstRow);
        Map<String, Object> data2 = new HashMap<String, Object>();
        data2.put("name", CommonUtils.getFormatDate("yyyy-MM-dd", secondDate));
        data2.put("data", secondRow);
        String financeSeriesData = gson.toJson(new Object[]{data1, data2});

        render(sec,
                marketPerforseriesDataJson, marketPerfor, induPerfor, secPerfor, marketIdxInfo, induIdxInfo,
                companyScaleList, companyScaleCharJson,
                appraisementList, appraisementCharJson,
                zeroRow, firstRow, secondRow, thirdRow, firstDate, secondDate, financeStatusCharJson, financeXData, financeSeriesData
        );
    }

    //填充缺少的数据(主要是少发报告的情况). 返回4个报告期时间
    private static Date[] paddingLackData(List<SecEps> secEpsList) {
        if (secEpsList != null && secEpsList.size() > 0) {
            Date lastDate = secEpsList.get(0).detail.get(0).endDate; //最新报告时间

            //完整的要比较的4个时间
            Date[] fullDateArr = new Date[]{lastDate, CommonUtils.calcReportDate2(lastDate, -1),
                    CommonUtils.calcReportDate2(lastDate, -2), CommonUtils.calcReportDate2(lastDate, -3)};
            for (SecEps s : secEpsList) {
                //先把数据给补上, 防止有中间没有发布报告的情况
                List<Date> curDates = Lists.newLinkedList(); //当前有的日期
                for (SecEps.Item i : s.detail) {
                    curDates.add(i.endDate);
                }
                //看日期是否在里面,不在的话, 补齐
                for (Date d : fullDateArr) {
                    if (!curDates.contains(d)) {
                        SecEps.Item newItem = new SecEps.Item();
                        newItem.endDate = d;
                        newItem.eps = 0; //这里先填个0值吧. 如果不在的话. 更好的话, 应该是null
                        s.addItem(newItem);
                    }
                }
                s.sortItems(); //新加了数据,就要重新排序一下

                if (s.detail.size() > 4) { //超过4项的
                    s.detail = s.detail.subList(0, 4);
                }
            }

            return fullDateArr;
        }

        return null;
    }

    /**
     * 公司规模更多
     */
    public static void moreCompanyScale(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        List<CompanyScale> companyScaleList = getMoreCompany(sec);
        render(companyScaleList, sec);
    }

    @Util
    private static List<CompanyScale> getMoreCompany(BondSec sec) {
        Gson gson = CommonUtils.createGson();
        List<CompanyScale> companyScaleList = Lists.newLinkedList();
        String induLevel2Code = BondSec.institutionIdToInduCode.get(sec.institutionId); //二级行业代码
        for (Long institutionId : BondSec.induLev2CodeToSecIdMultimap.get(induLevel2Code)) { //取出这个二级行业相关的公司
            CompanyScale item = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.company_sec_scale + institutionId, CompanyScale.class, gson);
            if (item != null) {
                companyScaleList.add(item);
            }
        }
        Collections.sort(companyScaleList, new Comparator<CompanyScale>() {
            @Override
            public int compare(CompanyScale o1, CompanyScale o2) {
                int compare = Doubles.compare(o2.totalValue, o1.totalValue);
                if (compare == 0) {
                    compare = Ints.compare(o1.rank, o2.rank);
                }
                return compare;
            }
        });
        return companyScaleList;
    }

    public static void exportMoreCompany(String scode) throws Exception {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        List<CompanyScale> companyScaleList = getMoreCompany(sec);
        renderArgs.put("numberFormat", NumbericFomate.instance);
        renderArgs.put("companyScaleList", companyScaleList);
        renderArgs.put(RenderExcel.RA_FILENAME, "同行业公司规模比较.xls");
        request.format = EXCEL_FILE_SUFFIX;
        render();
    }

    @Util
    private static void getMoreAppraisement(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        long secId = sec.secId;
        Gson gson = CommonUtils.createGson();
        ComparatorT comparatorT = new ComparatorT();
        String yearVal = "";
        AppraisementFullItem current = null; //当前股票的
        AppraisementFullIndu fullIndu = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.appr_full_indu + BondSec.fetchInduLevel2CodeByCode(scode), AppraisementFullIndu.class, gson);
        Collections.sort(fullIndu.list, comparatorT);
        if (fullIndu != null) {
            int pos = 0;
            int rank = 1;
            for (AppraisementFullItem it : fullIndu.list) {//排名
                it.rank = rank;
                rank++;
            }
            for (AppraisementFullItem it : fullIndu.list) {
                if ( secId == it.secId ) { //找到
                    current = it;
                    break;
                }
                pos++;
            }
            if (current != null) {
                yearVal = current.yearStr();
                fullIndu.list.remove(pos); //移除掉当前的
            }

        }

        renderArgs.put("yearVal", yearVal);
        renderArgs.put("fullIndu", fullIndu);
        renderArgs.put("current", current);
        renderArgs.put("sec", sec);
    }

    /**
     * 估值水平更多
     */
    public static void moreAppraisement(String scode) {
        getMoreAppraisement(scode);
        render();
    }

    public static void exportMoreAppraisement(String scode) {
        getMoreAppraisement(scode);
        renderArgs.put(RenderExcel.RA_FILENAME, "行业估值分析.xls");
        renderArgs.put("numberFormat", NumbericFomate.instance);
        request.format = EXCEL_FILE_SUFFIX;
        render();
    }


    private static class ComparatorT implements Comparator<AppraisementFullItem> {
        @Override
        public int compare(AppraisementFullItem o1, AppraisementFullItem o2) {
            //首先比较市盈率(11A)，如果市盈率(11A)相同，则比较市盈率(TTM)
            return ComparisonChain.start()
                    .compare(o1.pe1a, o2.pe1a)
                    .compare(o1.pe1ttm, o2.pe1ttm)
                    .result();
        }
    }

    @Util
    private static void getMoreFinance(String scode) {
        Gson gson = CommonUtils.createGson();
        List<SecEps> financeList = Lists.newLinkedList();
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        String induLevel2Code = BondSec.institutionIdToInduCode.get(sec.institutionId); //二级行业代码
        for (Long institutionId : BondSec.induLev2CodeToSecIdMultimap.get(induLevel2Code)) { //取出这个二级行业相关的公司
            SecEps item = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.financeStatus_sec + institutionId, SecEps.class, gson);
            if (item != null) {
                financeList.add(item);
            }
        }

        //排序一下
        Collections.sort(financeList, new Comparator<SecEps>() {
            @Override
            public int compare(SecEps o1, SecEps o2) {
                return Ints.compare(o1.rank, o2.rank);
            }
        });

        //填充数据
        //数据填充要在排序的后面 不然数据位置有可能错乱@bug——liuhj
        Date[] fullDate = paddingLackData(financeList);

        renderArgs.put("fullDate", Arrays.asList(fullDate));//要变成list结构 jsxl foreach才能遍历
        renderArgs.put("financeList", financeList);
        renderArgs.put("sec", sec);
    }

    //更多财务分析
    public static void moreFinance(String scode) {
        getMoreFinance(scode);
        render();
    }

    public static void exportMorefinance(String scode) {
        getMoreFinance(scode);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put("dateFormat", dateFormat);
        renderArgs.put("numberFormat", NumbericFomate.instance);
        renderArgs.put(RenderExcel.RA_FILENAME, "同行业财务状况比较.xls");
        request.format = EXCEL_FILE_SUFFIX;
        render();
    }


    //财务分析
    private static F.T2<List<SecEps>, String> financeStatus(Gson gson, BondSec sec) {
        String charJson = "{'xAxis':[], 'series':[]}"; //画图的json数据
        List<SecEps> secEpsList = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.financeStatus + BondSec.fetchInduLevel2CodeByCode(sec.code),
                new TypeToken<List<SecEps>>() {
                }.getType(), gson);
        SecEps secEps = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.financeStatus_sec + sec.institutionId, SecEps.class, gson);
        List<SecEps> distData = null;
        if (secEpsList != null ) {
            distData = Lists.newLinkedList();
            boolean isInPre6 = false;
            for (SecEps eps : secEpsList) {
                if (sec.institutionId == eps.institutionId) {
                    isInPre6 = true;
                    break;
                }
            }

            if (isInPre6) {
                distData = secEpsList;
            } else {
                if (secEpsList.size() > 5) {
                    if (secEps != null) {
                        distData.addAll(secEpsList.subList(0, 5));
                        distData.add(secEps);
                    } else {
                        distData.addAll(secEpsList.subList(0, 6));
                    }
                } else {
                    distData.addAll(secEpsList);
                }

            }
            List<String> xAxisList = Lists.newLinkedList();
            List<Double> seriesList = Lists.newLinkedList();

            for (SecEps s : distData) {
                xAxisList.add(s.secName());
                if (s.detail.size() > 0) {
                    seriesList.add(s.detail.get(0).eps);
                } else {
                    seriesList.add(null);
                }
            }

            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("xAxis", xAxisList);
            dataMap.put("series", seriesList);

            charJson = gson.toJson(dataMap);
        }

        return F.T2(distData, charJson);
    }

    //估值水平
    private static F.T2<List<AppraisementRankItem>, String> appraisement(Gson gson, BondSec sec) {
        String charJson = "{'xAxis':[], 'series':[]}"; //画图的json数据

        List<AppraisementRankItem> appraisementList = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.appraisement + BondSec.fetchInduLevel2CodeByCode(sec.code),
                new TypeToken<List<AppraisementRankItem>>() {
                }.getType(), gson);

        AppraisementRankItem appraisementItem = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.appraisement_sec + sec.secId, AppraisementRankItem.class, gson);
        List<AppraisementRankItem> distData = null;

        if (appraisementList != null ) {
            distData = Lists.newLinkedList(); //最终数据
            boolean isInPre6 = false;
            for (AppraisementRankItem s : appraisementList) {
                if (sec.secId == s.secId) {
                    isInPre6 = true;
                    break;
                }
            }

            if (isInPre6) {
                distData = appraisementList;
            } else {
                if (appraisementList.size() > 5) {
                    if (appraisementItem != null) {
                        distData.addAll(appraisementList.subList(0, 5));
                        distData.add(appraisementItem);
                    } else {
                        distData.addAll(appraisementList.subList(0, 6));
                    }
                } else {
                    distData.addAll(appraisementList);
                }

            }

            List<String> xAxisList = Lists.newLinkedList();
            List<Double> seriesList = Lists.newLinkedList();

            for (AppraisementRankItem s : distData) {
                xAxisList.add(s.secName());
                seriesList.add(s.pe1a);
            }

            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("xAxis", xAxisList);
            dataMap.put("series", seriesList);

            charJson = gson.toJson(dataMap);
        }

        return F.T2(distData, charJson);
    }

    //公司规模
    private static F.T2<List<CompanyScale>, String> compareCompanyScale(Gson gson, BondSec sec) {
        String charJson = "{'xAxis':[], 'series':[]}"; //画图的json数据

        List<CompanyScale> companyScaleList = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.companyscale + BondSec.fetchInduLevel2CodeByCode(sec.code),
                new TypeToken<List<CompanyScale>>() {
                }.getType(), gson);

        CompanyScale companyScale = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.company_sec_scale + sec.institutionId, CompanyScale.class, gson);
        List<CompanyScale> distData = null;

        if (companyScaleList != null && companyScale != null) {
            distData = Lists.newLinkedList(); //最终数据
            boolean isInPre6 = false;
            for (CompanyScale s : companyScaleList) {
                if (sec.institutionId == s.institutionId) {
                    isInPre6 = true;
                    break;
                }
            }

            if (isInPre6) {
                distData = companyScaleList;
            } else {
                if (companyScaleList.size() > 5) {
                    distData.addAll(companyScaleList.subList(0, 5));
                } else {
                    distData.addAll(companyScaleList);
                }
                distData.add(companyScale);
            }

            List<String> xAxisList = Lists.newLinkedList();
            List<Double> seriesList = Lists.newLinkedList();

            for (CompanyScale s : distData) {
                xAxisList.add(s.secName());
                seriesList.add(CommonUtils.scaleNum(s.totalValue, -8, "0.00"));
            }

            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("xAxis", xAxisList);
            dataMap.put("series", seriesList);

            charJson = gson.toJson(dataMap);
        }


        return F.T2(distData, charJson);
    }

    //最新市场表现
    @Util
    private static F.T3<String, SecMarketPerfor[], IndexInfo[]> lastedMarketPerfor(Gson gson, BondSec sec) {
        String marketPerforseriesDataJson = "[]";
        //市场表现数据的json格式. 数据格式为:
        /*
        [
            {name:'上证指数',data: [ [Date.UTC(2010, 0, 1), 29.9], [Date.UTC(2010, 0, 2), 71.5], [Date.UTC(2010, 0, 3), 106.4]]},
            {name:'平安银行',data: [ [Date.UTC(2010, 0, 1), 39.9], [Date.UTC(2010, 0, 2), null], [Date.UTC(2010, 0, 3), 306.4]]}
        ]
        */
        //市场指数的
        IndexInfo marketIdxInfo = IndexInfo.secCode2MarketIdx(sec.code);
        if (marketIdxInfo == null) { //如果没有的话, 就认为是 深证成份指数
            marketIdxInfo = IndexInfo.codeMap.get(IndexInfo.INDEX_SZ);
        }
        SecMarketPerfor marketPerfor = null;
        if (marketIdxInfo != null) {
            marketPerfor = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.idx_lastedMarketPerfor + marketIdxInfo.secId, SecMarketPerfor.class, gson);
        }

        //行业指数的
        IndexInfo induIdxInfo = IndexInfo.secCode2InduIdx(sec.code);
        SecMarketPerfor induPerfor = null;
        if (induIdxInfo != null) {
            induPerfor = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.idx_lastedMarketPerfor + induIdxInfo.secId, SecMarketPerfor.class, gson);
        }

        //股票的
        SecMarketPerfor secPerfor = RedisUtil.fetchFromRedis(RedisKey.IndustryAna.sec_lastedMarketPerfor + sec.secId, SecMarketPerfor.class, gson);

        if (marketPerfor != null && marketPerfor.details.size() > 0) {
            Date sdate = marketPerfor.details.get(0).tradingDate;//开始日期
            Date edate = marketPerfor.details.get(marketPerfor.details.size() - 1).tradingDate; //结束日期

            //处理市场
            Map<String, Object> marketMap = Maps.newHashMap();
            marketMap.put("name", marketIdxInfo.name);
            List<Object[]> marketDatas = Lists.newArrayListWithCapacity(marketPerfor.details.size());
            for (SecMarketPerfor.Item i : marketPerfor.details) {
                marketDatas.add(new Object[]{new HighChartDataType.HCDate(i.tradingDate), CommonUtils.scaleNum(i.returnDaily1, 0, "#.00")});
            }
            marketMap.put("data", marketDatas);

            //处理行业
            Map<String, Object> induMap = Maps.newHashMap();
            if (induIdxInfo != null) {
                induMap = fillData(induIdxInfo.name, induPerfor, 0);
            }

            //处理股票
            Map<String, Object> secMap = fillData(sec.name, secPerfor, 2);
            if(induMap.size()==0){//当induMap没有填充数组时 忽悠它，防止页面绘图出错 （修护BUG 2013-07-01 liuhj)
                marketPerforseriesDataJson = CommonUtils.toJsonWithHighChartDataType(new Object[]{marketMap, secMap});
            }else{
                marketPerforseriesDataJson = CommonUtils.toJsonWithHighChartDataType(new Object[]{marketMap, induMap, secMap});
            }
        }

        return F.T3(marketPerforseriesDataJson,
                new SecMarketPerfor[]{marketPerfor, induPerfor, secPerfor},
                new IndexInfo[]{marketIdxInfo, induIdxInfo});
    }

    //填充数据
    @Util
    private static Map<String, Object> fillData(String cnName, SecMarketPerfor perfor, int scale) {
        Map<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("name", cnName);
        List<Object[]> induDatas = Lists.newLinkedList();
        if (perfor != null && perfor.details.size() > 0) {
            for (SecMarketPerfor.Item i : perfor.details) {
                induDatas.add(new Object[]{new HighChartDataType.HCDate(i.tradingDate), CommonUtils.scaleNum(i.returnDaily1, scale, "#.00")});
            }
        }

        dataMap.put("data", induDatas);

        return dataMap;
    }


}
