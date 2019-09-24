var req_data = {
    URL: 'http://localhost:4242',
    uri_path: '/books',
    method: '',
    url: '',
    response: '',
    tbl: '',
    error: ''
};

/*
(function() {
        if (window.location.hostname.includes('localhost')) {
            return 'http://localhost:4242';
        } else {
            return window.location.protocol + '//' + window.location.hostname + window.location.pathname;
        }
    })()
*/

(function($){

    $('#mainForm').on('submit', function(e) {
        e.preventDefault();

        var pResponse = $("#response");
        pResponse.removeClass('error');
        pResponse.hide()
        pResponse.fadeIn(750);

        // update req_data.method
        if (!req_data.method) updateSubmit();

        // get form values
        var title =  $('#title').val();
        var author = $('#author').val();
        var notes = $('#notes').val();

        // set URL
        var url = (function() {
            var temp = req_data.URL + req_data.uri_path;
            return temp;
        })();
        req_data.url = url;

        // structure JSON data
        var data;
        if (req_data.method.match(/post|put/i)) {
            var jsonObj = {
                "title": title,
                "author": author,
                "notes": notes
            };
            data = JSON.stringify(jsonObj);
        }

        // ajax request call
        $.ajax({
            type: 'GET', //req_data.method,
            url: req_data.url,
            dataType : 'json',
            contentType: 'application/json; charset=utf-8',
            data: jsonObj,
            beforeSend : function(xhr) {},

            success: function(response) {
                req_data.response = response;
                // req_data.tbl += '<table class="table table-hover table-inverse table-responsive">' +
                //         '<thead class="thead-inverse">' +
                //         '<tr>' +
                //             '<th>Question</th>' +
                //             '<th>Answer</th>' +
                //         '</tr>' +
                //         '</thead>' +
                //         '<tbody>';
                req_data.tbl += '<div class="container response responsive">' +
                            '<div class="header">' +
                                '<div class="col1">BookId</div>' +
                                '<div class="col2">Title</div>' +
                                '<div class="col3">Author</div>' +
                                '<div class="col4">Notes</div>' +
                            '</div>' +
                            '<div class="data-rows">';
                var typ = typeof response;
                if (typ == 'object') {
                    createTblRowsAnswer(response);
                    // req_data.tbl += '</tbody></table>';
                    req_data.tbl += '</div></div>';
                    pResponse.html(req_data.tbl);
                    req_data.tbl = '';
                } else {
                    pResponse.html(response);
                }
                //formatSuccessReponse();
            },
            error: function( xhr, request, error ) {
                req_data.error = 'Error: ' + xhr.responseText +
                    '\nStatus Code: ' + xhr.status;
                pResponse.html('');
                pResponse.addClass('error');
                try {
                    req_data['errorObj'] = eval(xhr.responseText);
                    req_data['errorObj'].forEach(function(e) {
                        pResponse.append(e.message + '\n');
                    });
                } catch (e) {
                    pResponse.html(req_data.error);
                }
            }
        });
    });

})(jQuery);

function formatSuccessReponse() {
    if (req_data.uri_path == '/word') {
        var newVal = $('div.data-rows > div > div.col2').html().replace(/[0-9]\)/g, "<br>");
        $('div.data-rows > div > div.col2').html(newVal);
    }
    var rCol1 = $('#response > div > div.data-rows > div > div.col1');
    var rCol2 = $('#response > div > div.data-rows > div > div.col2');
    rCol1.css("color", "#0C0C8E");
    rCol1.css("font-size", "xx-large");
    rCol1.css("padding-top", "3vh");
    rCol1.css("font-weight", "bold");
    rCol2.css("padding-top", "2vh");
    rCol2.css("height", "20vh")
    rCol2.css("margin-left", "5vh");
    rCol2.css('color', '#4C2A85');
    rCol2.css('font-size', 'larger');

}

function formatErrorReponse() {
    var res = $('#response');
    res.css()
}

function createTblRowsAnswer(r) {
    var col1, col2, col3, col4;
    switch (req_data.uri_path) {
        case '/books': {
            col1 = 'bookId';
            col2 = 'title';
            col3 = 'author';
            col4 = 'notes';
            break;
        }
        default: break;
    }
    /*
    var msg = '';
                    entry[col4].forEach( function(note) {
                        msg += '(ID ' + note['noteId'] + ') ' + note['note'];
                    });
    */

    if (false) {
        if (Array.isArray(r)) {
            r.forEach(function(entry) {
                req_data.tbl += '<tr>' +
                        '<td class="col1">' + entry[col1] + '</td>' +
                        '<td class="col2">' + entry[col2] + '</td>' +
                        '</tr>';
            });
        } else if (typeof r == 'object') {
            req_data.tbl += '<tr>' +
                        '<td class="col1">' + r[col1] + '</td>' +
                        '<td class="col2">' + r[col2] + '</td>' +
                        '</tr>';
        }
    } else {
        if (Array.isArray(r)) {
            r.forEach(function(entry) {
                var msg = '';
                if (entry[col4].length != 0) {
                    entry[col4].forEach( function(note) {
                        msg += '(ID ' + note['noteId'] + ') ' + note['note'] + '<br>';
                    });
                } else {
                    msg += "--------";
                }
                req_data.tbl += '<div class="dataRow">' +
                        '<div class="col1">' + entry[col1] + '</div>' +
                        '<div class="col2">' + entry[col2] + '</div>' +
                        '<div class="col3">' + entry[col3] + '</div>' +
                        '<div class="col4">' + msg + '</div>' +
                        '</div>';
            });
        } else if (typeof r == 'object') {
            var msg = '';
            if (r[col4].length != 0) {
                r[col4].forEach( function(note) {
                    msg += '(ID ' + note['noteId'] + ') ' + note['note'] + '<br>';
                });
            } else {
                msg += "--------";
            }
            req_data.tbl += '<div class="dataRow">' +
                        '<div class="col1">' + r[col1] + '</div>' +
                        '<div class="col2">' + r[col2] + '</div>' +
                        '<div class="col3">' + r[col3] + '</div>' +
                        '<div class="col4">' + msg + '</div>' +
                        '</div>';
        }
    }

}

function createTblRows(r) {
    var entries;
    if (Array.isArray(r)) {
        entries = r;
    } else {
        entries = Object.entries(r);
    }
    entries.forEach(function(entry) {
        if (typeof entry[1] == 'object') {
            createTblRows(entry[1]);
        } else {
            req_data.tbl += '<tr><td>' + entry[0] + '</td><td>' + entry[1] + '</td></tr>' + entry[2] + '</td></tr>' +
                    entry[3] + '</td></tr>';
        }
    })
}