<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/customerComplaintChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }

            var customerComplain = data.data.customerComplain;
            $("#customerComplain").text(customerComplain);
            var validComplain = data.data.validComplain;
            $("#validComplain").text(validComplain);
            var mediumPoorEvaluate = data.data.mediumPoorEvaluate;
            $("#mediumPoorEvaluate").text(mediumPoorEvaluate);
            var validComplainRate = data.data.validComplainRate;
            $("#validComplainRate").text(validComplainRate+"%");
            var mediumPoorEvaluateRate = data.data.mediumPoorEvaluateRate;
            $("#mediumPoorEvaluateRate").text(mediumPoorEvaluateRate+"%");


            var novalidComplain = 100 - validComplainRate;
            var nomediumPoorEvaluate = 100 - mediumPoorEvaluateRate;

            var myChart6 = echarts.init(document.getElementById('main7'));
            var myChart7 = echarts.init(document.getElementById('main8'));
            setTimeout(function () {
                option = {
                    graphic: [{ //环形图中间添加文字
                        type: 'text', //通过不同top值可以设置上下显示
                        left: 'center',
                        top: '45%',
                        style: {
                            text: '有效',
                            textAlign: 'center',
                            fill: '#666666 100%', //文字的颜色
                            fontSize: 12
                        }
                    }],

                    series: [
                        {
                            type: 'pie',
                            radius: ['75%', '100%'],
                            hoverAnimation : false,
                            label: {
                                show: false,
                                position: 'center'
                            },
                            color: ["#00B0FF", "#F6F6F6"],
                            data: [
                                validComplainRate,
                                novalidComplain
                            ]
                        }
                    ]

                };
                myChart6.on("mouseover", function (params){

                    myChart6.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart6.setOption(option);

            });

            setTimeout(function () {
                option = {
                    graphic: [{ //环形图中间添加文字
                        type: 'text', //通过不同top值可以设置上下显示
                        left: 'center',
                        top: '45%',
                        style: {
                            text: '中差评',
                            textAlign: 'center',
                            fill: '#666666 100%', //文字的颜色
                            fontSize: 12
                        }
                    }],

                    series: [
                        {
                            type: 'pie',
                            radius: ['75%', '100%'],
                            hoverAnimation : false,
                            label: {
                                show: false,
                                position: 'center'
                            },
                            color: ["#00B0FF", "#F6F6F6"],
                            data: [
                                mediumPoorEvaluate,
                                nomediumPoorEvaluate
                            ]
                        }
                    ]

                };
                myChart7.on("mouseover", function (params){

                    myChart7.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart7.setOption(option);
            });

        }
    });

</script>
