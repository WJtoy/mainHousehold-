<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">

    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/incurExpenseChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }
            var list = data.data;
            var total = 0;
            for (j = 0, len = list.length; j < len; j++) {
                total = list[j] + total;
            }
            total = total.toFixed(2);

            $("#total").text(total);

            var myChart2 = echarts.init(document.getElementById('main2'));

            setTimeout(function () {
                option = {
                    color: ['#00B0FF'],
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '1%',
                        height: '90%',
                        containLabel: true
                    },
                    xAxis: [
                        {
                            type: 'category',
                            data: ['远程费', '好评费', '时效费', '加急费', '补贴费', '互助基金', '其他费用'],
                            axisTick: {
                                show: false
                            }

                        }
                    ],
                    yAxis: [
                        {
                            type: 'value',
                            boundaryGap: [0, 0.1],
                            splitLine: {  // 坐标轴在 grid 区域中的分隔线。
                                show: true,
                                lineStyle: {
                                    type: 'dashed' // (实线：'solid'，虚线：'dashed'，星罗棋布的：'dotted')
                                }
                            },
                            axisTick: {       //y轴刻度线
                                show: false
                            }
                        }
                    ],
                    series: [
                        {
                            type: 'bar',
                            barWidth: '50%',
                            data: list,
                            label: {
                                show: true,
                                position: 'top'
                            }
                        }
                    ]
                };

                myChart2.setOption(option);
            });
        }
    });
</script>

