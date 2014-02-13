package dto;

import play.data.binding.As;

/**
 * 研报订阅条件
 * User: panzhiwei
 * Date: 13-1-25
 * Time: 上午9:13
 * To change this template use File | Settings | File Templates.
 */
public class ReportOrderCnd {
    //报告类型
    @As(",")
    public String[] reportClassify;
    //研报来源
    @As(",")
    public String[] reportOrg;
    //板块树
    @As(",")
    public String[] plateTree;
    //证券代码
    @As(",")
    public String[] symbolArr;



}
