package dto;

import com.google.common.base.Joiner;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import util.CommonUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资讯索引
 * User: liangbing
 * Date: 13-3-26
 * Time: 下午5:10
 */
public class MessageIndexDto {
    @SerializedName("declaredate")
    @Expose
    public Date declaredate;

    @SerializedName("symbols")
    @Expose
    public List<String> symbol;

    @Expose
    public String stockName;

    @Expose
    public String title;

    @Expose
    @SerializedName("itype")
    public int type;

    @Expose
    public String attach;

    public String keyword;

    @Expose
    @SerializedName("newsid")
    public String newsId;

    @Expose
    @SerializedName("annid")
    public String annId;

    @Expose
    @SerializedName("repid")
    public String repId;

    @Expose
    @SerializedName("nsource")
    public String nSource; //新闻来源

    /**
     * 研报作者
     */
    @Expose
    @SerializedName("rauthor")
    public List<String> repAuthors;

    /**
     * 研报来源机构名称
     */
    @Expose
    @SerializedName("rsnames")
    public List<String> repSourceNams;

    public String getStringType() {
        if (type == 1) {
            return "新闻";
        } else if (type == 2) {
            return "公司公告";
        } else {
            return "研究报告";
        }
    }

    public String getSymbolOne() {
        if (symbol == null || symbol.size() == 0) {
            return null;
        }
        if (keyword != null && symbol.size() > 1 && symbol.contains(keyword.trim())) {
            return keyword;
        }
        return symbol.get(0);
    }

    public String getStockName() {
        if (symbol == null || symbol.size() == 0) {
            return null;
        }
        return BondSec.allSecMap.containsKey(getSymbolOne()) ? BondSec.allSecMap.get(getSymbolOne()) : "";
    }

    static Joiner commaJoiner = Joiner.on(',').skipNulls();

    public String repSourceNamsJoin() {
        if (repSourceNams == null || repSourceNams.size() == 0) {
            return null;
        }

        return commaJoiner.join(repSourceNams);
    }

    public String repAuthorsJoin() {
        if (repAuthors == null || repAuthors.size() == 0) {
            return null;
        }

        return commaJoiner.join(repAuthors);
    }

}
