//最新综合评级
$(function () {
    //右上角 横向图
    var compositeRatingChart;
    var arrowLeftMargin = -20;
    var arrowTopMargin = 38;
    var arrowWidth = 16;
    var arrowHeight = 16;
    var columnWidth = 96.1;
    //var ratingArray = [5.0, 4.10, 3.10, 2.10, 1.10, 1.0];
    var ratingArray = [5.90, 4.34, 3.20, 2.00, 0.9, -0.5];

    //var rating = 1.0;
    var rating = reportRatingCharJson.rating;

    function countPoint(value) {
        var result = arrowLeftMargin - arrowWidth / 2;
        for (var i = 1; i < ratingArray.length; i++) {
            if (value >= ratingArray[i]) {
                if (value < 2) {
                    result = result + 5;
                }
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
            {name:'买入', color:"#fb6035", data:[1.09]},
            {name:'增持', color:"#fee1b2", data:[1.09]},
            {name:'中性', color:"#fffbd4", data:[1.09]},
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


//机构预测及明细
$(function () {
    var predictChart = new Highcharts.Chart({
        chart:{
            renderTo:'predictChart',
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
                categories:yearList

            }
        ],
        yAxis:[
            { // Primary yAxis
                //tickInterval:20,
                endOnTick:false,
                labels:{
                    formatter:function () {
                        return this.value;
                    },
                    style:{font:'normal 12px 宋体'}
                },
                title:{
                    text:"净利润(亿元)"
                }
            },
            { // Secondary yAxis
                //tickInterval:0.5,
                //reversed:true,
                title:{
                    text:"每股收益(元)"
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
                return '<span style="fill:' + this.series.color + '">' +
                    this.x + ': ' + this.y + '</span>';
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
        series:[
            {name:'净利润', /*color:'red',*/ type:'column', data:netProfitList},
            {name:'每股收益', /*color:'#89A54E',*/ type:'line', yAxis:1, data:epsList}

        ]
    });

});


$(function () {
    //目标价预测图
    var priceForecastChart = new Highcharts.Chart({

        chart:{
            renderTo:'priceForecastChart',
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
            type:'datetime',
            tickInterval:5 * 24 * 3600 * 1000, //X轴点间隔
            labels:{
                rotation:-70,
                align:'right',
                formatter:function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.value);
                },
                style:{font:'normal 14px 宋体'}
            }
        },
        yAxis:{
            //max:14,
            //min:0,
            //tickInterval:2,
            endOnTick:false,
            title:{
                text:null
            },
            labels:{
                formatter:function () {
                    return this.value;
                },
                style:{font:'normal 12px 宋体'}
            }
        },

        tooltip:{
            formatter:function (tooltip) {
                var name = this.series.name;
                if (name == "预测目标价") {
                    var pointDataArr = this.series.options.data;//拿到series.data数据
                    var orgName; //机构名称
                    var priceTerm; //价格周期
                    var date = this.x;
                    var price = this.y
                    $.each(pointDataArr, function (index, array) {
                        if (date == array[0] && price == array[1]) {
                            priceTerm = array[2];
                            orgName = array[3];
                        }
                    });
                    var ddate = Highcharts.dateFormat('%Y-%m-%d', this.x);
                    var tipMessage = '<b>' + orgName + '</b> ' + ddate + '预测 <br/>' + priceTerm + '目标价:' + '<b>' + this.y + '</b>';
                    return tipMessage;
                } else if (name == "当前价") {
                    return  '<strong>日期:</strong>' +
                        Highcharts.dateFormat('%Y-%m-%d', this.x) + '<strong>当日收盘价:</strong> ' + this.y;
                }
            }
        },

        plotOptions:{
            spline:{
                events:{
                    legendItemClick:highChartLegendItemClick
                },
                connectNulls:true,
                lineWidth:1,
                marker:{
                    radius:1 //鼠标接触时 显示点的大小
                }
            },
            scatter:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        series:[
            {
                type:'scatter',
                name:'预测目标价',
                data:targetPriceCharJson,
                marker:{
                    radius:4
                }
            },

            {
                type:'spline',
                name:'当前价',
                data:actualPriceCharJson
            }
        ]
    });

});
