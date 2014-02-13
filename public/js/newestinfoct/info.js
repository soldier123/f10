//最新市场表现
$(function () {
    var lastMarketShowChart = new Highcharts.Chart({
        chart:{
            renderTo:'lastMarketShowChart',
            marginBottom:200,
            type:'line',
            borderRadius:0 /*边框不要圆角*/
        },

        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },

        title:{
            text:null
        },

        subtitle:{
            text:null
        },

        legend:{
            align:'left',
            x:5,
            y:-60,
            verticalAlign:'bottom',
            floating:true,
            backgroundColor:(Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
            borderColor:'#CCC',
            borderWidth:1,
            shadow:false
        },

        xAxis:{
            categories:['最近一周涨幅(%)', '最近一月涨幅(%)', '最近三月涨幅(%)', '最近六月涨幅(%)', '年初至今涨幅(%)'],
            paddingBottom:200,
            labels:{
                rotation:-45,
                align:'right',
                style:{
                    font:'normal 14px 宋体',
                    fontFamily:'宋体'
                }
            }

        },

        yAxis:{
            endOnTick: false,
            title:{
                text:null
            },
            labels:{
                formatter:function () {
                    return this.value + '%';
                }
            },
            plotLines:[
                {
                    value:0,
                    width:1,
                    color:'#808080'
                }
            ]
        },

        plotOptions:{
            series:{
                marker:{
                    enabled:false   //不显示线条细节
                },
                enableMouseTracking:true,    //取消鼠标事件
                connectNulls:true
            },
            line:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },

        tooltip:{
            formatter:function () {
                return '<b>' + this.series.name + '</b><br/>' +
                    this.x + ': ' + this.y;
            }
        },

        series: markUpPicData.seriesData
    });
});

//最新财务分析 -- 主营收入
$(function () {
    var miSerialArr = []; //画图数据
    var item = {'name':'主营收入(全年)', 'data':finDrawDataJson.mainIncomeYearData};
    miSerialArr.push(item);
    if(finDrawDataJson.seasonName){ //说明有季度数据
        var item2 = {'name':'主营收入(' + finDrawDataJson.seasonName + ')'};
        item2['data'] = finDrawDataJson.mainIncomeSeasonData;
        miSerialArr.push(item2);
    }


    var mainIncomeChart = new Highcharts.Chart({
        chart:{
            renderTo:'mainIncomeChart',
            type:'column',
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:null
        },
        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },
        xAxis:{
            //categories:['2008', '2009', '2010', '2011', '2012'],
            categories:finDrawDataJson.yearList,
            title:{
                text:null
            },
            labels:{
                style:{font:'normal 12px 宋体'}
            }
        },
        yAxis:{
            /*
            min:0.00,
            max:300.00,
            tickInterval:20,
            */
            endOnTick: false,
            title:{
                text:'主营收入（万元）'
                // align: 'high'
            },
            labels:{
                overflow:'justify',
                formatter:function () {
                    return this.value + ' ';
                },
                style:{font:'normal 12px 宋体'}
            }



        },
        tooltip:{
            formatter:function () {
                return '<span style="fill:'+this.series.color+'">' + this.x + ': ' + this.y+'</span>';
            }
        },
        legend:{
            enabled:true
        },
        plotOptions:{
            column:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        /*
        series:[
            {
                name:'主营收入（全年）',
                data:[140, 150, 160, 288, 100 ]
            },
            {
                name:'主营收入（第一季度）',
                data:[50, 66, 75, 45, 38 ]
            }
        ]*/
        series : miSerialArr

    });

});

//最新财务分析 -- 净利润
$(function () {

    //{name:'净利润（全年）', color:'red', type:'column', data:[49.9, 71.5, 106.4, 129.2, 144.0]},
    //{name:'净利润（一季度）', color:'#4572A7', type:'column', data:[49.9, 71.5, 106.4, 129.2, 144.0]},
    //{name:'基本每股收益（全年）', color:'green', type:'line', yAxis:1, data:[7.0, 6.9, 9.5, 14.5, 18.2]},
    //{name:'基本每股收益（一季度）', color:'#89A54E', type:'line', yAxis:1, data:[7.0, 5.0, 4.5, 18.5, 19.2]}
    var seriesArr = []; //画图数据
    var item = {'name':'净利润(全年)', 'data':finDrawDataJson.netProfitYearData, /*color:'red',*/ type:'column'};
    seriesArr.push(item);
    if(finDrawDataJson.seasonName){ //说明有季度数据
        var item2 = {'name':'净利润(' + finDrawDataJson.seasonName + ')',/* color:'#4572A7',*/ type:'column'};
        item2['data'] = finDrawDataJson.netProfitSeasonData;
        seriesArr.push(item2);
    }
    var item3 = {'name':'基本每股收益(全年)', 'data':finDrawDataJson.epsYearData, /*color:'green',*/ type:'line', yAxis:1};
    seriesArr.push(item3);
    if(finDrawDataJson.seasonName){ //说明有季度数据
        var item4 = {'name':'基本每股收益(' + finDrawDataJson.seasonName + ')', /*color:'#89A54E',*/ type:'line', yAxis:1};
        item4['data'] = finDrawDataJson.epsSeasonData;
        seriesArr.push(item4);
    }

    var netProfitChart = new Highcharts.Chart({
        chart:{
            renderTo:'netProfitChart',
            borderRadius:0 /*边框不要圆角*/
            //zoomType: 'xy'
        },
        title:{
            text:null
        },
        subtitle:{
            text:null
        },
        xAxis:[
            {
                //categories:['2008', '2009', '2010', '2011', '2012']
                categories:finDrawDataJson.yearList
            }
        ],
        yAxis:[
            { // Primary yAxis
                labels:{
                    formatter:function () {
                        return this.value;
                    },
                    style:{font:'normal 12px 宋体'}
                },
                title:{
                    text:'净利润（万元）'
                }
            },
            { // Secondary yAxis
                title:{
                    text:' 基本每股收益'
                },
                labels:{
                    formatter:function () {
                        return this.value;
                    },
                    style:{font:'normal 12px 宋体'}
                },
                opposite:true
            }
        ],
        tooltip:{
            formatter:function () {
                return '<span style="fill:'+this.series.color+'">' +
                    this.x + ': ' + this.y+'</span>';
            }
        },
        legend:{
            enabled:true
        },
        plotOptions:{
            column:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            },
            line:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        /*
        series:[
            {name:'净利润（全年）', color:'red', type:'column', data:[49.9, 71.5, 106.4, 129.2, 144.0]},
            {name:'净利润（一季度）', color:'#4572A7', type:'column', data:[49.9, 71.5, 106.4, 129.2, 144.0]},
            {name:'基本每股收益（全年）', color:'green', type:'line', yAxis:1, data:[7.0, 6.9, 9.5, 14.5, 18.2]},
            {name:'基本每股收益（一季度）', color:'#89A54E', type:'line', yAxis:1, data:[7.0, 5.0, 4.5, 18.5, 19.2]}

        ]*/
        series: seriesArr
    });

});

 //最新综合评级
$(function () {
    //右上角 横向图
    var compositeRatingChart;
    var arrowLeftMargin = -8;
    var arrowTopMargin = 38;
    var arrowWidth = 16;
    var arrowHeight = 16;
    var columnWidth = 96.1;
    //var ratingArray = [5.40, 4.34, 3.20, 2.00, 0.9,-0.5];
    var ratingArray = [5.40, 4.34, 3.20, 2.00, 0.9,-0.5];

    //var rating = 1.0;
    var rating = reportRatingCharJson.rating;

    function countPoint(value) {
        var result = arrowLeftMargin - arrowWidth / 2;
        for (var i = 1; i < ratingArray.length; i++) {
            if (value >= ratingArray[i]) {
                result = result + (value - ratingArray[i]) / (ratingArray[i - 1] - 0.01 - ratingArray[i]) * columnWidth + ((columnWidth * 1.01) * (5 - i));
                break;
            }
        }
        return result;
    }

    compositeRatingChart = new Highcharts.Chart({
        chart:{
            renderTo:'compositeRatingChart',
            defaultSeriesType:'bar',
            height:150,
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:"机构研究评级统计分析(前1月)"
        },
        subtitle:{
            //text:"(" + "深发展A" + "-" + "2012-09-18" + ")"
           // text:"(" + reportRatingCharJson.name + "-" + reportRatingCharJson.reportDate + ")"
        },
        credits:{
            style:{
                align:'right',
                verticalAlign:'bottom'
            }
        },
        xAxis:{
            labels:{
                enabled:false
            },
            gridLineWidth:0,
            lineWidth:0,
            tickLength:0
        },
        yAxis:{
            min:0,
            title:{
                text:''
            },
            labels:{
                enabled:false

            },

            gridLineWidth:0,
            tickInterval:1.09,
            max:5.45
        },
        legend:{
            reversed:true,
            y:-15
        },
        tooltip:{
            formatter:function () {
                return '' +
                    this.series.name + ': ' + this.y + '';
            },
            enabled:false
        },
        plotOptions:{
            series:{
                stacking:'normal',
                pointWidth:20,
                events:{
                    legendItemClick:function (event) {
                        return false;
                    }
                },
                shadow:false,
                borderWidth:0

            }
        },
        series:[
            {name:'买入', color:"#fb6035", data:[1.26]},
            {name:'增持', color:"#fee1b2", data:[1.02]},
            {name:'中性', color:"#fffbd4", data:[1.00]},
            {name:'减持', color:"#d7f3b7", data:[1.09]},
            {name:'卖出', color:"#84ce84", data:[1.09]}
        ]
    }, function (chart) { // on complete
        var x = countPoint(rating);
        if (rating != 0) {
            var imgkey = "1.png";
            chart.renderer.image(_gF10.ctx + '/public/images/point_' + imgkey, x, arrowTopMargin, arrowWidth, arrowHeight).attr({zIndex:10}).add();
            var t = rating.toFixed(1).toString();
            var textLength = t.length;
            var width = x + arrowWidth / 2 - (textLength * 12 / 4);
            t = '<span style="font-size:12px;font-family:Courier New">' + t + '</span>';
            var text = chart.renderer.text(t, width, 70).attr({zIndex:10}).add();
        }
    });

});


