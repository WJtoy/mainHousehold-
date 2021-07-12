<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/reminderChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }

            var reminderQty = data.data.reminderQty;
            $("#reminderQty").text(reminderQty);
            var reminderFirstQty = data.data.reminderFirstQty;
            $("#reminderFirstQty").text(reminderFirstQty);
            var reminderMultipleQty = data.data.reminderMultipleQty;
            $("#reminderMultipleQty").text(reminderMultipleQty);
            var exceed48hourReminderQty = data.data.exceed48hourReminderQty;
            $("#exceed48hourReminderQty").text(exceed48hourReminderQty);
            var reminderRate = data.data.reminderRate;
            $("#reminderRate").text(reminderRate+"%");
            var reminderFirstRate = data.data.reminderFirstRate;
            $("#reminderFirstRate").text(reminderFirstRate+"%");
            var reminderMultipleRate = data.data.reminderMultipleRate;
            $("#reminderMultipleRate").text(reminderMultipleRate+"%");
            var exceed48hourReminderRate = data.data.exceed48hourReminderRate;
            $("#exceed48hourReminderRate").text(exceed48hourReminderRate+"%");

            var complete24hourRate = data.data.complete24hourRate;
            $("#complete24hourRate").text(complete24hourRate+"%");
            var over48ReminderCompletedRate = data.data.over48ReminderCompletedRate;
            $("#over48ReminderCompletedRate").text(over48ReminderCompletedRate+"%");

            var unComplete24hourRate = 100 - complete24hourRate;
            var over48ReminderUnCompletedRate = 100 - over48ReminderCompletedRate;

            var myChart4 = echarts.init(document.getElementById('main5'));
            var myChart5 = echarts.init(document.getElementById('main6'));
            setTimeout(function () {
                option = {

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
                                complete24hourRate,
                                unComplete24hourRate
                            ]
                        }
                    ]

                };
                myChart4.on("mouseover", function (params){

                    myChart4.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart4.setOption(option);

            });

            setTimeout(function () {
                option = {

                    series: [
                        {
                            name: '访问来源',
                            type: 'pie',
                            radius: ['75%', '100%'],
                            hoverAnimation : false,
                            label: {
                                show: false,
                                position: 'center'
                            },
                            color: ["#00B0FF", "#F6F6F6"],
                            data: [
                                over48ReminderCompletedRate,
                                over48ReminderUnCompletedRate
                            ]
                        }
                    ]

                };
                myChart5.on("mouseover", function (params){

                    myChart5.dispatchAction({
                        type: 'downplay'
                    });
                });
                myChart5.setOption(option);
            });

        }
    });

</script>