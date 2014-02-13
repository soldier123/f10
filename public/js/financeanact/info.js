//财务全景图
$(function () {
    var chart = new Highcharts.Chart({
        chart: {
            renderTo: 'finFullViewChart',
            polar: true,
            //type: 'area'
            type: 'line',
            borderRadius:0 /*边框不要圆角*/
        },

        credits:{
            position:{
                align:'right',
                verticalAlign:'bottom'
            }
        },

        title: {
            text: null,
            x: -80
        },

        pane: {
            size: '80%'
        },

        xAxis: {
            categories: fvItemStr,
            tickmarkPlacement: 'on',
            lineWidth: 0,
            labels:{
                style:{font:'normal 12px 宋体'}
            }
        },

        yAxis: {
            gridLineInterpolation: 'polygon',
            lineWidth: 0,
            min: 0,
            max: 5,
            tickInterval:1
            /* 控制是否把刻度标在图上
            labels:{
                enabled:false
            }
            */
        },
        /* 控制多边形是否显示点
        plotOptions: {
            series: {
                color:'yellow',
                marker: {
                    enabled: true   //不显示线条细节
                },
                enableMouseTracking: false    //取消鼠标事件
            }
        },
        */
        tooltip: {
            //enabled:true
            shared: true
            //valuePrefix: ''
        },

        legend: {
           enabled:false
        },

        series: [{
            name: " ",
            data: fvJsonData,
            pointPlacement: 'on'
        }]

    });
});