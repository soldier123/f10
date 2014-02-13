//各股的
$(function () {
    var secChart = new Highcharts.Chart({
        chart:{
            renderTo:'secChart',
            type:'bar',
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:null
        },
        xAxis:{
            categories:['募资', '派现'],
            labels:{
                style:{font:'normal 12px 宋体'}
            }
        },
        yAxis:{
            min:0,
            lineWidth:1,
            title:{
                text:null
            },
            labels: {
                align: 'right'
            }
        },
        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },
        legend:{
            reversed:false
        },
        tooltip:{
            formatter:function () {
                return '' +
                    this.series.name + ': ' + this.y + '';
            }
        },
        plotOptions:{
            series:{
                stacking:'normal'
            },
            bar:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        series:secChartData
    });
});

//市场的
$(function () {
    var marketChart = new Highcharts.Chart({
        chart:{
            renderTo:'marketChart',
            type:'bar',
            borderRadius:0 /*边框不要圆角*/
        },
        title:{
            text:null
        },
        xAxis:{
            categories:[sname, '市场平均'],
            labels:{
                style:{font:'normal 12px 宋体'}
            }
        },
        yAxis:{
            //min:0,
            //max:0.2,
            lineWidth:1,
            title:{
                text:null
            },
            labels: {
                formatter: function() {
                        return '' + this.value  + '%'
                },
                align: 'right'

            }
        },
        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },
        legend:{
            reversed:false
        },
        tooltip:{
            formatter:function () {
                return '派现总额/融资总额(' +
                    this.series.name + '): ' + this.y.toFixed(2) + '%';
            }
        },
        plotOptions:{
            series:{
                stacking:'normal'
            },
            bar:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }
        },
        series:marketCharData
    });
});