
//图一（左上）
$(function () {
    //市场表现
    var marketPerforChar = new Highcharts.Chart({
        chart:{
            renderTo:'marketPerforChar',
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
            tickInterval :25 * 24 * 3600 * 1000,//X轴点间隔
            labels:{
                formatter:function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.value);
                },
                style:{font:'normal 12px 宋体'}
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
            }
        },
        tooltip:{
            formatter:function () {
                return   this.series.name + '<br/><strong>日期:</strong>' +
                    Highcharts.dateFormat('%Y%m%d', this.x) + '<strong>当日涨跌幅:</strong> ' + this.y + '%';
            }
        },
        plotOptions:{
            series:{
                lineWidth:1.8,
                marker:{
                    radius:1 //鼠标接触时 显示点的大小
                },
                connectNulls:true
            },
            line:{
                events:{
                    legendItemClick:highChartLegendItemClick
                }
            }

        },
        series:marketPerforseriesDataJson
    });
});

 //图二（右上）
//公司规模图
$(function () {
    var companyScalChart = new Highcharts.Chart({
            chart: {
                renderTo: 'companyScalChart',
                type: 'column',
                marginBottom: 50,
                borderRadius:0 /*边框不要圆角*/
            },
            title: {
                text: null
            },
            credits:{
                position: {
                    align:'right',
                    verticalAlign:'bottom'
                }
            },
            legend: {
                enabled:false
            },
            plotOptions: {
                series: {
                    /*color: '#AA4643'*/
                }
            },
            xAxis: {
                categories: companyScaleCharJson.xAxis,
                title: {
                    text: null
                },
                labels:{
                    style:{font:'normal 12px 宋体'}
                }
            },
            yAxis: {
                endOnTick: false,
                title: {
                    text: null
                },
                labels: {
                    overflow: 'justify'
                }
            },
            tooltip: {
              enabled:false
            },
            series: [{
                name: '',
                data: companyScaleCharJson.series
            }]
        });
});

//图三（左下）估值水平
$(function () {
    var appraisementChart = new Highcharts.Chart({
            chart: {
                renderTo: 'appraisementChart',
                type: 'column',
                marginBottom: 50,
                borderRadius:0 /*边框不要圆角*/
            },
            title: {
                text: null
            },
            credits:{
                position: {
                    align:'right',
                    verticalAlign:'bottom'
                }
            },
            legend: {
                enabled:false
            },
            plotOptions: {
                series: {
                    /*color: '#3D96AE'*/
                }
            },
            xAxis: {
                categories: appraisementCharJson.xAxis,
                title: {
                    text: null
                },
                labels:{
                    style:{font:'normal 12px 宋体'}
                }
            },
            yAxis: {
                endOnTick: false,
                title: {
                    text: null
                },
                labels: {
                    overflow: 'justify'
                }
            },
            tooltip: {
              enabled:false
            },
            series: [{
                name: '',
                data: appraisementCharJson.series
            }]
        });
});


//图四（右下）. 财务状况
$(function () {
    var financeStatusChar = new Highcharts.Chart({
            chart: {
                renderTo: 'financeStatusChar',
                type: 'column',
                borderRadius:0 /*边框不要圆角*/
            },
            title: {
                text: null
            },
            credits:{
                position: {
                    align: 'right',
                    verticalAlign: 'bottom'
                }
            },
            legend: {
                enabled:true

            },
            xAxis: {
                //categories: financeStatusCharJson.xAxis,
                categories: financeXData,
                title: {
                    text: null
                },
                labels:{
                    style:{font:'normal 12px 宋体'}
                }
            },
            yAxis: {
                // min: 0,
                // max:0.8,
                endOnTick: false,
                title: {
                    text: null
                },
                labels: {
                    overflow: 'justify'
                }
            },
            tooltip: {
                formatter: function() {
                    return '<span style="fill:'+this.series.color+'">'+
                        this.x +': '+ this.y+'</span>';
                }
            },
            plotOptions:{
                column:{
                    events:{
                        legendItemClick:highChartLegendItemClick
                    }
                }
            },
//            series:[{
//                name: ' ',
//                data: financeStatusCharJson.series
//            }]
            series:financeSeriesData
        });
});


