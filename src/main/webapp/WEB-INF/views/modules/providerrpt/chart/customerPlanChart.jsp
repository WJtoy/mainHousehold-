<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">

    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/customerPlanChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }
            var planOrderQty = data.data.planOrderQty;
            $("#planOrderQty").text(planOrderQty);

            var lastMonthPlanOrderRate = data.data.lastMonthPlanOrderRate;
            $("#lastMonthPlanOrderRate").text(lastMonthPlanOrderRate);
            var lastYearPlanOrderRate = data.data.lastYearPlanOrderRate;
            $("#lastYearPlanOrderRate").text(lastYearPlanOrderRate);





            var myChart1 = echarts.init(document.getElementById('main1'));

            var productCategory = data.data.productCategory;
            var planOrders = data.data.planOrders;
            var planOrderTotal = data.data.planOrderTotal;

            setTimeout(function () {
                option = {
                    grid: {
                        left: '1%',
                        right: '18%',
                        bottom: '-12%',
                        top: '5%',
                        containLabel: true
                    },
                    xAxis: {
                        show: false,
                        type: 'value',
                        max: planOrderTotal
                    },
                    yAxis: {
                        show: true,
                        type: 'category',
                        axisLine: {
                            show: false
                        },
                        axisTick: {       //y轴刻度线
                            show: false
                        },
                        data: productCategory
                    },
                    series: [
                        {
                            type: 'bar',
                            margin: '50%',
                            color: '#00B0FF',
                            showBackground: true,
                            barWidth: 8,

                            data: planOrders,
                            label: {
                                color: '#999999', //字的颜色
                                position: [155, 0],
                                show: true
                            },
                            itemStyle: {
                                normal: {
                                    barBorderRadius: 20
                                }
                            }
                        }

                    ]
                };

                myChart1.setOption(option);
            });
        }
    });
</script>
