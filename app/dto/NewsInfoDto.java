package dto;

import util.CommonUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 新闻主题
 * User: liangbing
 * Date: 13-1-17
 * Time: 下午5:14
 */
public class NewsInfoDto implements Serializable {
    //新闻ID
    public String newsId;
    //公布日期
    public Date declareDate;
    //标题
    public String title = "";
    //正文
    public String newsContent = "";
    //来源
    public String newsSource = "";
    //作者
    public String autor = "";
    //是否有附件
    public String isAccessory = "";

    public String getRawTitle(){
        title =  title==null?"":title;
        title = CommonUtils.getText(title);
        //改到view模板端来切割
        /*if(title.length()>10){
            return title.substring(0,10) + "...";
        }else {
            return title;
        }*/
        return title;
    }

    //附件名称
    public String fullName;
    //附件路径
    public String accessOryroute;
    //后缀名
    public String  filenameExtension;

    public String getNewsContent(){
        if(newsContent!=null){
            newsContent = CommonUtils.getText(newsContent);
        }
        return newsContent;
    }

}
