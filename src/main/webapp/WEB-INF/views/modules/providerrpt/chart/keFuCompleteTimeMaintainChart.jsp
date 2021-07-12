<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/keFuCompletionTimeMaintainChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }

            var createDates = data.data.createDates;
            var strComplete24hourRates = data.data.strComplete24hourRates;
            var strComplete48hourRates = data.data.strComplete48hourRates;
            var strComplete72hourRates = data.data.strComplete72hourRates;
            var strOverComplete72hourRates = data.data.strOverComplete72hourRates;
            var myChart3 = echarts.init(document.getElementById('main4'));
            setTimeout(function () {
                option = {
                    title: {
                        left: 'center',
                        top:'3%',
                        text: '客服完成时效(维修)',
                        textStyle: {
                            fontSize: '14',
                            color:"#666666"
                        }
                    },
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            var res = params[0].name + '<br/>';
                            for (var i = 0; i < params.length; i++) {
                                res += params[i].marker + params[i].seriesName + ' : ' + params[i].value + '%</br>';
                            }
                            return res;
                        }
                    },
                    legend: {
                        top:'12%',
                        data: ['24小时', '48小时', '72小时', '96小时']
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '5%',
                        top:'21%',
                        containLabel: true
                    },
                    xAxis: {
                        type: 'category',
                        boundaryGap: false,
                        data: createDates,
                        axisLabel:{
                            interval:0
                        }
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: {
                            show: true,
                            interval: 'auto',
                            formatter: '{value} %',
                            showMaxLabel:false
                        },
                        minorTick: {
                            show: true ,
                            splitNumber: 10
                        },
                        splitLine: {  // 坐标轴在 grid 区域中的分隔线。
                            show: false
                        } ,
                        max: 55,
                        min: 0,
                        splitNumber: 5
                    },
                    series: [
                        {
                            name: '24小时',
                            type: 'line',
                            color: "#00B0FF",
                            data: strComplete24hourRates
                        },
                        {
                            name: '48小时',
                            type: 'line',
                            color: "#13C2C2",
                            data: strComplete48hourRates
                        },
                        {
                            name: '72小时',
                            type: 'line',
                            color: "#F3637B",
                            data: strComplete72hourRates
                        },
                        {
                            name: '96小时',
                            type: 'line',
                            color: "#FFCB01",
                            data: strOverComplete72hourRates
                        }

                    ]
                };

                myChart3.setOption(option);
            });

        }
    });



</script>