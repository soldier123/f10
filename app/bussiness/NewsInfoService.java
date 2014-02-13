package bussiness;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dbutils.CustomDbUtil;
import dbutils.ExtractDbUtil;
import dbutils.SqlLoader;
import dto.*;
import dto.newestinfo.GreatInventRemind;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.F;
import play.libs.WS;
import util.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 每日头条service类
 * User: liangbing
 * Date: 13-1-17
 * Time: 下午5:29
 */
public class NewsInfoService {
    public static String key = RedisKey.News.recently_news;

    public static final String NEWS_INFO_KEY = "news_";
    public static final String NEWS_INFO_EXPIRATION = "20mn";
    public static final int NEWS_PAGE_SIZE = 40; //每页条数

    /**
     * @param newsId 新闻ID
     * @return 新闻类容
     */
    public static NewsInfoDto getNewsInfo(String newsId) {
        NewsInfoDto newsInfo = Cache.get(NEWS_INFO_KEY + newsId, NewsInfoDto.class);
        if (newsInfo == null) {
            String sql = SqlLoader.getSqlById("newsInfoDetail");
            newsInfo = ExtractDbUtil.queryExtractDBSingleBean(sql, NewsInfoDto.class, newsId);
            if (newsInfo != null) {
                Cache.set(NEWS_INFO_KEY + newsInfo.newsId, newsInfo, NEWS_INFO_EXPIRATION);
            }
        }

        return newsInfo;
    }

    /**
     * 根据股票获取大事提醒信息。
     * @param pageNo
     * @param pageSize
     * @param secCodes
     * @return
     */
    public static F.T2<List<GreatInventRemind>,Page> fetchSecRemindList(int pageNo, int pageSize, String... secCodes){
        String sql = SqlLoader.getSqlById("greatInventReminds");
        Joiner joiner = Joiner.on(',').skipNulls();
        String secIdGroup = joiner.join(secCodes);
        sql = sql.replaceAll("#secIdGroup#", secIdGroup);
        String coutSql = "select count(*) from (\n" + sql + "\n) distTable  \n";
        Logger.info(coutSql);
        Long total = ExtractDbUtil.queryCount(coutSql);
        Page page = new Page(total.intValue(), pageNo, pageSize);
        sql += " limit " + page.beginIndex + "," + page.pageSize + "\n";
        List<GreatInventRemind> lists = ExtractDbUtil.queryExtractDBBeanList(sql,GreatInventRemind.class);
        return F.T2(lists,page);
    }

    /**
     * 今日头条关键字搜索 从ES里搜索得到
     *
     * @param content 关键字
     * @param pageNo  这个的pageno是从1开始的
     * @return
     */
    public static F.T2<List<NewsInfoDto>, Page> searchFromEs(String content, int pageNo) {
        String df = "yyyyMMddHHmmss";
        Date maxDate = MessageIndexService.getMaxDateFromES("1"); //新闻
        AdvanceSearchDto asd = new AdvanceSearchDto();
        asd.itype = Lists.newArrayList("1"); //新闻
        asd.endTime = DateFormatUtils.format(maxDate, df);
        asd.startTime = DateFormatUtils.format(DateUtils.addDays(maxDate, -1), df);
        asd.pageNo = pageNo;
        asd.pageSize = NEWS_PAGE_SIZE;
        if (StringUtils.isNotBlank(content)) {
            asd.keyword = content;
        }
        return send2Es(pageNo, NEWS_PAGE_SIZE, asd);
    }


    /**
     * 今日头条关键字搜索 从数据库里搜索得到
     *
     * @param content 关键字
     * @param pageNo
     * @return
     */
    public static F.T2<List<NewsInfoDto>, Page> search(String content, int pageNo) {
        int pageSize = 30;
        Page page = null;
        String sql = SqlLoader.getSqlById("newsListSql");
        if (StringUtils.isNotBlank(content)) {
            sql += " AND a.TITLE LIKE '%" + content + "%' ";
        }
        sql += " ORDER BY a.`DECLAREDATE` DESC ";
        StringBuilder coutSql = new StringBuilder("select count(*) from (\n" + sql + "\n) distTable  \n");
        Long total = CustomDbUtil.queryCount(coutSql.toString());
        if ( (pageNo - 1) * pageSize > total) {
            List<NewsInfoDto> list = new ArrayList<NewsInfoDto>();
            return F.T2(list, page);
        }

        page = new Page(total.intValue(), pageNo, pageSize);
        sql += " limit " + page.beginIndex + "," + page.pageSize + "\n";
        List<NewsInfoDto> newsList = CustomDbUtil.queryCustomDBBeanList(sql, NewsInfoDto.class);
        return F.T2(newsList, page);
    }

    /**
     * 按订阅条件id搜索新闻
     * @param id
     * @param pageNo 这里的pageno从1开始
     * @return
     */
    public static F.T2<List<NewsInfoDto>, Page> orderSelectFromEs(long id, int pageNo, UserComposeInfo userComposeInfo) {
        //得到模板内容
        String content = TemplateService.fetchUserTemplateContentById(id, userComposeInfo);
        if (StringUtils.isNotBlank(content)) {
            Gson gson = CommonUtils.createGson();
            ToDayNewsSearchCnd cnd = gson.fromJson(content, ToDayNewsSearchCnd.class);
            List<String> realNewsClassifyList = Lists.newArrayList();
            if (cnd.newsClass != null && cnd.newsClass.length > 0) {
                realNewsClassifyList = NewsHelper.getRealNewsClassifyIdByCode(Lists.newArrayList(cnd.newsClass));
            }
            String df = "yyyyMMddHHmmss";
            Date maxDate = MessageIndexService.getMaxDateFromES("1"); //新闻
            AdvanceSearchDto asd = new AdvanceSearchDto();
            asd.itype = Lists.newArrayList("1"); //新闻
            asd.endTime = DateFormatUtils.format(maxDate, df);
            asd.startTime = DateFormatUtils.format(DateUtils.addDays(maxDate, -1), df);
            asd.pageNo = pageNo;
            asd.pageSize = NEWS_PAGE_SIZE;
            asd.ncids = realNewsClassifyList; //新闻分类
            List<String> newsSourceList = Lists.newArrayList();
            if (cnd.source != null && cnd.source.length > 0) {
                newsSourceList = Lists.newArrayList(cnd.source);
            }
            asd.nsource = newsSourceList;

            return send2Es(pageNo, NEWS_PAGE_SIZE, asd);
        } else {
            Page page = new Page(0, pageNo, NEWS_PAGE_SIZE);
            List<NewsInfoDto> list = Lists.newArrayList(); //没有相应对象
            return F.T2(list, page);
        }
    }

    /**
     * 发送到es进行检索
     * @param pageNo
     * @param pageSize
     * @param asd
     * @return
     */
    private static F.T2<List<NewsInfoDto>, Page> send2Es(int pageNo, int pageSize, AdvanceSearchDto asd) {
        String asdJson = asd.toJson();
        Logger.info("新闻搜索,send data to es= %s", asdJson);
        JsonElement result = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
        Logger.info("es返回内容=%s", result.toString());
        JsonObject jsonObject = result.getAsJsonObject();
        int total = jsonObject.getAsJsonPrimitive("total").getAsInt();
        Logger.info("当前页=%d, es返回总条数=%d", pageNo, total);
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<MessageIndexDto>>() {
        }.getType();

        Gson gson2 = CommonUtils.createGsonIncludeNullCustDateformat("yyyy-MM-dd HH:mm:ss");
        List<MessageIndexDto> messageIndexDtoList = gson2.fromJson(messageData, type);

        Iterator<NewsInfoDto> iter = Iterators.transform(messageIndexDtoList.iterator(), new Function<MessageIndexDto, NewsInfoDto>() {
            @Override
            public NewsInfoDto apply(MessageIndexDto input) {
                NewsInfoDto item = new NewsInfoDto();
                item.newsId = input.newsId;
                item.title = input.title;
                item.declareDate = input.declaredate;
                item.newsSource = input.nSource;
                return item;
            }
        });

        Page page = new Page(total, pageNo, pageSize);
        List<NewsInfoDto> list = Lists.newArrayList(iter);

        return F.T2(list, page);
    }


    /**
     * 订阅搜索
     *
     * @param newsClass 分类
     * @param source    来源
     * @param pageNo
     * @return
     */
    public static F.T2<List<NewsInfoDto>, Page> orderSelect(String newsClass, String source, int pageNo) {
        Page page = null;
        int pageSize = 30;
        String sql = SqlLoader.getSqlById("orderSelectNews");
        if (StringUtils.isNotBlank(newsClass) && !StringUtils.isNotBlank(source)) {
            sql += " INNER JOIN qic_db.c_news_classify_lst b ON a.NEWSID = b.NEWSID INNER JOIN gta_data.`news_classify_parameters` c ON c.`CLASSIFYID` = b.`CLASSIFYID` where a.DECLAREDATE >= DATE_ADD((SELECT MAX(DECLAREDATE)  FROM qic_db.c_news_newsinfo_lst), INTERVAL - 1 DAY) ";
            sql += " AND b.`CLASSIFYID` IN " + newsClass + " ";
        }
        if (StringUtils.isNotBlank(source) && !StringUtils.isNotBlank(newsClass)) {
            sql += " where a.DECLAREDATE >= DATE_ADD((SELECT MAX(DECLAREDATE)  FROM qic_db.c_news_newsinfo_lst), INTERVAL - 1 DAY) ";
            sql += " AND a.`NEWSSOURCE` IN " + source + " ";
        }
        if (StringUtils.isNotBlank(newsClass) && StringUtils.isNotBlank(source)) {
            sql += " INNER JOIN qic_db.c_news_classify_lst b ON a.NEWSID = b.NEWSID INNER JOIN gta_data.`news_classify_parameters` c ON c.`CLASSIFYID` = b.`CLASSIFYID` where a.DECLAREDATE >= DATE_ADD((SELECT MAX(DECLAREDATE)  FROM qic_db.c_news_newsinfo_lst), INTERVAL - 1 DAY)";
            sql += " AND b.`CLASSIFYID` IN " + newsClass + " AND a.`NEWSSOURCE` IN" + source + "";
        }
        if (!StringUtils.isNotBlank(newsClass) && !StringUtils.isNotBlank(source)) {
            sql += " where a.DECLAREDATE >= DATE_ADD((SELECT MAX(DECLAREDATE)  FROM qic_db.c_news_newsinfo_lst), INTERVAL - 1 DAY) ";
        }
        sql += " ORDER BY a.`DECLAREDATE` DESC ";
        StringBuilder coutSql = new StringBuilder("select count(*) from (\n" + sql + "\n) distTable  \n");
        Long total = CustomDbUtil.queryCount(coutSql.toString());
        if ((pageNo - 1) * pageSize > total) {
            List<NewsInfoDto> list = new ArrayList<NewsInfoDto>();
            return F.T2(list, page);
        }

        page = new Page(total.intValue(), pageNo, pageSize);
        sql += " limit " + page.beginIndex + "," + page.pageSize + "\n";
        List<NewsInfoDto> newsList = CustomDbUtil.queryCustomDBBeanList(sql, NewsInfoDto.class);
        return F.T2(newsList, page);
    }

    /**
     * 获取附件路径
     *
     * @param newId
     * @return
     */
    public static List<NewsAccessoryDto> getNewAccessList(String newId) {
        String sql = SqlLoader.getSqlById("newsAccessSql");
        List<NewsAccessoryDto> nadlist = new ArrayList<NewsAccessoryDto>();
        List<NewsAccessoryDto> newsAccessoryDtos = ExtractDbUtil.queryExtractDBBeanList(sql, NewsAccessoryDto.class, newId);
        if (newsAccessoryDtos != null && newsAccessoryDtos.size() > 0) {
            for (NewsAccessoryDto newsAccessoryDto : newsAccessoryDtos) {
                if (newsAccessoryDto.accessOryroute.contains(".")) {
                    newsAccessoryDto.filenameExtension = newsAccessoryDto.accessOryroute.substring(newsAccessoryDto.accessOryroute.indexOf(".") + 1).toUpperCase();
                }
                nadlist.add(newsAccessoryDto);
            }
        }

        return nadlist;
    }

    /**
     * 获取比参数 date 更新的新闻条数
     */
    public static int fetchNewNewstotalFromEs(Date date) {
        try {
            String df = "yyyyMMddHHmmss";
            AdvanceSearchDto asd = new AdvanceSearchDto();
            asd.itype = Lists.newArrayList("1"); //新闻
            asd.startTime = DateFormatUtils.format(DateUtils.addSeconds(date, 1), df); //这里要加上1秒,不然总会返回最新的一条
            asd.endTime = DateFormatUtils.format(DateUtils.addDays(date, 100), df); // + 100天,这个够可以了吧
            asd.pageNo = 1;
            asd.pageSize = 1;
            String asdJson = asd.toJson();
            Logger.debug("是否有新的新闻搜索,send data to es= %s", asdJson);
            JsonElement result = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
            Logger.debug("es返回内容=%s", result.toString());
            JsonObject jsonObject = result.getAsJsonObject();
            int total = jsonObject.getAsJsonPrimitive("total").getAsInt();
            Logger.debug("是否有新的新闻搜索, es返回总条数=%d", total);
            return total;
        } catch (Exception e) {
            Logger.error(e, "是否有新的新闻搜索出错,为了不让前端应用出错,返回0");
            return 0;
        }
    }

    /**
     * 获取比参数 date 更新的新闻条数
     */
    public static int fetchNewNewstotal(Date date) {
        String sql = SqlLoader.getSqlById("newNewstotalSql");
        return CustomDbUtil.queryCount(sql, date).intValue();
    }

    public static String fetchMaxDeclareDate() {
        String sql = SqlLoader.getSqlById("maxDeclareDateSql");
        Date maxDate = CustomDbUtil.queryCustomDbWithHandler(sql, new ScalarHandler<Date>());
        if (maxDate == null) {
            maxDate = new Date();
        }
        return CommonUtils.getFormatDate("yyyy-MM-dd HH:mm:ss", maxDate);
    }

    public static String fetchMaxDeclareDateFromEs(){
        Date maxDate = MessageIndexService.getMaxDateFromES("1"); //新闻
        if (maxDate == null) {
            maxDate = new Date();
        }
        return CommonUtils.getFormatDate("yyyy-MM-dd HH:mm:ss", maxDate);
    }
}
