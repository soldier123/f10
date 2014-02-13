
/*
*股本股东
* 主力机构持股 柱状图
 */
$(function () {
    var histogram = new Highcharts.Chart({
        chart:{
            renderTo:'histogram',
            type:'column',
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:null
        },
        xAxis:{
            categories : barData.xAxis
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
                    return this.value + '%';
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
                return '' +
                    this.series.name + ':  (' +this.y.toFixed(2)+ ')';
            }
        },
        plotOptions:{
            column:{
               // stacking:'normal',//控制叠加状态
                dataLabels:{
                    enabled:false,
                    color:(Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white'
                },
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        series : barData.series
    });
});

//主力机构持股 饼图
$(function () {
    var pieChart = new Highcharts.Chart({
        chart:{
            renderTo:'pieChart',
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
            pointFormat:'<b>{point.y}%</b>',
            valueDecimals:2
        },
        legend:{
            labelFormatter:function () {
                if(this.name=="非机构股东"){
                    return this.name;
                }else{
                    return this.name + '(' + this.num + ')';
                }
            }
        },
        plotOptions:{
            pie:{
                allowPointSelect:true,
                cursor:'pointer',
                dataLabels:{
                    formatter:function () {
                        return  '<span style="fill:white">' + this.y.toFixed(2) + '%</span> ';
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
                data: picData.seriesData
            }
        ]

    });
});

//股本结构
$(function () {
    var structureChart = new Highcharts.Chart({
        chart:{
            renderTo:'structureChart',
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
            pointFormat:'<b>{point.y}%</b>',
            valueDecimals:1
        },
        legend:{
            labelFormatter:function () {
                return this.name;
            }
        },
        plotOptions:{
            pie:{
                allowPointSelect:true,
                cursor:'pointer',
                dataLabels:{
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %'
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
                data:[
                    ['流通A股', structureData.tradableSharesA],
                    ['股改限售股份', structureData.lockSharesTotal]
                ]
            }
        ]
    });

});