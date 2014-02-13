package bussiness;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dto.*;
import dto.newestinfo.CompanyBulletin;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.WS;
import util.CommonUtils;
import util.Page;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 用于新闻, 研报, 行业取股票相关信息
 * User: wenzhihong
 * Date: 13-3-29
 * Time: 下午6:14
 */
public class NewsSearchService {
    /**
     * 检索服务的前缀
     */
    public static final String SEARCH_SERVER_URL = Play.configuration.getProperty("search.url");

    public static Joiner commaJoin = Joiner.on(",").skipNulls();


    /**
     * 从es上取研报信息(公司研报). 包含 id, declareDate, title
     * @param pageNo
     * @param pageSize
     * @param secCodes
     * @return
     */
    public static F.T2<List<ReportDto>, Page> secReportFromEs(int pageNo, int pageSize, String... secCodes) {
        Param param = new Param();
        param.seType(SearchType.report);
        Date today = new Date();
        String pattern = "yyyy-MM-dd";
        param.startTime = DateFormatUtils.format(DateUtils.addYears(today, -1), pattern); //往前推一年
        param.endTime = DateFormatUtils.format(today, pattern);
        param.pageSize = pageSize;
        param.pageNo = pageNo;
        param.symbols = secCodes;

        Result result = fetchResult(param);
        if (result != null && result.data != null && result.data.size() > 0) {
            Iterator<ReportDto> itemIter = Iterators.transform(result.data.iterator(), new Function<DataItem, ReportDto>() {
                @Override
                public ReportDto apply(DataItem input) {
                    ReportDto item = new ReportDto();
                    item.reportId = input.repid;
                    item.declareDate = input.declaredate;
                    item.title = input.title;
                    item.symbol = Lists.newArrayList(input.symbols);
                    item.institutionName = input.repSourceNamsJoin();
                    return item;
                }
            });
            Page page = new Page(result.total, pageNo, pageSize);
            long startTime=System.currentTimeMillis();   //获取开始时间
            List<ReportDto> reportList = Lists.newArrayList(itemIter);
            long endTime=System.currentTimeMillis(); //获取结束时间
            System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
            return F.T2(reportList, page);
        } else {
            return null;
        }

    }

    /**
     * 从es上取行业新闻信息, 只包含新闻的id, 标题, 发布时间
     * @param pageNo
     * @param pageSize
     * @param secCodes
     * @return
     */
    public static F.T2<List<NewsInfoDto>,Page> secInduNewsFromEs(int pageNo, int pageSize, String... secCodes){
        Set<String> induSet = Sets.newHashSet();
        for (String code : secCodes) {
            String induCode = BondSec.fetchInduCodeByCode(code);
            if (StringUtils.isNotBlank(induCode)) {
                induSet.add(induCode.substring(0, 1));
            }
        }
        if (induSet.size() == 0) {
            return null;
        }

        //从MessageIndexService 借过来的代码, 实现高级检索
        AdvanceSearchDto asd = new AdvanceSearchDto();
        asd.itype = Lists.newArrayList("1"); //代表新闻
        Date today = new Date();
        String pattern = "yyyy-MM-dd";
        asd.startTime = DateFormatUtils.format(DateUtils.addYears(today, -1), pattern); //往前推一年
        asd.endTime = DateFormatUtils.format(today, pattern);
        asd.niids = Lists.newArrayList(induSet); //新闻行业分类
        asd.pageNo = pageNo;
        asd.pageSize = pageSize;

        Gson gson = CommonUtils.createIncludeNulls();
        String asdJson = asd.toJson();
        Logger.info("按股票查找行业新闻发到es上的参数:" + asdJson);

        JsonElement jsonElement = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
        Result result = fetchResult(jsonElement,CommonUtils.DATE_FORMAT_STR_ARR[1]);
        if (result != null && result.data != null && result.data.size() > 0) {
            Iterator<NewsInfoDto> itemIter = Iterators.transform(result.data.iterator(), new Function<DataItem, NewsInfoDto>() {
                @Override
                public NewsInfoDto apply(DataItem input) {
                    NewsInfoDto item = new NewsInfoDto();
                    item.declareDate = input.declaredate;
                    item.newsId = String.valueOf(input.newsid);
                    item.title = input.title;
                    item.newsSource = input.newSourceNams;
                    return item;
                }
            });

            Page page = new Page(result.total, pageNo, pageSize);
            List<NewsInfoDto> newsList = Lists.newArrayList(itemIter);
            return F.T2(newsList, page);
        } else {
            return null;
        }
    }

    /**
     * 从es里取股票的行业研报. 包含 id, declareDate, title
     * @param pageNo
     * @param pageSize
     * @param secCodes
     * @return  如果没有结果, 则返回null
     */
    public static F.T2<List<ReportDto>, Page> secInduReportFromEs(int pageNo, int pageSize, String... secCodes){
        Set<String> induSet = Sets.newHashSet();
        for (String code : secCodes) {
            String induCode = BondSec.fetchInduCodeByCode(code);
            if (StringUtils.isNotBlank(induCode)) {
                //这是制造业有二级分类
                /*if (induCode.startsWith("C")) {
                    induSet.add(induCode);
                } else {
                    induSet.add(induCode.substring(0, 1));
                }*/
                //只查一级分类
                induSet.add(induCode.substring(0, 1));
            }
        }
        if (induSet.size() == 0) {
            return null;
        }

        //从MessageIndexService 借过来的代码, 实现高级检索
        AdvanceSearchDto asd = new AdvanceSearchDto();
        asd.itype = Lists.newArrayList("3"); //代表研报
        Date today = new Date();
        String pattern = "yyyy-MM-dd";
        asd.startTime = DateFormatUtils.format(DateUtils.addYears(today, -1), pattern); //往前推一年
        asd.endTime = DateFormatUtils.format(today, pattern);
        asd.rcids = Lists.newArrayList(induSet); //分类
        asd.pageNo = pageNo;
        asd.pageSize = pageSize;

        Gson gson = CommonUtils.createIncludeNulls();
        String asdJson = asd.toJson();
        Logger.info("按股票查找行业研报发到es上的参数:" + asdJson);
        JsonElement jsonElement = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
        Result result = fetchResult(jsonElement,null);
        if (result != null && result.data != null && result.data.size() > 0) {
            Iterator<ReportDto> itemIter = Iterators.transform(result.data.iterator(), new Function<DataItem, ReportDto>() {
                @Override
                public ReportDto apply(DataItem input) {
                    ReportDto item = new ReportDto();
                    item.reportId = input.repid;
                    item.declareDate = input.declaredate;
                    item.title = input.title;
                    item.symbol = Lists.newArrayList(input.symbols);
                    item.institutionName = input.repSourceNamsJoin();
                    return item;
                }
            });

            Page page = new Page(result.total, pageNo, pageSize);
            List<ReportDto> reportList = Lists.newArrayList(itemIter);
            return F.T2(reportList, page);
        } else {
            return null;
        }
    }


    /**
     * 从es上取新闻信息, 只包含新闻的id, 标题, 发布时间
     * @param pageNo
     * @param pageSize
     * @param secCodes
     * @return
     */
    public static F.T2<List<NewsInfoDto>, Page> secNewsShortInfoFromEs(int pageNo, int pageSize, String... secCodes){
        Param param = new Param();
        param.seType(SearchType.news);
        Date today = new Date();
        String pattern = "yyyy-MM-dd";
        param.startTime = DateFormatUtils.format(DateUtils.addYears(today, -1), pattern); //往前推一年
        param.endTime = DateFormatUtils.format(today, pattern);
        param.pageSize = pageSize;
        param.pageNo = pageNo;
        param.symbols = secCodes;

        Result result = fetchResult(param);
        if (result != null && result.data != null && result.data.size() > 0) {
            Iterator<NewsInfoDto> itemIter = Iterators.transform(result.data.iterator(), new Function<DataItem, NewsInfoDto>() {
                @Override
                public NewsInfoDto apply(DataItem input) {
                    NewsInfoDto item = new NewsInfoDto();
                    item.declareDate = input.declaredate;
                    item.newsId = String.valueOf(input.newsid);
                    item.title = input.title;
                    item.newsSource = input.newSourceNams;;
                    return item;
                }
            });

            Page page = new Page(result.total, pageNo, pageSize);
            List<NewsInfoDto> newsList = Lists.newArrayList(itemIter);
            return F.T2(newsList, page);
        } else {
            return null;
        }
    }

    //股票公告
    public static F.T2<List<CompanyBulletin>, Page> secBulletinShortInfoFromEs(int pageNo, int pageSize, String... secCodes){
        Param param = new Param();
        param.seType(SearchType.bulletin);
        param.pageSize = pageSize;
        param.pageNo = pageNo;
        param.symbols = secCodes;
        Date today = new Date();
        String pattern = "yyyy-MM-dd";
        param.startTime = DateFormatUtils.format(DateUtils.addYears(today, -1), pattern); //往前推一年
        param.endTime = DateFormatUtils.format(today, pattern);

        Result result = fetchResult(param);
        if (result != null && result.data != null && result.data.size() > 0) {
            Iterator<CompanyBulletin> itemIter = Iterators.transform(result.data.iterator(), new Function<DataItem, CompanyBulletin>() {
                @Override
                public CompanyBulletin apply(DataItem input) {
                    CompanyBulletin item = new CompanyBulletin();
                    item.guid = String.valueOf(input.annid);
                    item.title = input.title;
                    item.pubDate = input.declaredate;
                    item.attachUrl = input.attach;
                    return item;
                }
            });

            Page page = new Page(result.total, pageNo, pageSize);
            List<CompanyBulletin> bulletinList = Lists.newArrayList(itemIter);
            return F.T2(bulletinList, page);
        } else {
            return null;
        }
    }

    public static Result fetchResult(Param param){
        Gson gson = CommonUtils.createIncludeNulls();
        String json = gson.toJson(param);
        Logger.info("公司研究报告,send data to es= %s", json);
        JsonElement jsonElement = WS.url(NewsSearchService.SEARCH_SERVER_URL).body(json).post().getJson();
        return fetchResult(jsonElement,CommonUtils.DATE_FORMAT_STR_ARR[1]);
    }
    //重载该获取方法以可选格式化时间。
    public static Result fetchResult(Param param, String fomatter){
        Gson gson = CommonUtils.createIncludeNulls();
        String json = gson.toJson(param);
        Logger.info("公司研究报告,send data to es= %s", json);
        JsonElement jsonElement = WS.url(NewsSearchService.SEARCH_SERVER_URL).body(json).post().getJson();
        return fetchResult(jsonElement,fomatter);
    }

    /**
     * 解析结果. 如果错误返回null
     * @param jsonElement json格式
     * @return
     */
    public static Result fetchResult(JsonElement jsonElement, String fommatter) {
        String datePattern = (fommatter == null)? CommonUtils.DATE_FORMAT_STR_ARR[0] : fommatter;
        Result result = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonPrimitive status = jsonObject.getAsJsonPrimitive("status");
            if (status != null && !status.isJsonNull() && status.getAsInt() == 0) {
                result = CommonUtils.createGsonIncludeNullCustDateformat(datePattern).fromJson(jsonObject, Result.class);
            }
        }

        return result;
    }

    public static class Result{
        public List<DataItem> data; //数据

        public int total; //总共条数

        public int pageSize;

        public int pageNo;

        public int status;
    }

    public static class DataItem{
        public long newsid;

        public long annid;

        public long repid;

        public String title;

        public String[] symbols;

        public int itype;

        /**
         * 研报来源机构名称
         */
        @Expose
        @SerializedName("rsnames")
        public List<String> repSourceNams;

        //新闻来源
        @SerializedName("nsource")
        public String newSourceNams;

        public String reportSourceName; //研报来源名称

        public String attach; //附件地址

        public Date updatedate; //更新时间

        public Date declaredate; //发布时间

        //研报来源名称
        public String repSourceNamsJoin() {
            if (repSourceNams == null || repSourceNams.size() == 0) {
                return null;
            }

            return commaJoin.join(repSourceNams);
        }
    }


    public static class Param {
        public String cmd = "symbolInfoSearch";

        public int pageNo = 1; //从1开始

        public int pageSize = 10;

        @Expose
        public String startTime = "";          //发布起始时间
        @Expose
        public String endTime = "";            //发布结束时间

        //股票代码
        public String[] symbols = new String[0];

        //类型1：新闻 2：公告  3： 研报
        public int[] itype = new int[0];

        public String sortField;

        public int sortType = 1;//0 升序 1降序

        public void seType(SearchType ... types){
            itype = new int[types.length];
            int i = 0;
            for (SearchType t : types) {
                itype[i++] = t.type;
            }
        }

        @Override
        public String toString() {
            return CommonUtils.createGson().toJson(this);
        }
    }

    //类型 0：全部  1：新闻 2：公告  3： 研报
    public static enum SearchType {
        all(0), news(1), bulletin(2), report(3);

        int type;
        SearchType(int type){
            this.type = type;
        }

        public static SearchType int2Type(int t) {
            SearchType type = all;
            switch (t){
                case 0:
                    type = all;
                    break;
                case 1:
                    type = news;
                    break;
                case 2:
                    type = bulletin;
                    break;
                case 3:
                    type = report;
                    break;
                default:
                    type = all;
                    break;
            }
            return type;
        }
    }


}
