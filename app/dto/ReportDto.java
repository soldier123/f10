package dto;

import bussiness.ReportService;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * 研报
 * User: panzhiwei
 * Date: 13-1-17
 * Time: 下午3:13
 * To change this template use File | Settings | File Templates.
 */
public class ReportDto {
    //id
    public Long id = 0L;
    //标题
    public String title;
    //发布日期
    public Date declareDate;
    //发布机构名称
    public String institutionName;
    //搜索关键字
    public String researcherName;
    //研报ID
    public Long reportId;
    //行号
    public int rowNum;
    //股票代码
    public List<String> symbol;
    //获取第一个代码
    public String getSymbolOne() {
        if (symbol == null || symbol.size() == 0) {
            return null;
        }
        /*if (keyword != null && symbol.size() > 1 && symbol.contains(keyword.trim())) {
            return keyword;
        }*/
        return symbol.get(0);
    }
    //从redis获取股票简称
    public String getStockName() {
        if (symbol == null || symbol.size() == 0) {
            return null;
        }
        return BondSec.allSecMap.containsKey(getSymbolOne()) ? BondSec.allSecMap.get(getSymbolOne()) : "";
    }
}
