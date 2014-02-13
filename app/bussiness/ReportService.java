package bussiness;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dbutils.CustomDbUtil;
import dbutils.ExtractDbUtil;
import dbutils.MemDbUtil;
import dbutils.SqlLoader;
import dto.*;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import util.CommonUtils;
import util.Page;

import java.lang.reflect.Type;
import java.util.*;

import static util.GuavaF.*;

/**
 * 研报列表
 * User: panzhiwei
 * Date: 13-1-17
 * Time: 下午3:09
 * To change this template use File | Settings | File Templates.
 */
public class ReportService {

    /**
     * 获取研报详情信息
     *
     * @param reportId 研报id
     * @return 研报DTO
     */
    public static ReportDetailDto getDetail(long reportId) {
        String sql = SqlLoader.getSqlById("reportDetail");
        return ExtractDbUtil.queryExtractDBSingleBean(sql, ReportDetailDto.class, reportId);
    }

    public static F.T4<List<ReportDto>, Page, ReportParameter, Long> reportListFromES(ReportParameter sp, int pageNo, boolean isExport) {
        final int pageSize = 21; //每页条数
        String df = "yyyyMMddHHmmss";
        //String df = "yyyy-MM-dd";
        Date maxDate = MessageIndexService.getMaxDateFromES("3");
        AdvanceSearchDto asd = new AdvanceSearchDto();
        asd.itype = Lists.newArrayList("3");
        asd.endTime = DateFormatUtils.format(maxDate, df);
        asd.startTime = DateFormatUtils.format(DateUtils.addDays(maxDate, -1), df);
        asd.pageNo = pageNo;
        asd.pageSize = pageSize;

        if (StringUtils.isNotBlank(sp.classCode)) { //对应于左边菜单的研报分类
            asd.rcids = Lists.newArrayList(sp.classCode);
        }else if (StringUtils.isNotBlank(sp.childrenCode)) { //对应于左边菜单的一级行业的行业研报检索
            asd.rcids = Lists.newArrayList(sp.childrenCode);
        }else if (StringUtils.isNotBlank(sp.keywords)) { //对于于关键字检索
            asd.keyword = sp.keywords;
        }else if (sp.id != null) {
            UserTemplate userTemplate = ReportService.findReportOrderById(sp.id);
            ReportOrderCnd cnd = new Gson().fromJson(userTemplate.content, new TypeToken<ReportOrderCnd>() {
            }.getType());
            String[] reportClassify = cnd.reportClassify;
            String[] reportOrg = cnd.reportOrg;
            String[] plateTree = cnd.plateTree;
            String[] symbolArr = cnd.symbolArr;

            List<String> reportOrgList = Lists.newArrayList(); //研报机构列表, 把 包含 || 字符的也转化了
            List<String> industryList = Lists.newArrayList();  //研报行业分类列表
            List<String> classifyList = Lists.newArrayList();  //研报分类列表
            List<String> plateTreeList = Lists.newArrayList(); //板块id列表
            List<String> symbolList = Lists.newArrayList();    //股票列表

            if (reportClassify != null && reportClassify.length > 0) {
                for (String s : reportClassify) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        if (s.length() == 1 || (s.length() == 2 && s.startsWith("C"))) { //长度为1则认为是行业分类 或者是长度为2且以C开头. 这个以后可能会发生变化时, 将是个错误
                            industryList.add(s);
                        } else { //其它认为是分类
                            classifyList.add(s);
                        }
                    }
                }

                classifyList.remove("P3203"); //去掉行业分类, 因为没有这个分类. 这里先硬编码.
            }
            //把研报分类跟一级行业分类 都加上. 因为在es里不区分这两种
            List<String> rcidList = Lists.newArrayList();
            rcidList.addAll(classifyList);
            rcidList.addAll(industryList);
            asd.rcids =  rcidList;

            if (reportOrg != null && reportOrg.length > 0) {
                Splitter splitter = Splitter.on("||");
                for (String s : reportOrg) {
                    if (StringUtils.isNotBlank(s)) {
                        s  = s.trim();
                        reportOrgList.addAll(Lists.newArrayList(splitter.split(s)));
                    }
                }
            }
            asd.rsource = reportOrgList;

            if (plateTree != null && plateTree.length > 0) {
                for (String s : plateTree) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        plateTreeList.add(s);
                    }
                }
            }

            if (symbolArr != null && symbolArr.length > 0) {
                for (String s : symbolArr) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        symbolList.add(s);
                    }
                }
            }

            if (plateTreeList.size() > 0 || symbolList.size() > 0) {
                List<String> symbolFromList = ReportService.findSecurityByPlateId(plateTree);
                symbolList.addAll(symbolFromList);

                if ( symbolList.size() > 0 ) {
                    asd.symbols = symbolList;
                }else{
                    Logger.info("[研报] 有选择了板块树, 但是从板块树上找不到相应的股票代码, 这种情况的话, 就不相应查询出信息来");
                    Page page = new Page(0, pageNo);
                    return F.T4(null, page, sp, 0L);
                }
            }

        }else { //没有其它条件, 也就是什么条件都不选
            //这里是什么条件都不选, 所以就不要设置条件了, 因为共有的条件前面都设置过了
        }

        if (isExport) { //导出, 则只要前面的1000条
            asd.pageNo = 1;
            asd.pageSize = 1000;
        }

        Gson gson = CommonUtils.createIncludeNulls();
        String asdJson = asd.toJson();
        Logger.info("研报搜索,send data to es= %s", asdJson);
        JsonElement result = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
        Logger.debug("es返回内容=%s", result.toString());
        JsonObject jsonObject = result.getAsJsonObject();
        int total = jsonObject.getAsJsonPrimitive("total").getAsInt();
        Logger.info("当前页=%d, es返回总条数=%d", pageNo, total);
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<MessageIndexDto>>() {
        }.getType();
        List<MessageIndexDto> messageIndexDtoList = gson.fromJson(messageData, type);

        Iterator<ReportDto> iter = Iterators.transform(messageIndexDtoList.iterator(), new Function<MessageIndexDto, ReportDto>() {
            @Override
            public ReportDto apply(MessageIndexDto input) {
                Long id = Long.valueOf(input.repId);
                ReportDto item = new ReportDto();
                item.reportId = id;
                item.declareDate = input.declaredate;
                item.title = input.title;
                item.institutionName = input.repSourceNamsJoin();
                item.researcherName = input.repAuthorsJoin();
                return item;
            }
        });

        Page page = new Page(total, pageNo, pageSize);
        List<ReportDto> list = Lists.newArrayList(iter);
        return F.T4(list, page, sp, new Long(total));

    }

    /**
     * 研报列表
     * @param sp     研报查询参数
     * @param pageNo  当前页码. 从1开始
     * @param isExport 是否导出
     * @return
     */
    public static F.T4<List<ReportDto>, Page, ReportParameter, Long> reportList(ReportParameter sp, int pageNo, boolean isExport) {
        final int pageSize = 30;
        String sb = "";
        if (StringUtils.isNotBlank(sp.classCode)) { //对应于左边菜单的研报分类
            sb = SqlLoader.getSqlById("reportWithClassifyAnd24hour").replaceAll("#classifyCode#", sp.classCode);
        } else if (StringUtils.isNotBlank(sp.childrenCode)) { //对应于左边菜单的一级行业的行业研报检索
            sb = SqlLoader.getSqlById("reportWithInduCodeAnd24hour").replaceAll("#induCode#", sp.childrenCode);
        } else if (StringUtils.isNotBlank(sp.keywords)) { //对于于关键字检索
            sb = SqlLoader.getSqlById("reportWithKeyWordAnd24hour").replaceAll("#keyword#", sp.keywords);
        } else if (sp.id != null) {
            sb = SqlLoader.getSqlById("reportList");
            UserTemplate userTemplate = ReportService.findReportOrderById(sp.id);
            ReportOrderCnd cnd = new Gson().fromJson(userTemplate.content, new TypeToken<ReportOrderCnd>() {
            }.getType());
            String[] reportClassify = cnd.reportClassify;
            String[] reportOrg = cnd.reportOrg;
            String[] plateTree = cnd.plateTree;
            String[] symbolArr = cnd.symbolArr;

            List<String> reportOrgList = Lists.newArrayList(); //研报机构列表, 把 包含 || 字符的也转化了
            List<String> industryList = Lists.newArrayList();  //研报行业分类列表
            List<String> classifyList = Lists.newArrayList();  //研报分类列表
            List<String> plateTreeList = Lists.newArrayList(); //板块id列表
            List<String> symbolList = Lists.newArrayList();    //股票列表

            String repIndustryTableSql = ""; //行业研报分类table的join
            String repCategoryTableSql = ""; //研报分类table的join
            String repSecurityTableSql = ""; //公司研报table的join

            if (reportClassify != null && reportClassify.length > 0) {
                for (String s : reportClassify) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        if (s.length() == 1 || (s.length() == 2 && s.startsWith("C"))) { //长度为1则认为是行业分类 或者是长度为2且以C开头. 这个以后可能会发生变化时, 将是个错误
                            industryList.add(s);
                        } else { //其它认为是分类
                            classifyList.add(s);
                        }
                    }
                }

                classifyList.remove("P3203"); //去掉行业分类, 因为没有这个分类. 这里先硬编码.

                if (industryList.size() > 0) {
                    repIndustryTableSql = "\n LEFT JOIN qic_db.c_rep_industry_lst e ON a.REPORTID = e.REPORTID \n";
                }
                if (classifyList.size() > 0) {
                    repCategoryTableSql = "\n LEFT JOIN qic_db.c_rep_category_lst c ON a.REPORTID = c.REPORTID \n";
                }
            }

            if (reportOrg != null && reportOrg.length > 0) {
                Splitter splitter = Splitter.on("||");
                for (String s : reportOrg) {
                    if (StringUtils.isNotBlank(s)) {
                        s  = s.trim();
                        reportOrgList.addAll(Lists.newArrayList(splitter.split(s)));
                    }
                }
            }


            if (plateTree != null && plateTree.length > 0) {
                for (String s : plateTree) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        plateTreeList.add(s);
                    }
                }
            }

            if (symbolArr != null && symbolArr.length > 0) {
                for (String s : symbolArr) {
                    if (StringUtils.isNotBlank(s)) {
                        s = s.trim();
                        symbolList.add(s);
                    }
                }
            }

            if (plateTreeList.size() > 0 || symbolList.size() > 0) {
                repSecurityTableSql = "\n LEFT JOIN qic_db.c_rep_security_lst d  ON a.REPORTID = d.REPORTID \n";
            }


            //where 条件
            String genCnd = "\n where a.DECLAREDATE >= DATE_ADD((SELECT MAX(DECLAREDATE) FROM c_rep_reportinfo_lst), INTERVAL - 1 DAY) ";//一般条件
            String reportOrgCnd = ""; //研报发布机构条件
            String industryCnd = ""; //行业分类条件
            String symbolCnd = ""; //股票代码条件
            if (reportOrgList.size() > 0) {
                reportOrgCnd = "\n and b.INSTITUTIONID in (" + commaJoin.join(reportOrgList) + ") ";
            }

            if(industryList.size() > 0 || classifyList.size() > 0 ){
                Iterator<String> induCndIter = Iterators.transform(industryList.iterator(), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "\n e.INDUSTRYCODE like '" + input + "%' ";
                    }
                });
                String tmpInduCnd = orJoin.join(induCndIter);

                Iterator<String> categoryIter = Iterators.transform(classifyList.iterator(), sqlSingleQuote);
                String tmpCategoryCnd = commaJoin.join(categoryIter);

                industryCnd = "\n and (\n " + tmpInduCnd;
                if (StringUtils.isNotBlank(tmpCategoryCnd)) {
                    if (StringUtils.isNotBlank(tmpInduCnd) ) {
                        industryCnd += "\n or c.CATEGORYCODE in (" + tmpCategoryCnd + ") \n ";
                    }else {
                        industryCnd += "\n c.CATEGORYCODE in (" + tmpCategoryCnd + ") \n ";
                    }
                }
                industryCnd += ") ";
            }

            if (plateTreeList.size() > 0 || symbolList.size() > 0) {
                List<String> symbolFromList = ReportService.findSecurityByPlateId(plateTree);
                symbolList.addAll(symbolFromList);
                Iterator<String> codeCndIter = Iterators.transform(symbolList.iterator(), sqlSingleQuote);
                String tmpCodeCnd = commaJoin.join(codeCndIter);

                if (StringUtils.isNotBlank(tmpCodeCnd)) {
                    symbolCnd = "\n and d.symbol in (" + tmpCodeCnd + ") ";
                }else{
                    Logger.info("[研报] 有选择了板块树, 但是从板块树上找不到相应的股票代码, 这种情况的话, 就不相应查询出信息来");
                    Page page = new Page(0, pageNo);
                    return F.T4(null, page, sp, 0L);
                }

            }

            sb += repIndustryTableSql + repCategoryTableSql + repSecurityTableSql + "\n"
                    + genCnd + reportOrgCnd + industryCnd + symbolCnd + "\n";

            sb += " GROUP BY a.REPORTID ORDER BY a.DECLAREDATE desc ";
        } else{ //没有其它附加条件
            sb = SqlLoader.getSqlById("reportWithoutConditionWith24hour");
        }

        StringBuilder coutSql = new StringBuilder("select count(*) from (\n" + sb + "\n) distTable  \n");
        Long total = CustomDbUtil.queryCount(coutSql.toString());
        Page page = new Page(total.intValue(), pageNo, pageSize);
        if ((pageNo - 1) * page.pageSize >= total) {
            return F.T4(null, page, sp, total);
        }
        if (isExport) {
            sb +=  "\n limit 0,1000 \n";
        } else {
            sb += "\n limit " + page.beginIndex + "," + page.pageSize + "\n";
        }
        List<ReportDto> reportList = CustomDbUtil.queryCustomDBBeanList(sb, ReportDto.class);
        return F.T4(reportList, page, sp, total);
    }

    /**
     * 根据ID获取到研报机构名称
     * @param id
     * @return
     */
    public static String fetchInstitutionNameById(Long id){
        String sql = SqlLoader.getSqlById("fetchInstitutionNameById");
        String institutionName = CustomDbUtil.queryOneFieldOneRow(sql,String.class,id);
        return institutionName;
    }



    /**
     * 根据id查找订阅信息
     *
     * @param id
     * @return
     */
    public static UserTemplate findReportOrderById(Long id) {
        String sql = SqlLoader.getSqlById("findReportById");
        UserTemplate userTemplate = CustomDbUtil.queryCustomDBSingleBean(sql, UserTemplate.class, id);
        return userTemplate;
    }


    //板块数据
    /* public static List<PlateTreeDto> getPlateTree() {
        String sql = SqlLoader.getSqlById("getPlateTree");
        List<PlateTreeDto> plateTreeDtoList = CustomDbUtil.queryCustomDBBeanList(sql,PlateTreeDto.class);
        return plateTreeDtoList;
    }*/

    /**
     * 根据板块树id查找子节点
     *
     * @param id
     * @return
     */
    public static List<PlateTreeDto> getPlateTreeByPlateTreeId(String id) {
        String sql = SqlLoader.getSqlById("getPlateTree");
        List<PlateTreeDto> plateTreeDtoList = ExtractDbUtil.queryExtractDBBeanList(sql, PlateTreeDto.class, id);
        return plateTreeDtoList;
    }

    /**
     * 根据板块id得到股票代码list
     *
     * @param bankCodeArr
     * @return
     */
    public static List<String> findSecurityByPlateId(String[] bankCodeArr) {
        Set<String> codeSet = Sets.newHashSet();
        for (String bankCode : bankCodeArr) {
            if (StringUtils.isNotBlank(bankCode)) {
                String sql = SqlLoader.getSqlById("getSymbolByPlateId");
                sql = sql.replaceAll("#typeid#", bankCode);
                List<String> codeList  = ExtractDbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>());
                if (codeList.size() == 0) {
                    Logger.warn("板块id[%s]下面没有所属股票代码. 从gta_data.Plate_StockChangelatest查得是这种情况, 可能是数据问题", bankCode);
                }
                codeSet.addAll(codeList);
            }
        }
        return Lists.newArrayList(codeSet.iterator());
    }

    /**
     * 根据证券代码或简称自动查找股票
     *
     * @param name_startsWith
     * @return
     */
    public static List<Map<String, Object>> findStockAutoComplete(String name_startsWith) {
        String sql = SqlLoader.getSqlById("findStockByName_startsWith");
        String upperCase =  name_startsWith.toUpperCase();
        List<Map<String, Object>> mapList = MemDbUtil.queryMapList(sql, "%" + name_startsWith + "%", "%" + name_startsWith + "%","%"+upperCase+"%");
        return mapList;
    }

    /**
     * 根据股票代码查股票
     *
     * @param symbolArr
     * @return
     */
    public static List<Map<String, Object>> findStockBySymbol(String[] symbolArr) {
        String sql = SqlLoader.getSqlById("findStockBySymbol");
        sql += "(";
        for (String s : symbolArr) {
            sql += "?" + ",";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += ")";
        List<Map<String, Object>> mapList = ExtractDbUtil.queryExtractDBMapList(sql, symbolArr);
        return mapList;
    }

    /**
     * 根据板块id得到板块名称
     *
     * @param reportPlateTreeArr
     * @return
     */
    public static List<PlateTreeDto> getPlateTreeName(String[] reportPlateTreeArr) {
        String sql = SqlLoader.getSqlById("getPlateTreeName");
        sql += "(";
        for (String s : reportPlateTreeArr) {
            sql += "?" + ",";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += ")";
        List<PlateTreeDto> plateTreeDtoList = ExtractDbUtil.queryExtractDBBeanList(sql, PlateTreeDto.class, reportPlateTreeArr);
        return plateTreeDtoList;
    }


}