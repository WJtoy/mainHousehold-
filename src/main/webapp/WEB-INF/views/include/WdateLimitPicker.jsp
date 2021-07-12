<%@ page contentType="text/html;charset=UTF-8" %>
<!-- 限定Wdate控件选择日期范围 -->
<script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
<script type="text/javascript">
    //初始化日期范围选择：1年
    function oneYearDatePicker(startId,endId,isShowClear){
        $('#' + startId).unbind("click");
        $('#' + startId).bind("click",function(){
            WdatePicker({
                readOnly: true,
                dateFmt: 'yyyy-MM-dd',
                isShowClear: isShowClear | false,
                maxDate: '#F{$dp.$D(\'' + endId + '\')||\'%y-%M-%d\'}',
                minDate: '#F{$dp.$D(\'' + endId + '\',{y:-1,d:+1})}'
            });
        });

        $('#' + endId).unbind("click");
        $('#' + endId).bind("click",function(){
            WdatePicker({
                dateFmt: 'yyyy-MM-dd',
                isShowClear: isShowClear | false,
                minDate: '#F{$dp.$D(\''+ startId + '\')||\'%y-%M-%d\'}',
                maxDate: '#F{$dp.$D(\'' + startId + '\',{y:1,d:-1})}'
            });
        });
    }

    function noLimitDatePicker(startId,endId,isShowClear,endDateCanAfterToday){
        $('#' + startId).unbind("click");
        $('#' + startId).bind("click",function(){
            var endDate = $("#"+endId).val();
            if(endDate === "") {
                WdatePicker({
                    readOnly: true,
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: isShowClear | false
                });
            }else{
                WdatePicker({
                    readOnly: true,
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: isShowClear | false,
                    maxDate: '#F{$dp.$D(\'' + endId + '\')||\'%y-%M-%d\'}'
                });
            }
        });

        $('#' + endId).unbind("click");
        $('#' + endId).bind("click",function(){
            var start = $("#"+startId).val();
            if(!endDateCanAfterToday){
                WdatePicker({
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: isShowClear | false,
                    minDate: '#F{$dp.$D(\''+ startId + '\')||\'%y-%M-%d\'}',
                    maxDate: '#F{\'%y-%M-%d\'}'
                });
            }else{
                if(start === ""){
                    WdatePicker({
                        dateFmt: 'yyyy-MM-dd',
                        isShowClear: isShowClear | false,
                        maxDate: '#F{\'%y-%M-%d\'}'
                    });
                }else {
                    WdatePicker({
                        dateFmt: 'yyyy-MM-dd',
                        isShowClear: isShowClear | false,
                        minDate: '#F{$dp.$D(\'' + startId + '\')||\'%y-%M-%d\'}'
                    });
                }
            }
        });
    }

    //初始化日期范围选择：1年
    function customerLimitDatePicker(startId,endId,month,isShowClear){
        if(month && month >0) {
            $('#' + startId).unbind("click");
            $('#' + startId).bind("click", function () {
                WdatePicker({
                    readOnly: true,
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: isShowClear | false,
                    maxDate: '#F{$dp.$D(\'' + endId + '\')||\'%y-%M-%d\'}',
                    minDate: '#F{$dp.$D(\'' + endId + '\',{M:-' + month + ',d:+1})}'
                });
            });

            $('#' + endId).unbind("click");
            $('#' + endId).bind("click", function () {
                WdatePicker({
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: isShowClear | false,
                    minDate: '#F{$dp.$D(\'' + startId + '\')||\'%y-%M-%d\'}',
                    maxDate: '#F{$dp.$D(\'' + startId + '\',{M:+' + month + ',d:-1})}'
                });
            });
        }
    }
</script>