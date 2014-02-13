$(function () {
    //饼状图. 最近一期
    var picChart = new Highcharts.Chart({
        chart:{
            renderTo:'picChart',
            plotBackgroundColor:null,
            plotBorderWidth:null,
            plotShadow:false,
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
        tooltip:{
            formatter: function() {
                return '<b>'+this.percentage.toFixed(1) +'%</b>';
            }
        },
        legend:{
            labelFormatter:function () {
                return this.name;
            }
        },
        plotOptions:{
            pie:{
                startAngle: 180,
                allowPointSelect:true,
                cursor:'pointer',
                dataLabels:{
                    formatter:function () {
                        return '<span style="fill:white">' + this.percentage.toFixed(2) + ' %</span>';
                    }

                },
                showInLegend:true,
                point:{
                    events:{
                        legendItemClick:highChartPicLegendItemClick
                    }
                }
            }
        },
        series:[
            {
                type:'pie',
                data:picData
            }
        ]
    });
});

$(function () {
    //柱状图(最近1期)
    var top4Chart = new Highcharts.Chart({
        chart:{
            renderTo:'top4Chart',
            type:'column',
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:null
        },
        xAxis:{
            categories:barXData,
            labels:{
                style:{font:'normal 12px 宋体'}
            }
        },
        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },
        yAxis:{
            endOnTick: false,
            title:{
                text:null
            },
            labels:{
                formatter:function () {
                    return this.value;
                },
                style:{
                    color:'#89A54E'
                }
            },
            stackLabels:{
                enabled:false,
                style:{
                    fontWeight:'bold',
                    color:(Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        },
        tooltip:{
            formatter:function () {
                return  this.y+'(股)' ;

            }
        },
        plotOptions:{
            column:{
                stacking:'normal',
                dataLabels:{
                    enabled:false,
                    color:(Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white'
                },
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        series : barYData
    });
});