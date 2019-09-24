function updateSubmit() {
    var methodSelect = $('#method');
    var formVal1Div = $('#formVal1');
    var methodVal = methodSelect.val()
    var method = methodSelect.val().match(/(post|get|put|delete)/igm)[0]
    req_data.uri_path = '/books';
    if (methodVal.match(/post/i)) {
        if (formVal1Div.is(':hidden')) formVal1Div.slideDown(1000);
        req_data.method = method;
    } else {
        if (formVal1Div.is(':visible')) formVal1Div.slideUp(1000);
        req_data.method = method;
    }
}