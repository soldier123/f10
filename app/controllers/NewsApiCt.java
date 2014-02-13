package controllers;

import bussiness.NewsSearchService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import util.CommonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 资讯类对外服务.
 * 提供给nt的首页要用
 * User: wenzhihong
 * Date: 13-4-16
 * Time: 下午5:40
 */
public class NewsApiCt extends BaseController{
    /**
     * 取最新的n条记录
     * @param n 新闻条数
     * @param type 类型
     */
    public static void topRecords(Integer n, Integer type) {
        if (n == null || !(n > 0 && n < 20)) {
            n = 20;
        }

        if (type == null) {
            type = 1;
        }

        NewsSearchService.SearchType searchType = NewsSearchService.SearchType.int2Type(type);

        NewsSearchService.Param param = new NewsSearchService.Param();
        param.pageSize = n;
        param.seType(searchType);

        NewsSearchService.Result result = NewsSearchService.fetchResult(param);
        Gson gson = CommonUtils.createGsonIncludeNullCustDateformat(CommonUtils.DATE_FORMAT_STR_ARR[1]);
        Map<String, Object> resultJsonMap = Maps.newHashMap();
        if (result != null) {
            List<Map<String, Object>> resultDatas = Lists.newArrayListWithCapacity(result.data.size());
            for (NewsSearchService.DataItem item : result.data) {
                Map<String, Object> dataItem = Maps.newHashMap();
                dataItem.put("title", item.title);
                dataItem.put("updatedate", item.declaredate);

                switch (searchType){
                    case news:
                        dataItem.put("guid", item.newsid);
                        break;
                    case bulletin:
                        dataItem.put("guid", item.annid);
                        break;
                    case report:
                        dataItem.put("guid", item.repid);
                        break;
                }

                resultDatas.add(dataItem);
            }
            resultJsonMap.put("sucess", true);
            resultJsonMap.put("data", resultDatas);
        }else {
            resultJsonMap.put("sucess", false);
            resultJsonMap.put("message", "搜索失败");
        }

        String json = gson.toJson(resultJsonMap);
        renderJSON(json);
    }


}
