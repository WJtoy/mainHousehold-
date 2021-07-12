<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/servicePointQtyChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }

            var frequentQtyRate = data.data.frequentQtyRate;
            var trialQtyRate = data.data.trialQtyRate;
            var frequentQty = data.data.frequentQty;
            var trialQty = data.data.trialQty;

            var servicePointTotal = frequentQty + trialQty;
            if (servicePointTotal) {
                $("#servicePointTotal").text(servicePointTotal);

            }

            var servicePointProductCategory1 = "0";
            var ProductCategory1Rate = "0";
            var productCategory1;
            if (data.data.servicePointProductCategoryQty != null) {
                if (data.data.servicePointProductCategoryQty[0] != null) {
                    productCategory1 = data.data.servicePointProductCategoryQty[0];
                    servicePointProductCategory1 = productCategory1.total;
                    ProductCategory1Rate = productCategory1.totalRate;
                }
            }
            $("#ProductCategory1Rate").text(ProductCategory1Rate + "%");


            var servicePointProductCategory2 = "0";
            var ProductCategory2Rate = "0";
            if (data.data.servicePointProductCategoryQty != null) {
                if (data.data.servicePointProductCategoryQty[1] != null) {
                    var productCategory2 = data.data.servicePointProductCategoryQty[1];
                    servicePointProductCategory2 = productCategory2.total;
                    ProductCategory2Rate = productCategory2.totalRate;
                }
            }
            $("#ProductCategory2Rate").text(ProductCategory2Rate + "%");


            var servicePointProductCategory3 = "0";
            var ProductCategory3Rate = "0";
            if (data.data.servicePointProductCategoryQty != null) {
                if (data.data.servicePointProductCategoryQty[2] != null) {
                    var productCategory3 = data.data.servicePointProductCategoryQty[2];
                    servicePointProductCategory3 = productCategory3.total;
                    ProductCategory3Rate = productCategory3.totalRate;

                }
            }
            $("#ProductCategory3Rate").text(ProductCategory3Rate + "%");

            var autoPlanProductCategory1Name = "";
            var autoPlan1Rate = "0";
            var autoPlan1Qty = 0;
            if(data.data.servicePointAutoPlanQty != null) {
                if (data.data.servicePointAutoPlanQty[0] != null) {
                    var autoPlan1 = data.data.servicePointAutoPlanQty[0];
                    autoPlanProductCategory1Name = autoPlan1.productCategoryName;
                    autoPlan1Rate = autoPlan1.totalRate;
                    autoPlan1Qty = autoPlan1.autoPlanQty;
                }
            }

            $("#autoPlanProductCategory1Name").text(autoPlanProductCategory1Name);
            $("#autoPlan1Rate").text(autoPlan1Rate + "%");
            $("#autoPlan1Qty").text(autoPlan1Qty);


            var autoPlan2Rate = "0";
            var autoPlanProductCategory2Name = "";
            var autoPlan2Qty = "0";
            if(data.data.servicePointAutoPlanQty != null) {
                if (data.data.servicePointAutoPlanQty[1] != null) {
                    var autoPlan2 = data.data.servicePointAutoPlanQty[1];
                    autoPlanProductCategory2Name = autoPlan2.productCategoryName;
                    autoPlan2Rate = autoPlan2.totalRate;
                    autoPlan2Qty = autoPlan2.autoPlanQty;
                }
            }

            $("#autoPlanProductCategory2Name").text(autoPlanProductCategory2Name);
            $("#autoPlan2Rate").text(autoPlan2Rate + "%");
            $("#autoPlan2Qty").text(autoPlan2Qty);


            var autoPlan3Rate = "0";
            var autoPlanProductCategory3Name = "";
            var autoPlan3Qty = "0";
            if(data.data.servicePointAutoPlanQty != null) {
                if (data.data.servicePointAutoPlanQty[2] != null) {
                    var autoPlan3 = data.data.servicePointAutoPlanQty[2];
                    autoPlanProductCategory3Name = autoPlan3.productCategoryName;
                    autoPlan3Rate = autoPlan3.totalRate;
                    autoPlan3Qty = autoPlan3.autoPlanQty;
                }
            }

            $("#autoPlanProductCategory3Name").text(autoPlanProductCategory3Name);
            $("#autoPlan3Rate").text(autoPlan3Rate + "%");
            $("#autoPlan3Qty").text(autoPlan3Qty);

            var myChart8 = echarts.init(document.getElementById('main9'));
            var myChart9 = echarts.init(document.getElementById('main10'));
            var myChart10 = echarts.init(document.getElementById('main11'));
            var myChart11 = echarts.init(document.getElementById('main12'));

            if(servicePointTotal) {
                var frequentPosition = "bottom";
                if(frequentQty / servicePointTotal * 100<= 10){
                    frequentPosition = [0,15];
                }
                var trialPosition = "bottom";
                if(trialQty / servicePointTotal * 100 <= 15){
                    trialPosition = [-15,15];
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
                            max: servicePointTotal

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
                                data: [frequentQty],
                                label: {
                                    backgroundColor:'#EBF9FF',
                                    position: frequentPosition,
                                    formatter: frequentQtyRate + "%   " + frequentQty,
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
                                data: [trialQty],
                                label: {
                                    position: trialPosition,
                                    backgroundColor:'#E8FFFF',
                                    formatter: trialQtyRate + "%   " + trialQty,
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


                    myChart8.on("mouseover", function (params) {

                        myChart8.dispatchAction({
                            type: 'downplay'
                        });
                    });
                    myChart8.setOption(option);
                });
            }
            if(productCategory1 != null) {
                //环形图1
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
                                text: productCategory1.productCategoryName + '\n\n' + servicePointProductCategory1,
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
                                    },

                                },
                                color: ["#00B0FF", "#13C2C2"],
                                data: [productCategory1.frequentQty, productCategory1.trialQty]
                            }
                        ]
                    };

                    myChart9.setOption(option);

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
                                    x = pointX - boxWidth + 130;
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
                                text: productCategory2.productCategoryName + '\n\n' + servicePointProductCategory2,
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

                                color: ["#00B0FF", "#13C2C2"],
                                data: [productCategory2.frequentQty, productCategory2.trialQty]
                            }
                        ]
                    };


                    myChart10.setOption(option);
                });

            }
            if(productCategory3 != null) {
                //环形图3
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
                                text: productCategory3.productCategoryName + '\n\n' + servicePointProductCategory3,
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
                                color: ["#00B0FF", "#13C2C2"],
                                data: [productCategory3.frequentQty, productCategory3.trialQty]
                            }
                        ]
                    };

                    myChart11.setOption(option);
                });
            }

        }
    });

</script>