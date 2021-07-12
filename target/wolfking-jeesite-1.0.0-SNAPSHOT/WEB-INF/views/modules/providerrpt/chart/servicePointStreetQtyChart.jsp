
<%@ page contentType="text/html;charset=UTF-8"%>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/servicePointStreetChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }
            if(data.data.servicePointStreetQty != null) {
                var servicePointStreet = data.data.servicePointStreetQty.servicePointStreet;
                $("#servicePointStreet").text(servicePointStreet);
                var frequentServicePoint = data.data.servicePointStreetQty.frequentServicePoint;
                var trialServicePoint = data.data.servicePointStreetQty.trialServicePoint;
                var withoutServicePoint = data.data.servicePointStreetQty.withoutServicePoint;

                var frequentServicePointRate = data.data.servicePointStreetQty.frequentServicePointRate;
                var trialServicePointRate = data.data.servicePointStreetQty.trialServicePointRate;
                var withoutServicePointRate = data.data.servicePointStreetQty.withoutServicePointRate;
            }

            if(data.data.servicePointProductCategoryQty != null) {
                var productCategory1 = data.data.servicePointProductCategoryQty[0];
                var productCategory2 = data.data.servicePointProductCategoryQty[1];
                var productCategory3 = data.data.servicePointProductCategoryQty[2];
            }

            var autoPlanStreet = "0";
            var autoPlanStreetRate = "0";
            var autoPlanWithoutFrequent = "0";
            var frequentWithoutAutoPlan = "0";
            if(data.data.servicePointStreetAutoPlanQty != null) {
                autoPlanStreet = data.data.servicePointStreetAutoPlanQty.autoPlanStreet;
                autoPlanStreetRate = data.data.servicePointStreetAutoPlanQty.autoPlanStreetRate;
                autoPlanWithoutFrequent = data.data.servicePointStreetAutoPlanQty.autoPlanWithoutFrequent;
                frequentWithoutAutoPlan = data.data.servicePointStreetAutoPlanQty.frequentWithoutAutoPlan;
            }

            $("#autoPlanStreet").text(autoPlanStreet);
            $("#autoPlanStreetRate").text(autoPlanStreetRate +"%");
            $("#autoPlanWithoutFrequent").text(autoPlanWithoutFrequent);
            $("#frequentWithoutAutoPlan").text(frequentWithoutAutoPlan);

            var myChart12 = echarts.init(document.getElementById('main13'));
            var myChart13 = echarts.init(document.getElementById('main14'));
            var myChart14 = echarts.init(document.getElementById('main15'));
            var myChart15 = echarts.init(document.getElementById('main16'));

            if(servicePointStreet) {
                var frequentPosition = "bottom";
                var trialPosition = "bottom";

                if(frequentServicePoint / servicePointStreet * 100 <= 10){
                    frequentPosition = [0,15];
                    trialPosition = [55,15];
                }

                setTimeout(function () { //柱状图
                    option = {
                        tooltip: {
                            trigger: 'axis',
                            formatter: '{c}%',
                            axisPointer: {
                                type: 'shadow'
                            }
                        },
                        grid: {
                            left: '3%',
                            right: '1%',
                            bottom: '3%',
                            top: '2%',
                            containLabel: true
                        },
                        xAxis: {
                            show: false,
                            type: 'value',
                            max: servicePointStreet

                        },
                        yAxis: {
                            show: false,
                            type: 'category'
                        },
                        series: [
                            {
                                type: 'bar',
                                margin: '50%',
                                color: '#00B0FF',
                                showBackground: true,
                                barWidth: 10,
                                stack: '数量',
                                data: [frequentServicePoint],
                                label: {
                                    position: frequentPosition,
                                    formatter: frequentServicePointRate + "%  " + frequentServicePoint,
                                    show: true
                                },
                                itemStyle: {
                                    normal: {
                                        barBorderRadius: [20, 0, 0, 20]
                                    }
                                }
                            },
                            {
                                type: 'bar',
                                margin: '50%',
                                color: '#13C2C2',
                                showBackground: true,
                                barWidth: 10,
                                stack: '数量',
                                data: [trialServicePoint],
                                label: {
                                    position: trialPosition,
                                    formatter: trialServicePointRate + "%  " + trialServicePoint,
                                    show: true

                                }
                            },
                            {
                                type: 'bar',
                                margin: '50%',
                                color: '#F3637B',
                                showBackground: true,
                                barWidth: 10,
                                stack: '数量',
                                data: [withoutServicePoint],
                                label: {
                                    position: 'bottom',
                                    formatter: withoutServicePointRate + "%  " + withoutServicePoint,
                                    show: true

                                },
                                itemStyle: {
                                    normal: {
                                        barBorderRadius: [0, 20, 20, 0]
                                    }
                                }
                            }

                        ]
                    };
                    myChart12.on("mouseover", function (params) {

                        myChart12.dispatchAction({
                            type: 'downplay'
                        });
                    });
                    myChart12.setOption(option);
                });
            }
            //环形图1
            if(productCategory1 != null) {
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

                                if (pointX > 90) {
                                    x = pointX - boxWidth + 140;
                                } else {
                                    x = pointX - boxWidth - 10;
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
                        graphic: [{ //环形图中间添加文字
                            type: 'text', //通过不同top值可以设置上下显示
                            left: 'center',
                            top: 'center',
                            style: {
                                text: productCategory1.productCategoryName,
                                textAlign: 'center',
                                fill: '#666666', //文字的颜色
                                fontSize: 12

                            }
                        }],
                        series: [
                            {

                                type: 'pie',
                                radius: ['65%', '90%'],
                                hoverOffset: 3,
                                avoidLabelOverlap: false,
                                hoverAnimation: true,
                                label: {
                                    normal: {
                                        show: false,

                                    },
                                },
                                color: ["#00B0FF", "#13C2C2", "#F3637B"],
                                data: [productCategory1.frequentServicePoint, productCategory1.trialServicePoint, productCategory1.withoutServicePoint]
                            }
                        ]
                    };

                    myChart13.setOption(option);
                });
            }
            if(productCategory2 != null) {
                //环形图2
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

                                if (pointX > 90) {
                                    x = pointX - boxWidth + 140;
                                } else {
                                    x = pointX - boxWidth - 10;
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
                        graphic: [{ //环形图中间添加文字
                            type: 'text', //通过不同top值可以设置上下显示
                            left: 'center',
                            top: 'center',
                            style: {
                                text: productCategory2.productCategoryName,
                                textAlign: 'center',
                                fill: '#666666', //文字的颜色
                                fontSize: 12

                            }
                        }],
                        series: [
                            {

                                type: 'pie',
                                radius: ['65%', '90%'],
                                hoverOffset: 3,
                                avoidLabelOverlap: false,
                                hoverAnimation: true,
                                label: {
                                    normal: {
                                        show: false
                                    }

                                },
                                color: ["#00B0FF", "#13C2C2", "#F3637B"],
                                data: [productCategory2.frequentServicePoint, productCategory2.trialServicePoint, productCategory2.withoutServicePoint]
                            }
                        ]
                    };

                    myChart14.setOption(option);
                });
            }
            if(productCategory2 != null) {
                //环形图3
                setTimeout(function () {
                    option = {
                        tooltip: {
                            trigger: 'item',
                            formatter: function (params) {
                                var res = '';
                                res += params.marker + params.percent + '% : ' + params.value;
                                return res;
                            }
                        },
                        graphic: [{ //环形图中间添加文字
                            type: 'text', //通过不同top值可以设置上下显示
                            left: 'center',
                            top: 'center',
                            style: {
                                text: productCategory3.productCategoryName,
                                textAlign: 'center',
                                fill: '#666666', //文字的颜色
                                fontSize: 12

                            }
                        }],
                        series: [
                            {
                                type: 'pie',
                                radius: ['65%', '90%'],
                                hoverOffset: 3,
                                avoidLabelOverlap: false,
                                hoverAnimation: true,
                                label: {
                                    normal: {
                                        show: false,
                                    }
                                },
                                color: ["#00B0FF", "#13C2C2", "#F3637B"],
                                data: [productCategory3.frequentServicePoint, productCategory3.trialServicePoint, productCategory3.withoutServicePoint]
                            }
                        ]
                    };

                    myChart15.setOption(option);
                });
            }
        }
    });

</script>
