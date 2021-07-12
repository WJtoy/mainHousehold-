<script type="text/javascript">
    //search and goto page 1
    function search(){
        beforePage();
        $("#searchForm").submit();
        return false;
    }

    function setPage(p){
        if(p){
            $("#pageNo").val(p);
        }else{
            $("#pageNo").val(1);
        }
        return true;
    }

    //go to page
    function page(n, s) {
        beforePage();
        if(n) $("#pageNo").val(n);
        if(s) $("#pageSize").val(s);
        $("#searchForm").submit();
        return false;
    }

    function beforePage(){}

    //refresh this page
    function repage() {
        //beforePage();
        $("#searchForm").submit();
        return false;
    }
    function reload(){
        repage();
    }
</script>