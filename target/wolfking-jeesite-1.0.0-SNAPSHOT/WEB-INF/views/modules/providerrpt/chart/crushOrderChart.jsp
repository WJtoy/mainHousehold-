
<%@ page contentType="text/html;charset=UTF-8"%>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/crushOrderChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }

            var orderCrushQty = data.data.orderCrushQty;
            $("#orderCrushQty").text(orderCrushQty);

            $("#orderCrush").text(orderCrushQty);

            var onceCrushQty = data.data.onceCrushQty;
            $("#onceCrushQty").text(onceCrushQty);

            var repeatedlyCrushQty = data.data.repeatedlyCrushQty;
            $("#repeatedlyCrushQty").text(repeatedlyCrushQty);

            if(data.data.completedOnceCrushRate != null){
                var completedOnceCrushRate = data.data.completedOnceCrushRate;
                $("#completedOnceCrushRate").text(completedOnceCrushRate + "%");
            }

            if(data.data.completedRepeatedlyCrushRate != null){
                var completedRepeatedlyCrushRate = data.data.completedRepeatedlyCrushRate;
                $("#completedRepeatedlyCrushRate").text(completedRepeatedlyCrushRate + "%");
            }
            if(data.data.onceCrushPlainOrderRate != null){
                var onceCrushPlainOrderRate = data.data.onceCrushPlainOrderRate;
                $("#onceCrushPlainOrderRate").text(onceCrushPlainOrderRate + "%");
            }

            var completedCrushQty = data.data.completedCrushQty;
            $("#completedCrushQty").text(completedCrushQty);

            var completedOnceCrush = data.data.completedOnceCrush;
            $("#completedOnceCrush").text(completedOnceCrush);

            var completedRepeatedlyCrush = data.data.completedRepeatedlyCrush;
            $("#completedRepeatedlyCrush").text(completedRepeatedlyCrush);

            var completed24hour = data.data.completed24hour;
            var completed48hour = data.data.completed48hour;
            var over48hourCompleted = data.data.over48hourCompleted;
            var myChart17 = echarts.init(document.getElementById('main18'));
            var myChart16 = echarts.init(document.getElementById('main17'));

            setTimeout(function () {
                option = {
                    barGap: '-100%',
                    grid: {
                        left: '1%',
                        right: '35%',
                        bottom: '-12%',
                        top: '5%',
                        containLabel: true
                    },
                    xAxis: {
                        show: false,
                        type: 'value',
                        max: 473
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
                        data: ["没有网点","试用网点","常用网点"]
                    },
                    series: [
                        {
                            type: 'bar',
                            margin: '50%',

                            showBackground: true,
                            barWidth: 8,

                            data: [85,38,350],
                            label: {
                                position: [175, 0],
                                show: true
                            },
                            itemStyle: {
                                normal: {
                                    barBorderRadius: 50,
                                    color: function(params) {
                                        var colorList = ["#FADB14", "#FF9502", "#F3637B"];
                                        return colorList[params.dataIndex]
                                    }
                                }
                            }
                        },
                        {
                            type: 'bar',
                            margin: '50%',
                            color: '#00B0FF',
                            showBackground: true,
                            barWidth: 8,
                            data: [17.97,8.04,73.99],
                            label: {
                                color: '#999999', //字的颜色
                                formatter: '{c}%',
                                position: [125, 0],
                                show: true
                            },
                            itemStyle: {
                                normal: {
                                    barBorderRadius: 50,
                                    color: function(params) {
                                        var colorList = ["#FADB14", "#FF9502", "#F3637B"];
                                        return colorList[params.dataIndex]
                                    }
                                }
                            }
                        }

                    ]
                };
                myChart16.on("mouseover", function (params){

                    myChart16.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart16.setOption(option);
            });

            //环形图
            setTimeout(function () {
                option = {
                    tooltip: {
                        trigger: 'item',
                        formatter: function (params) {
                            var res = '';
                            res += params.marker + params.percent + '% : ' + params.value;
                            return res;
                        },
                        position: function (point, params, dom, rect, size) {

                            var x = 0; // x坐标位置
                            var y = 0; // y坐标位置

                            // 当前鼠标位置
                            var pointX = point[0];
                            var pointY = point[1];


                            // 提示框大小
                            var boxWidth = size.contentSize[0];
                            var boxHeight = size.contentSize[1];

                            if(pointX > 174){
                                x = pointX - boxWidth + 100;
                            }else{
                                x= pointX - boxWidth-10;
                            }


                            // boxHeight > pointY 说明鼠标上边放不下提示框
                            if (boxHeight > pointY) {
                                y = 5;
                            } else { // 上边放得下
                                y = pointY - boxHeight;
                            }

                            return [x, y];
                        }
                    },
                    series: [
                        {
                            type: 'pie',
                            radius: ['60%', '85%'],
                            hoverOffset: 3,
                            avoidLabelOverlap: false,
                            hoverAnimation:true,
                            label: {
                                normal: {
                                    show: false,
                                    position: 'center'
                                }

                            },
                            color: ["#00B0FF", "#13C2C2","#F3637B"],
                            data: [completed24hour, completed48hour, over48hourCompleted]
                        }
                    ]
                };

                myChart17.setOption(option);
            });

        }
    });

</script>
