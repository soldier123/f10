package dto;

import com.google.common.base.Joiner;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import play.data.binding.As;

import java.util.ArrayList;
import java.util.List;

/**
 * 资讯索引普通搜索请求参数
 * User: panzhiwei
 * Date: 13-3-27
 * Time: 下午5:03
 * To change this template use File | Settings | File Templates.
 */
public class CommonSearchDto {
    @Expose
    public String cmd = "commonInfoSearch";                //命令类型
    @Expose
    public int pageNo = 1;                //查询页码
    @Expose
    public int pageSize = 40;              //请求条数
    @SerializedName("keyword")
    @Expose
    public String keyWord = "";           //关键字
    @Expose
    public String startTime = "";          //发布起始时间
    @Expose
    public String endTime = "";            //发布结束时间
    @SerializedName("itype")
    @Expose
    public int iType;                 //类型 0：全部  1：新闻 2：公告  3： 研报

    public String timeName = "今日";
    public String timeValue = "today";

    @Expose
    public String tempField;

    public static final String _CUSTOM = "自定义";//定义一个常量.在MessageIndexService.java中使用到

    @As(",")
    public List<String> newsSource = new ArrayList<String>();                         //新闻来源
    @As(",")
    public List<String> newsClassify = new ArrayList<String>();                      //新闻分类
    @As(",")
    public List<String> bulletinClassify = new ArrayList<String>();               //公告类型
    @As(",")
    public List<String> bulletinPlateTree = new ArrayList<String>();              //公告板块树
    @As(",")
    public List<String> reportPlateTree = new ArrayList<String>();                //研报板块树
    @As(",")
    public List<String> reportOrg = new ArrayList<String>();                      //研报来源
    @As(",")
    public List<String> reportClassify = new ArrayList<String>();                 //研报分类
    @As(",")
    public List<String> advanceType = new ArrayList<String>();

    public int searchType = 1;                       //搜索类型

    public long advanceId=-1;


    public String getTypeName() {
        if (iType == 0) {
            return "全部";
        } else if (iType == 1) {
            return "新闻";
        } else if (iType == 2) {
            return "公司公告";
        } else {
            return "研究报告";
        }
    }

    public String getSearchDate() {
        if (timeName.equals("今日")) {
            return "今日";
        } else if (timeName.equals("最近一周")) {
            return "最近一周";
        } else if (timeName.equals("最近一个月")) {
            return "最近一个月";
        } else if (timeName.equals("最近三个月")) {
            return "最近三个月";
        } else if (timeName.equals("最近一年")) {
            return "最近一年";
        } else {
            return "自定义";
        }
    }

    public String getCustomerNewsSource(){
        return Joiner.on(",").skipNulls().join(newsSource) ;
    }                         //新闻来源
    public String getCustomerNewsClassify(){
        return Joiner.on(",").skipNulls().join(newsClassify) ;
    }                      //新闻分类
    public String getCustomerBulletinClassify(){
        return Joiner.on(",").skipNulls().join(bulletinClassify) ;
    }               //公告类型
    public String getCustomerBulletinPlateTree(){
        return Joiner.on(",").skipNulls().join(bulletinPlateTree) ;
    }              //公告板块树
    public String getCustomerReportPlateTree(){
        return Joiner.on(",").skipNulls().join(reportPlateTree) ;
    }
    public String getCustomerReportOrg(){
        return Joiner.on(",").skipNulls().join(reportOrg) ;
    }                      //研报来源
    public String getCustomerReportClassify(){
        return Joiner.on(",").skipNulls().join(reportClassify) ;
    }                 //研报分类
    public String getCustomerAdvanceType(){
        return Joiner.on(",").skipNulls().join(advanceType) ;
    }

}
