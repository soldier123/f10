package dto;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * desc: 高级搜索Dto
 * User: weiguili(li5220008@163.com)
 * Date: 13-4-1
 * Time: 下午1:52
 */
public class AdvanceSearchDto {
    public String cmd = "advanceInfoSearch"; //cmd 标识名
    public int pageNo = 1; //页码
    public int pageSize = 40; //请求条数
    public String keyword = "";//关键字
    public List<String> nsource = new ArrayList<String>(); //新闻来源
    public List<String> ncids = new ArrayList<String>(); //新闻分类
    public List<String> niids = new ArrayList<String>(); //行业新闻分类
    public List<String> acids = new ArrayList<String>(); //公告分类
    public List<String> aiids = new ArrayList<String>(); //分行行业分类
    public List<String> rsource = new ArrayList<String>(); //研报来源
    public List<String> rcids = new ArrayList<String>();  //研报分类
    public List<String> riids = new ArrayList<String>();  //研报行业分类
    public List<String> symbols = new ArrayList<String>(); //股票代码
    public List<String> itype = new ArrayList<String>(); //类型 标识三个大类选择状态 （全选 例：[1,2,3] ）
    @Expose
    public String startTime = "";          //发布起始时间
    @Expose
    public String endTime = "";            //发布结束时间

    public String toJson() {
        if(pageNo<1){
            pageNo = 1;
        }
        if(pageSize<1){
            pageSize = 40;
        }
        nsource = ltrimList(nsource);
        ncids = ltrimList(ncids);
        acids = ltrimList(acids);
        aiids = ltrimList(aiids);
        rsource = ltrimList(rsource);
        rcids = ltrimList(rcids);
        riids = ltrimList(riids);
        niids = ltrimList(niids);
        symbols = ltrimList(symbols);

        return CommonUtils.createGson().toJson(this);
    }

    private static List<String> ltrimList(List<String> list) {
        if (list == null || (list.size() == 1 && "".equals(list.get(0)))) {
            return Lists.newArrayList();
        }

        return list;
    }
}
