package controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.financeana.*;
import org.apache.commons.lang.reflect.FieldUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 财务分析
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:47
 */
@With(value = {BaseInfoIntercept.class})
public class FinanceAnaCt extends Controller {

    public static void info(String scode) {
        Gson gson = CommonUtils.createGson();
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        //全景图
        FullView fv = RedisUtil.fetchFromRedis(RedisKey.FinanceAna.fullView + sec.institutionId, FullView.class, gson);
        //全景图的画图数据
        String fvItemStr = "['盈利能力', '经营能力', '投资收益', '短期偿债能力', '长期偿债能力', '成长能力']";
        String fvJsonData = "[]";
        if (fv != null) {
            fvJsonData = gson.toJson(new Object[]{fv.earnPowerCapacityStep, fv.operateCapacityStep, fv.roiCapacityStep, fv.shortDebtRepaymentStep, fv.longDebtRepaymentStep, fv.developmentCapacityStep});
        }

        //杜绑分析
        DupontVal dupontVal = RedisUtil.fetchFromRedis(RedisKey.FinanceAna.dupont + sec.institutionId, DupontVal.class, gson);
        if (dupontVal == null) {
            dupontVal = new DupontVal();
        }

        //主要财务指标
        //先取最新一期的时间, 以资产负债表的数据为准
        List<Integer> yearList = new ArrayList<Integer>(11); //年度list
        List<BalanceSheet> balanceList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.balanceSheet + sec.institutionId, new TypeToken<List<BalanceSheet>>() {
        }.getType(), gson);
        if (balanceList != null && balanceList.size() > 0) {
            int maxYear = Integer.parseInt(balanceList.get(0).enddateStr.substring(0, 4));
            for (int i = 0; i < 10; i++) {
                yearList.add(maxYear - i);
            }
        }

        render(fvItemStr, fvJsonData, fv,
                dupontVal,
                yearList,
                sec);
    }

    /**
     * 主要财务指标
     *
     * @param isFirstYear 是否是最新年度
     */
    public static void mainIndexDivContent(String scode, String year, boolean isFirstYear) {
        Gson gson = CommonUtils.createGson();
        BondSec sec = BondSec.fetchBondSecByCode(scode);

        List<DebtPayItem> debtPayList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.debtPay + sec.institutionId, new TypeToken<List<DebtPayItem>>() {
        }.getType(), gson);
        List<EarnPowerItem> earnPowerList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.earnPower + sec.institutionId, new TypeToken<List<EarnPowerItem>>() {
        }.getType(), gson);
        List<PerShareItem> perShareList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.perShare + sec.institutionId, new TypeToken<List<PerShareItem>>() {
        }.getType(), gson);
        List<LcDiscloseIndexItem> lcDiscloseIndexList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.lcDiscloseIndex + sec.institutionId, new TypeToken<List<LcDiscloseIndexItem>>() {
        }.getType(), gson);

        List<DebtPayItem> debtPaySubList = null;
        List<EarnPowerItem> earnPowerSubList = null;
        List<PerShareItem> perShareSubList = null;
        List<LcDiscloseIndexItem> lcDiscloseIndexSubList = null;

        if (isFirstYear) { //最新年度, 取最新的四期
            debtPaySubList = pre4(debtPayList);
            earnPowerSubList = pre4(earnPowerList);
            perShareSubList = pre4(perShareList);
            lcDiscloseIndexSubList = pre4(lcDiscloseIndexList);
        } else { //取当年的
            debtPaySubList = filterDataList(debtPayList, year);
            earnPowerSubList = filterDataList(earnPowerList, year);
            perShareSubList = filterDataList(perShareList, year);
            lcDiscloseIndexSubList = filterDataList(lcDiscloseIndexList, year);
        }

        List<String> debtPlayDateList = Lists.newArrayList(); //报告时间
        Map<String, DebtPayItem> debtPayMap = Maps.newHashMap();
        for (DebtPayItem it : debtPaySubList) {
            debtPayMap.put(it.enddateStr, it);
            if (!debtPlayDateList.contains(it.enddateStr)) { //不包含,则加入
                debtPlayDateList.add(it.enddateStr);
            }
        }

        List<String> earnPowerDateList = Lists.newArrayList(); //报告时间
        Map<String, EarnPowerItem> earnPowerMap = Maps.newHashMap();
        for (EarnPowerItem it : earnPowerSubList) {
            earnPowerMap.put(it.enddateStr, it);
            if (!earnPowerDateList.contains(it.enddateStr)) { //不包含,则加入
                earnPowerDateList.add(it.enddateStr);
            }
        }

        List<String> perShareDateList = Lists.newArrayList(); //报告时间
        Map<String, PerShareItem> perShareMap = Maps.newHashMap();
        for (PerShareItem it : perShareSubList) {
            perShareMap.put(it.enddateStr, it);
            if (!perShareDateList.contains(it.enddateStr)) { //不包含,则加入
                perShareDateList.add(it.enddateStr);
            }
        }

        Map<String, LcDiscloseIndexItem> lcDiscloseIndexMap = Maps.newHashMap();
        for (LcDiscloseIndexItem it : lcDiscloseIndexSubList) {
            lcDiscloseIndexMap.put(it.enddateStr, it);
        }

        render(debtPlayDateList, earnPowerDateList, perShareDateList, debtPayMap, earnPowerMap, perShareMap, lcDiscloseIndexMap);
    }

    //取前四
    @Util
    private static <T> List<T> pre4(List<T> dataList) {
        List<T> result = Lists.newArrayList();
        if (dataList != null) {
            if (dataList.size() > 4) {
                result = dataList.subList(0, 4);
            } else {
                result = dataList;
            }
        }
        return result;
    }

    @Util
    private static <T> List<T> filterDataList(List<T> dataList, String year) {
        List<T> result = Lists.newArrayList();
        if (dataList != null && dataList.size() > 0) {
            try {
                for (T o : dataList) {
                    String enddateStr = (String) FieldUtils.readField(o, "enddateStr");
                    if (enddateStr.startsWith(year)) {
                        result.add(o);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 财务简表
     */
    public static void financeDataDivByReportDate(String scode, String year, boolean isFirstYear, String type) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        List<String> dateList = Lists.newArrayList(); //报告时间
        Map<String, BalanceSheet> balanceMap = Maps.newHashMap();
        Map<String, CashFlowSheet> cashFlowMap = Maps.newHashMap();
        Map<String, IncomeSheet> incomeMap = Maps.newHashMap();
        if ("balanceSheet".equalsIgnoreCase(type)) {
            List<BalanceSheet> balanceList = null;
            List<BalanceSheet> balanceAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.balanceSheet + sec.institutionId, new TypeToken<List<BalanceSheet>>() {
            }.getType());
            if (balanceAllList != null) {
                if (isFirstYear) {//最新年度, 取最新的四期
                    balanceList = pre4(balanceAllList);
                } else { //取当年的
                    balanceList = filterDataList(balanceAllList, year);
                }
                for (BalanceSheet it : balanceList) {
                    if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                        dateList.add(it.enddateStr);
                    }
                }
                for (BalanceSheet it : balanceList) {
                    balanceMap.put(it.enddateStr, it);
                }
            }
        } else if ("cashFlowSheet".equalsIgnoreCase(type)) {
            List<CashFlowSheet> cashFlowList = null;
            List<CashFlowSheet> cashFlowAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.cashFlowSheet + sec.institutionId, new TypeToken<List<CashFlowSheet>>() {
            }.getType());
            if (cashFlowAllList != null) {
                if (isFirstYear) {
                    cashFlowList = pre4(cashFlowAllList);
                } else {
                    cashFlowList = filterDataList(cashFlowAllList, year);
                }
                for (CashFlowSheet it : cashFlowList) {
                    if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                        dateList.add(it.enddateStr);
                    }
                }
                for (CashFlowSheet it : cashFlowList) {
                    cashFlowMap.put(it.enddateStr, it);
                }
            }
        } else if ("incomeSheet".equalsIgnoreCase(type)) {
            List<IncomeSheet> incomeList = null;
            List<IncomeSheet> incomeAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.incomeSheet + sec.institutionId, new TypeToken<List<IncomeSheet>>() {
            }.getType());
            if (isFirstYear) {
                incomeList = pre4(incomeAllList);
            } else {
                incomeList = filterDataList(incomeAllList, year);
            }
            for (IncomeSheet it : incomeList) {
                if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                    dateList.add(it.enddateStr);
                }
            }
            for (IncomeSheet it : incomeList) {
                incomeMap.put(it.enddateStr, it);
            }
        }

        render("/FinanceAnaCt/financeDataDivContent.html", dateList, balanceMap, cashFlowMap, incomeMap);
    }

    @Util
    //取前4年的
    private static <T> List<T> pre4Year(List<T> dataList, String year) {
        int y = Integer.parseInt(year);
        List<T> result = Lists.newArrayList();
        if (dataList != null) {
            try {
                for (T t : dataList) {
                    String enddateStr = (String) FieldUtils.readField(t, "enddateStr");
                    int yearVal = Integer.parseInt(enddateStr.substring(0,4));
                    String monthDay = enddateStr.substring(4);
                    if("-12-31".equals(monthDay) && yearVal <= y && result.size() < 4){ //年报, 只取4年
                        result.add(t);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void financeDataDivByYear(String scode, String year, boolean isFirstYear, String type) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        List<String> dateList = Lists.newArrayList(); //报告时间
        Map<String, BalanceSheet> balanceMap = Maps.newHashMap();
        Map<String, CashFlowSheet> cashFlowMap = Maps.newHashMap();
        Map<String, IncomeSheet> incomeMap = Maps.newHashMap();
        if ("balanceSheet".equalsIgnoreCase(type)) {
            List<BalanceSheet> balanceList = null;
            List<BalanceSheet> balanceAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.balanceSheet + sec.institutionId, new TypeToken<List<BalanceSheet>>() {
            }.getType());
            if (balanceAllList != null) {
                balanceList = pre4Year(balanceAllList, year);
                for (BalanceSheet it : balanceList) {
                    if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                        dateList.add(it.enddateStr);
                    }
                }
                for (BalanceSheet it : balanceList) {
                    balanceMap.put(it.enddateStr, it);
                }
            }
        } else if ("cashFlowSheet".equalsIgnoreCase(type)) {
            List<CashFlowSheet> cashFlowList = null;
            List<CashFlowSheet> cashFlowAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.cashFlowSheet + sec.institutionId, new TypeToken<List<CashFlowSheet>>() {
            }.getType());
            if (cashFlowAllList != null) {
                cashFlowList = pre4Year(cashFlowAllList, year);
                for (CashFlowSheet it : cashFlowList) {
                    if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                        dateList.add(it.enddateStr);
                    }
                }
                for (CashFlowSheet it : cashFlowList) {
                    cashFlowMap.put(it.enddateStr, it);
                }
            }
        } else if ("incomeSheet".equalsIgnoreCase(type)) {
            List<IncomeSheet> incomeList = null;
            List<IncomeSheet> incomeAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.incomeSheet + sec.institutionId, new TypeToken<List<IncomeSheet>>() {
            }.getType());

            incomeList = pre4Year(incomeAllList, year);
            for (IncomeSheet it : incomeList) {
                if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                    dateList.add(it.enddateStr);
                }
            }
            for (IncomeSheet it : incomeList) {
                incomeMap.put(it.enddateStr, it);
            }
        }

        render("/FinanceAnaCt/financeDataDivContent.html", dateList, balanceMap, cashFlowMap, incomeMap);
    }

    public static void financeDataDivBySingle(String scode, String year, boolean isFirstYear, String type) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        List<String> dateList = Lists.newArrayList(); //报告时间
        Map<String, BalanceSheet> balanceMap = Maps.newHashMap();
        Map<String, CashFlowSheet> cashFlowMap = Maps.newHashMap();
        Map<String, IncomeSheet> incomeMap = Maps.newHashMap();
        if ("cashFlowSheet".equalsIgnoreCase(type)) {
            List<CashFlowSheet> cashFlowList = null;
            List<CashFlowSheet> cashFlowAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.cashFlowSheetSingle + sec.institutionId, new TypeToken<List<CashFlowSheet>>() {
            }.getType());
            if (cashFlowAllList != null) {
                if (isFirstYear) {
                    cashFlowList = pre4(cashFlowAllList);
                } else {
                    cashFlowList = filterDataList(cashFlowAllList, year);
                }
                for (CashFlowSheet it : cashFlowList) {
                    if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                        dateList.add(it.enddateStr);
                    }
                }
                for (CashFlowSheet it : cashFlowList) {
                    cashFlowMap.put(it.enddateStr, it);
                }
            }
        } else if ("incomeSheet".equalsIgnoreCase(type)) {
            List<IncomeSheet> incomeList = null;
            List<IncomeSheet> incomeAllList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.incomeSheetSingle + sec.institutionId, new TypeToken<List<IncomeSheet>>() {
            }.getType());
            if (isFirstYear) {
                incomeList = pre4(incomeAllList);
            } else {
                incomeList = filterDataList(incomeAllList, year);
            }
            for (IncomeSheet it : incomeList) {
                if (!dateList.contains(it.enddateStr)) { //不包含,则加入
                    dateList.add(it.enddateStr);
                }
            }
            for (IncomeSheet it : incomeList) {
                incomeMap.put(it.enddateStr, it);
            }
        }
        render("/FinanceAnaCt/financeDataDivContent.html", dateList, balanceMap, cashFlowMap, incomeMap);
    }

    //资产负债表附注
    public static void zcfzbfz(String scode, String year) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();
        List<AssetImpairment> assetImpairmentList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.assetImpairment + sec.institutionId, new TypeToken<List<AssetImpairment>>() {
        }.getType(), gson);
        List<EquityInvest> equityInvestList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.equityInvest + sec.institutionId, new TypeToken<List<EquityInvest>>(){}.getType(), gson);
        List<LongtermPrepaidFee> longtermPrepaidFeeList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.longtermPrepaidFee + sec.institutionId, new TypeToken<List<LongtermPrepaidFee>>(){}.getType(), gson);
        List<DeferredIncomeTax> deferredIncomeTaxList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.deferredIncomeTax + sec.institutionId, new TypeToken<List<DeferredIncomeTax>>(){}.getType(), gson);

        List<AssetImpairment> assetImpairmentSubList = filterDataList(assetImpairmentList, year);
        List<EquityInvest> equityInvestSubList = filterDataList(equityInvestList, year);
        List<LongtermPrepaidFee> longtermPrepaidFeeSubList = filterDataList(longtermPrepaidFeeList, year);
        List<DeferredIncomeTax> deferredIncomeTaxSubList = filterDataList(deferredIncomeTaxList, year);

        render(assetImpairmentSubList, equityInvestSubList, longtermPrepaidFeeSubList, deferredIncomeTaxSubList);
    }

    //利率表附注
    public static void llbfz(String scode, String year) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();

        List<OperateIncomeCosts> operateIncomeCostsList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.operateIncomeCosts + sec.institutionId, new TypeToken<List<OperateIncomeCosts>>(){}.getType(), gson);
        List<FinanceCosts> financeCostsList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.financeCosts + sec.institutionId, new TypeToken<List<FinanceCosts>>(){}.getType(), gson);
        List<BusinessTaxAppend> businessTaxAppendList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.businessTaxAppend + sec.institutionId, new TypeToken<List<BusinessTaxAppend>>(){}.getType(), gson);

        List<OperateIncomeCosts> operateIncomeCostsSubList = filterDataList(operateIncomeCostsList, year);
        List<FinanceCosts> financeCostsSubList = filterDataList(financeCostsList, year);
        List<BusinessTaxAppend> businessTaxAppendSubList = filterDataList(businessTaxAppendList, year);

        render(operateIncomeCostsSubList, financeCostsSubList, businessTaxAppendSubList);
    }

    public static void reportList(String scode, int reportType) {
        String preUrl = Play.configuration.getProperty("bulletinAttachPreUrl", "http://unknow.org/");
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        Type type =  new TypeToken<List<FinanceReportItem>>(){}.getType();
        List<FinanceReportItem> resultList = null;
        if(reportType == 1){
              resultList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.reportItem1 + secA.secId, type);
        }else if(reportType == 2){
            resultList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.reportItem2 + secA.secId, type);
        }else if (reportType == 3) {
            resultList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.reportItem3 + secA.secId, type);
        }else {
            resultList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.FinanceAna.reportItem4 + secA.secId, type);
        }

        render(preUrl, resultList);
    }
}