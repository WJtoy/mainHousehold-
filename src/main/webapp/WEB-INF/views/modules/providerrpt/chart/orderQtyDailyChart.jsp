<%@ page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript">

    $.ajax({
        type: "GET",
        url: "${ctx}/rpt/provider/dataDrawingList/orderQtyDailyChart?endDate=" + ${rptSearchCondition.endDate.getTime()},
        success: function (data) {
            if (ajaxLogout(data)) {
                return false;
            }
            var uncompletedOrderQty = data.data.uncompletedOrderQty;
            $("#uncompletedOrderQty").text(uncompletedOrderQty);

            var cancelledOrderQty = data.data.cancelledOrderQty;
            $("#cancelledOrderQty").text(cancelledOrderQty);
            var completedOrderQty = data.data.completedOrderQty;
            $("#completedOrderQty").text(completedOrderQty);
            var financialAuditQty = data.data.financialAuditQty;
            $("#financialAuditQty").text(financialAuditQty);
            var autoFinancialAuditQty = data.data.autoFinancialAuditQty;
            $("#autoFinancialAuditQty").text(autoFinancialAuditQty);
            var abnormalOrderQty = data.data.abnormalOrderQty;
            $("#abnormalOrderQty").text(abnormalOrderQty);
            var autoCompletedQty = data.data.autoCompletedQty;
            $("#autoCompletedQty").text(autoCompletedQty);
            var autoCompletedRate = data.data.autoCompletedRate;
            $("#autoCompletedRate").text(autoCompletedRate + "%");
        }
    });
</script>
