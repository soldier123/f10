package dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * description: 研究报告详细DTO
 * User: 刘泓江
 * Date: 13-1-17
 * Time: 上午11:36
 */
public class BulletinListDto {
    //股票代码 直接从数据库取的时候用这个来取股票代码
    public String symbol;
    //
    @SerializedName("symbols")
    @Expose
    //从搜索引荐走的时候取这里的股票代码
    public List<String> symboys;

    //发布时间
    @Expose
    @SerializedName("declaredate")
    public Date declareDate;

    //标题
    @Expose
    public String title;

    //附件路径
    @Expose
    @SerializedName("attach")
    public String announcementRoute;

    public String shortName;

    //股票简称
    public String getShortName() {
        return BondSec.allSecMap.containsKey(symbol) ? BondSec.allSecMap.get(symbol) : "";
    }
    public String getSymbol(){
        if(symboys !=null && symboys.size()>0){
            return symboys.get(0);
        }
        return null;
    }

}
