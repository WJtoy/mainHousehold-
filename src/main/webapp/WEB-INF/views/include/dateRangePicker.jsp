<%@ page contentType="text/html;charset=UTF-8" %>
<!-- date range picker -->
<link href="${ctxStatic}/jquery-daterangepicker/daterangepicker.min.css" type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/jquery-daterangepicker/jquery.daterangepicker.min.js" type="text/javascript"></script>
<script type="text/javascript">
    //自定义日期选择控件
    function myOneYearDateRangePicker(id) {
        var year = moment().get('year');
        $("#"+id).dateRangePicker({
            language: 'cn',
            autoClose: false,
            startOfWeek: 'monday',
            separator: ' ~ ',
            format: 'YYYY-MM-DD',
            time: {
                enabled: false
            },
            swapTime: true,
            minDays: 1,
            maxDays: 365,
            endDate: moment().endOf('day').format('YYYY-MM-DD'),
            showWeekNumbers: true,
            selectForward: false,
            shortcuts: null,
            showShortcuts: true,
            monthSelect: true,
            yearSelect: true,
            yearSelect: [2017, year],
            // customArrowPrevSymbol: '<i class="fa fa-arrow-circle-left"></i>',
            // customArrowNextSymbol: '<i class="fa fa-arrow-circle-right"></i>',
            customShortcuts:
                [
                    //two years ago
                    {
                        name: (year-2)+'年',
                        dates: function () {
                            var start = moment((year-2) + '-01-01').toDate();
                            var end = moment((year-2)+'-12-31').toDate();
                            return [start, end];
                        }
                    },
                    // prev year
                    {
                        name: (year-1)+'年',
                        dates: function () {
                            var start = moment((year-1) + '-01-01').toDate();
                            var end = moment((year-1)+'-12-31').toDate();
                            return [start, end];
                        }
                    },
                    //this year
                    {
                        name: year+'年',
                        dates: function () {
                            var start = moment(year + '-01-01').toDate();
                            var end = moment().toDate();
                            return [start, end];
                        }
                    },
                    {
                        name: '最近半年',
                        dates: function () {
                            var start = moment().subtract(6, 'M').toDate();
                            var end = moment().toDate();
                            return [start, end];
                        }
                    },
                    {
                        name: '最近三个月',
                        dates: function () {
                            var start = moment().subtract(3, 'M').toDate();
                            // var start = moment().subtract(89,'d').toDate();
                            var end = moment().toDate();
                            return [start, end];
                        }
                    },
                    {
                        name: '最近两个月',
                        dates: function () {
                            var start = moment().subtract(2, 'M').toDate();
                            var end = moment().toDate();
                            return [start, end];
                        }
                    },
                    {
                        name: '最近-个月',
                        dates: function () {
                            var start = moment().subtract(1, 'M').toDate();
                            var end = moment().toDate();
                            return [start, end];
                        }
                    }
                ]
        });
        // $("div.drp_top-bar").find(".apply-btn").before("<input type=\"button\" id=\"" + id + "-reset\" class=\"apply-btn\" value=\"清空\">");
        // resetSelectDate(id);
    }
    function resetSelectDate(id){
        $("#"+id+"-reset").click(function(evt)
        {
            evt.stopPropagation();
            $('#'+id).data('dateRangePicker').clear();
        });
    }
</script>