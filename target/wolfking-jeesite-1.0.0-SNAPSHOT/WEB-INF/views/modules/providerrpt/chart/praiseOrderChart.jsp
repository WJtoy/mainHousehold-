
<%@ page contentType="text/html;charset=UTF-8"%>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/praiseOrderChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }


            var myChart18 = echarts.init(document.getElementById('main19'));
            var myChart19 = echarts.init(document.getElementById('main20'));
            var myChart20 = echarts.init(document.getElementById('main21'));

            setTimeout(function () {
                option = {
                    grid: {
                        left: '-5%',
                        right: '33%',
                        bottom: '1%',
                        top: '100%',
                        containLabel: true
                    },
                    xAxis: {
                        show: false,
                        type: 'value',
                        max: 1322
                    },
                    yAxis: {
                        show: false,
                        type: 'category',
                        data: ["收入"]
                    },
                    series: [
                        {
                            type: 'bar',
                            margin: '50%',

                            showBackground: true,
                            barWidth: 8,
                            color: '#00B0FF',
                            data: [924],
                            label: {
                                color: '#999999',
                                formatter: '收入   '+ '1332元',
                                position: [195, 0],
                                show: true
                            },
                            itemStyle: {
                                normal: {
                                    barBorderRadius: 50,
                                }
                            }
                        }
                    ]
                };
                myChart19.on("mouseover", function (params){

                    myChart19.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart19.setOption(option);
            });


            setTimeout(function () {
                option = {
                    grid: {
                        left: '-5%',
                        right: '33%',
                        bottom: '1%',
                        top: '100%',
                        containLabel: true
                    },
                    xAxis: {
                        show: false,
                        type: 'value',
                        max: 349
                    },
                    yAxis: {
                        show: false,
                        type: 'category',
                        data: ["审核"]
                    },
                    series: [
                        {
                            type: 'bar',
                            margin: '50%',

                            showBackground: true,
                            barWidth: 8,
                            color: '#00B0FF',
                            data: [279],
                            label: {
                                color: '#999999',
                                formatter: '审核   '+ '349',
                                position: [195, 0],
                                show: true
                            },
                            itemStyle: {
                                normal: {
                                    barBorderRadius: 50,
                                }
                            }
                        }
                    ]
                };
                myChart20.on("mouseover", function (params){

                    myChart20.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart20.setOption(option);
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

                            if(pointX > 89){
                                x = pointX - boxWidth + 125;
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
                            hoverOffset: 3,
                            avoidLabelOverlap: false,
                            hoverAnimation:true,
                            radius: ['60%', '85%'],
                            label: {
                                normal: {
                                    show: false,
                                    position: 'center'
                                },

                            },
                            color: ["#00B0FF", "#13C2C2","#F3637B"],
                            data: [302, 47, 83]
                        }
                    ]
                };

                myChart18.setOption(option);
            });

        }
    });

</script>
