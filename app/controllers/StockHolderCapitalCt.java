package controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.stockholdercapital.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.lang.reflect.Type;
import java.util.*;


/**
 * 股东股本 控制器
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:43
 */
@With(value = {BaseInfoIntercept.class})
public class StockHolderCapitalCt extends Controller{
    public static void info(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();

        Type HolderListType = new TypeToken<List<StockHolderDto>>() {}.getType();
        //10大股东
        List<StockHolderDto> holderTop10List = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.stockHolderTop10 + sec.institutionId, HolderListType, gson);
        String holderLastReportDate = null;//上一期时间
        String holderCurReportDate = null; //本期时间
        long holdNumSum = 0;
        double holdRateSum = 0;
        if(holderTop10List != null && holderTop10List.size() > 0){
            holderLastReportDate = holderTop10List.get(0).secondDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", holderTop10List.get(0).secondDate);
            holderCurReportDate = holderTop10List.get(0).firstDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", holderTop10List.get(0).firstDate);
            for (StockHolderDto dto : holderTop10List) {
                holdNumSum += dto.holdNum != null ? dto.holdNum.longValue() : 0;
                holdRateSum += dto.rate != null ? dto.rate : 0;
            }
        }
        //10大流通股东
        List<StockHolderDto> flowHolderTop10List = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.stockFlowHolderTop10 + sec.institutionId, HolderListType, gson);
        String flowHolderLastReportDate = null;//上一期时间
        String flowHolderCurReportDate = null; //本期时间
        long flowHoldNumSum = 0;
        double flowHoldRateSum = 0;
        if(flowHolderTop10List != null && flowHolderTop10List.size() > 0){
            flowHolderLastReportDate = flowHolderTop10List.get(0).secondDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", flowHolderTop10List.get(0).secondDate);
            flowHolderCurReportDate = flowHolderTop10List.get(0).firstDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", flowHolderTop10List.get(0).firstDate);
            //过滤掉跟10大股东相同的股东, TODO 这里先注释掉, 感觉有点扯淡
            //flowHolderTop10List = filtterTheSame10Hold(flowHolderTop10List, holderTop10List);

            for (StockHolderDto dto : flowHolderTop10List) {
                flowHoldNumSum += dto.holdNum != null ? dto.holdNum.longValue() : 0;
                flowHoldRateSum += dto.rate != null ? dto.rate : 0;
            }
        }

        //机构主力持股统计. 画图
        F.T2<String, String> t2 = stockOrgGroupSumHolder(gson, sec);
        String barData = t2._1;
        String picData = t2._2;

        //机构股东明细
        List<StockHolderDto> orgHolderList = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.orgStockHolder + sec.institutionId, HolderListType, gson);
        List<StockHolderDto> orgFlowHolderList = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.orgStockFlowHolder + sec.institutionId, HolderListType, gson);
        String orgHolderLastReportDate = null;//上一期时间
        String orgHolderCurReportDate = null; //本期时间
        List<StockHolderDto> togetherOrgHoldList = Lists.newArrayList(); //合并起来的
        if(orgHolderList != null && orgHolderList.size() > 0){
            orgHolderLastReportDate = orgHolderList.get(0).secondDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd",orgHolderList.get(0).secondDate );
            orgHolderCurReportDate= orgHolderList.get(0).firstDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd",orgHolderList.get(0).firstDate );
        }else if(orgFlowHolderList != null && orgFlowHolderList.size() > 0 ){
            orgHolderLastReportDate= orgFlowHolderList.get(0).secondDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", orgFlowHolderList.get(0).secondDate);
            orgHolderCurReportDate= orgFlowHolderList.get(0).firstDate == null ? "" : CommonUtils.getFormatDate("yyyy-MM-dd", orgFlowHolderList.get(0).firstDate);
        }
        if(orgHolderList == null){
            orgHolderList = Lists.newArrayList();
        }
        if(orgFlowHolderList == null){
            orgFlowHolderList = Lists.newArrayList();
        }
        togetherOrgHoldList.addAll(orgHolderList);
        togetherOrgHoldList.addAll(orgFlowHolderList);

        //股本结构
        CapitalStructure capitalStructure = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.capitalStructure + sec.institutionId, CapitalStructure.class, gson);
        Map<String,Object> structureDataMap = Maps.newHashMap();
        if(capitalStructure == null){
            structureDataMap.put("tradableSharesA", 0);
            structureDataMap.put("lockSharesTotal", 0);
        }else{
            structureDataMap.put("tradableSharesA", capitalStructure.tradableSharesARateForPic());
            structureDataMap.put("lockSharesTotal", capitalStructure.lockSharesTotalRateForPic());
        }
        String structureData = gson.toJson(structureDataMap);

        //限售解禁
        List<LimitedAndLift> limitedAndLiftList  = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.limitAndLift + sec.institutionId,
                new TypeToken<List<LimitedAndLift>>() {}.getType(), gson);

        render(sec,
                holderTop10List, holderLastReportDate, holderCurReportDate, holdNumSum, holdRateSum,
                flowHolderTop10List, flowHolderLastReportDate, flowHolderCurReportDate, flowHoldNumSum, flowHoldRateSum,
                barData, picData,
                togetherOrgHoldList, orgHolderLastReportDate, orgHolderCurReportDate,
                capitalStructure, structureData,
                limitedAndLiftList);

    }

    /**
     * @param flowHolderTop10List 10大流通股东
     * @param holderTop10List 10大股东
     * @return
     */
    private static List<StockHolderDto> filtterTheSame10Hold(List<StockHolderDto> flowHolderTop10List, List<StockHolderDto> holderTop10List) {
        List<StockHolderDto> fitterResult = Lists.newArrayList();
        for (StockHolderDto fst : flowHolderTop10List) {
            boolean isFind = false;
            for (StockHolderDto s : holderTop10List) {
                if(fst.holderId != 0 && s.holderId != 0) {
                    if (fst.holderId == s.holderId) {
                        isFind = true;
                        break;
                    }
                }else if(fst.holderName != null && fst.holderName.equals(s.holderName)){
                    isFind = true;
                    break;
                }
            }
            if(!isFind){
                fitterResult.add(fst);
            }
        }
        return fitterResult;
    }

    /**
     * _1 为柱状图json数据, _2为饼图json数据
     */
    private static F.T2<String, String> stockOrgGroupSumHolder(Gson gson, BondSec sec) {
        List<OrgGroupSumHolder> holderList = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.stockOrgGroupSumHolder + sec.institutionId,
                new TypeToken<List<OrgGroupSumHolder>>(){}.getType());
        Set<Integer> counterSet = Sets.newLinkedHashSet();
        for(int i=0;i<holderList.size();i++){//结果集中 同一机构类型在同一时间段有多个合计比例的话，取最大的那个。（处理BUG-liuhj ）
            for(int j=0;j<holderList.size();j++){
                if((holderList.get(i).endDate+holderList.get(i).typeId).equals((holderList.get(j).endDate+holderList.get(j).typeId))){
                    if(i!=j){
                        if(holderList.get(i).sumRatio<=holderList.get(j).sumRatio){
                            holderList.get(i).sumRatio = 0;
                                continue;
                         }
                    }
                }

            }
        }
        for(int index :counterSet){
            holderList.get(index).sumRatio=0;
        }
        //这里的 holderList 是按endDate desc 排列的
        if(holderList != null && holderList.size() > 0){
            Map<Date, Collection<BigOrgSumHolder>> bigOrgSumHolderMap = Maps.newLinkedHashMap();
            Date preEndDate = CommonUtils.parseDate("1910-10-10"); //考考你对历史了解不, 知道这是一个什么日期吗? hehe
            Map<String, BigOrgSumHolder> ratesSumMap = null;
            for (OrgGroupSumHolder holder : holderList) {
                if(preEndDate.equals(holder.endDate) == false){ //新的一个报告期
                    ratesSumMap = initRatesSumMap();
                    bigOrgSumHolderMap.put(holder.endDate, ratesSumMap.values());
                }

                //查看一下, 属于哪一类. 设置到那一类的总和里去
                for (String typeGroupName : OrgGroupSumHolder.typeGroupMap.keySet()) {
                    if(ArrayUtils.contains(OrgGroupSumHolder.typeGroupMap.get(typeGroupName), holder.typeId)){
                        ratesSumMap.get(typeGroupName).sumRatio += holder.sumRatio;
                        ratesSumMap.get(typeGroupName).orgCount += holder.orgCount;
                        ratesSumMap.get(typeGroupName).endDate = holder.endDate;
                    }
                }

                preEndDate = holder.endDate;
            }

            Map<String, Object> barData = calcBarData(bigOrgSumHolderMap);
            Map<String, Object> picData = calcPicData(holderList, bigOrgSumHolderMap);

            return F.T2(gson.toJson(barData), gson.toJson(picData));
        }else{
            //返回空的对象
            return F.T2("{'xAxis':[], 'series':[]}", "{'seriesData':[]}");
        }
    }
    //饼图数据
    @Util
    private static Map<String, Object> calcPicData(List<OrgGroupSumHolder> holderList, Map<Date, Collection<BigOrgSumHolder>> bigOrgSumHolderMap) {
        //最新一期的数据
        List<Map<String, Object>> picData = Lists.newArrayList();
        Collection<BigOrgSumHolder> lastReportData = bigOrgSumHolderMap.get(holderList.get(0).endDate);
        F.T2<Double, Integer> t2 = null;
        double counter = 0.00;
        Map<String, Object> fundCompany = new HashMap<String, Object>();
        fundCompany.put("name", "基金公司");
        t2 = calcOrgSum(lastReportData, "fundCompany");
        fundCompany.put("y", t2._1);
        fundCompany.put("num", t2._2);
        picData.add(fundCompany);
        counter+=t2._1;

        Map<String, Object> qfII = new HashMap<String, Object>();
        qfII.put("name", "QFII");
        t2 = calcOrgSum(lastReportData, "qfii");
        qfII.put("y", t2._1);
        qfII.put("num", t2._2);
        picData.add(qfII);
        counter+=t2._1;

        Map<String, Object> insuranceCompany = new HashMap<String, Object>();
        insuranceCompany.put("name", "保险公司");
        t2 = calcOrgSum(lastReportData, "insuranceCompany");
        insuranceCompany.put("y", t2._1);
        insuranceCompany.put("num", t2._2);
        picData.add(insuranceCompany);
        counter+=t2._1;

        Map<String, Object> extra = new HashMap<String, Object>();
        t2 = calcOrgSum(lastReportData, "extra");
        double extraY = t2._1;
        double extraNum = t2._2;
        counter+=t2._1;

        Map<String, Object> otherOrgs = new HashMap<String, Object>();
        otherOrgs.put("name", "其它");
        t2 = calcOrgSum(lastReportData, "otherOrgs");
        otherOrgs.put("y", t2._1+extraY);
        otherOrgs.put("num", t2._2+extraNum);
        picData.add(otherOrgs);
        counter+=t2._1;

        Map<String, Object> bondCompany = new HashMap<String, Object>();
        bondCompany.put("name", "证券公司");
        t2 = calcOrgSum(lastReportData, "bondCompany");
        bondCompany.put("y", t2._1);
        bondCompany.put("num", t2._2);
        picData.add(bondCompany);
        counter+=t2._1;

        Map<String, Object> socialSecurityFund = new HashMap<String, Object>();
        socialSecurityFund.put("name", "社保基金");
        t2 = calcOrgSum(lastReportData, "socialSecurityFund");
        socialSecurityFund.put("y", t2._1);
        socialSecurityFund.put("num", t2._2);
        picData.add(socialSecurityFund);
        counter+=t2._1;

        Map<String, Object> bondFinancing = new HashMap<String, Object>();
        bondFinancing.put("name", "券商集合理财");
        t2 = calcOrgSum(lastReportData, "bondFinancing");
        bondFinancing.put("y", t2._1);
        bondFinancing.put("num", t2._2);
        picData.add(bondFinancing);
        counter+=t2._1;

        Map<String, Object> noOrgs = new HashMap<String, Object>();
        noOrgs.put("name", "非机构股东");
        noOrgs.put("y", (100-counter));
        noOrgs.put("num", 0);
        picData.add(noOrgs);

        Map<String, Object> data = Maps.newHashMap();
        data.put("seriesData", picData);
        return data;
    }

    //生成画柱状图的数据
    @Util
    private static Map<String,Object> calcBarData(Map<Date, Collection<BigOrgSumHolder>> bigOrgSumHolderMap) {
        //生成柱状图数据
        Map<String, Object> fund = new HashMap<String, Object>();
        fund.put("name", "基金");
        fund.put("data", new ArrayList<Double>());

        Map<String, Object> qfII = new HashMap<String, Object>();
        qfII.put("name", "QFII");
        qfII.put("data", new ArrayList<Double>());

        Map<String, Object> insurance = new HashMap<String, Object>();
        insurance.put("name", "保险");
        insurance.put("data", new ArrayList<Double>());

        Map<String, Object> other = new HashMap<String, Object>();
        other.put("name", "其它");
        other.put("data", new ArrayList<Double>());

        List<Date> dateList = new ArrayList<Date>(bigOrgSumHolderMap.keySet());
        Collections.sort(dateList); //原先是把过来的
        List<String> xAxis = new ArrayList<String>(dateList.size());
        for (Date d : dateList) {
            Collection<BigOrgSumHolder> sumHolders = bigOrgSumHolderMap.get(d);
            F.T2<Double, Integer> t2 = calcOrgSum(sumHolders, "fundCompany","extra","socialSecurityFund");
            ((ArrayList<Double>)fund.get("data")).add(t2._1);

            t2 = calcOrgSum(sumHolders, "qfii");
            ((ArrayList<Double>)qfII.get("data")).add(t2._1);

            t2 = calcOrgSum(sumHolders, "insuranceCompany");
            ((ArrayList<Double>)insurance.get("data")).add(t2._1);

            t2 = calcOrgSum(sumHolders, "bondCompany", "bondFinancing", "otherOrgs");
            ((ArrayList<Double>)other.get("data")).add(t2._1);

            xAxis.add(DateFormatUtils.format(d, "yyyy-MM"));
        }

        Map<String,Object> barData = new HashMap<String, Object>();
        barData.put("xAxis", xAxis);
        barData.put("series", new Object[]{fund, qfII, insurance, other});

        return barData;
    }

    /**
     * 按机构类型计算
     */
    @Util
    private static F.T2<Double, Integer> calcOrgSum(Collection<BigOrgSumHolder> bigOrgSumHolders, String... typeIds){
        double sumRate = 0.0;
        int orgCout = 0;
        for (BigOrgSumHolder holder : bigOrgSumHolders) {
            for (String id : typeIds) {
                if(holder.name.equals(id)){
                    sumRate += holder.sumRatio;
                    orgCout += holder.orgCount;
                }
            }
        }
        return F.T2(sumRate, orgCout);
    }

    //初始化一个存放大的机构分类合计的map
    @Util
    private static Map<String, BigOrgSumHolder> initRatesSumMap() {
        Map<String, BigOrgSumHolder> ratesSumMap = Maps.newHashMap() ;
        for (String typeGroupName : OrgGroupSumHolder.typeGroupMap.keySet()) {
            BigOrgSumHolder sumHolder = new BigOrgSumHolder();
            sumHolder.name = typeGroupName;
            sumHolder.sumRatio = 0.0;
            sumHolder.orgCount = 0;
            ratesSumMap.put(typeGroupName, sumHolder);
        }
        return ratesSumMap;
    }

}
